package com.antigenomics.mist.misc;

import com.antigenomics.mist.umi.UmiCoverageStatistics;
import org.apache.commons.math3.distribution.LogNormalDistribution;
import org.apache.commons.math3.distribution.PoissonDistribution;
import org.apache.commons.math3.exception.MathInternalError;

import java.util.HashMap;
import java.util.Map;

// non thread-safe
public class PoissonLogNormalEM {
    private static final int N_EM_PASSES = 100;
    private static final double JITTER = 1e-21;
    private final Map<Integer, Element> elements = new HashMap<>();

    public PoissonLogNormalEM() {

    }

    public void update(int x) {
        Element element = elements.computeIfAbsent(x, tmp -> new Element(x));
        element.incrementCounter();
    }

    public PoissonLogNormalModel run(int thresholdGuess) {
        for (Element element : elements.values()) {
            element.logNormalProb = element.value < thresholdGuess ? 0.0 : 1.0;
        }

        // Estimate unseen species in order to properly compute Poisson mixture weight

        double f1 = elements.containsKey(1) ? elements.get(1).count : 0.0,
                f2 = elements.containsKey(2) ? elements.get(2).count : 0.0,
                unseenSpeciesCount = f1 * (f1 - 1) / 2 / (f2 + 1);

        PoissonLogNormalModel model = null;

        for (int i = 0; i < N_EM_PASSES; i++) {
            // M-step
            double poissonMean = 0, logNormalMean = 0, logNormalStd = 0,
                    logNormalEstimateProbSum = 0, logNormalProbSum = 0, poissonProbSum = 0;

            for (Element element : elements.values()) {
                double logNormalFactor = element.count * element.logNormalProb,
                        poissonFactor = element.count * (1.0 - element.logNormalProb),
                        logNormalEstimateFactor = logNormalFactor * element.value;

                logNormalMean += logNormalEstimateFactor * element.value;
                logNormalStd += logNormalEstimateFactor * element.value * element.value;
                poissonMean += poissonFactor * element.value;

                logNormalEstimateProbSum += logNormalEstimateFactor;
                logNormalProbSum += logNormalFactor;
                poissonProbSum += poissonFactor;
            }

            // Zero division common here

            logNormalProbSum += JITTER;
            poissonProbSum += JITTER;

            logNormalMean /= logNormalEstimateProbSum;
            // <x^2> - <x>^2
            logNormalStd /= logNormalEstimateProbSum;
            logNormalStd -= logNormalMean * logNormalMean;
            logNormalStd = Math.sqrt(logNormalStd);

            // Here we estimate the remainder of poissonProbSum for unobserved elements (x=0)

            poissonProbSum += unseenSpeciesCount > 0 ? unseenSpeciesCount *
                    solveLambda(
                            poissonProbSum / unseenSpeciesCount,
                            poissonMean / unseenSpeciesCount
                    ) :
                    0;

            poissonMean /= poissonProbSum;


            // E-step
            // Prior

            double logNormalPrior = logNormalProbSum / (logNormalProbSum + poissonProbSum);

            model = new PoissonLogNormalModel(poissonMean, logNormalMean, logNormalStd, logNormalPrior);

            for (Element element : elements.values()) {
                double logNormalProb = model.computeLogNormalDensity(element.value),
                        poissonProb = model.computePoissonDensity(element.value);

                // Bayesian update
                element.logNormalProb = logNormalProb * logNormalPrior /
                        (logNormalPrior * logNormalProb + (1.0 - logNormalPrior) * poissonProb);
            }
            /*
            System.out.println("[EM-iter#" + i + "]");
            System.out.println("priorG=" + logNormalPrior);
            System.out.println("mu=" + mu);
            System.out.println("sigma=" + sigma);
            System.out.println("lambda=" + lambda);
            */
        }

        return model;
    }

    private static double solveLambda(double K, double T) {
        int nIter = 100;
        double atol = 1e-3, eps = 1e-14;
        double x0 = 1.0, x1 = x0;
        boolean found = false;

        // Newton - Rhapson like solver
        for (int i = 0; i < nIter; i++) {
            double f = f(x0, T, K), df = df(x0, T);

            if (Math.abs(df) < eps) {
                break;
            }

            x1 = x0 - f / df;

            if (Math.abs(x1 - x0) < atol) {
                found = true;
                break;
            }

            x0 = x1;
        }

        return found ? Math.exp(-x1) : 0.0;
    }

    private static double f(double x, double T, double K) {
        return x * Math.exp(-x) + x * T - K;
    }

    private static double df(double x, double T) {
        return Math.exp(-x) - x * Math.exp(-x) + T;
    }

    private static double ddf(double x) {
        return -2.0 * Math.exp(-x) + x * Math.exp(-x);
    }

    private class Element {
        double logNormalProb = 0.5;
        int count = 0;
        final int value;

        public Element(int value) {
            this.value = value;
        }

        public void incrementCounter() {
            count++;
        }
    }

    public static class PoissonLogNormalModel {
        private static final double LOG2 = Math.log(2);
        private final double lambda, mu, sigma, logNormalPrior;
        private final LogNormalDistribution logNormalDistribution;
        private final PoissonDistribution poissonDistribution;
        private double missingDensity = -1, weightedHistogramDensityNorm = -1;

        public PoissonLogNormalModel(double poissonMean, double logNormalMean, double logNormalStd, double logNormalPrior) {
            this.lambda = poissonMean;
            this.mu = Math.log(logNormalMean / Math.sqrt(1.0 + logNormalStd * logNormalStd / logNormalMean / logNormalMean));
            this.sigma = Math.sqrt(Math.log(1.0 + logNormalStd * logNormalStd / logNormalMean / logNormalMean));
            this.logNormalPrior = logNormalPrior;
            this.logNormalDistribution = new LogNormalDistribution(this.mu, this.sigma + JITTER);
            this.poissonDistribution = new PoissonDistribution(lambda + JITTER);
        }

        private void computeHistogramNorms() {
            if (missingDensity < 0) {
                missingDensity = logNormalPrior * logNormalDistribution.cumulativeProbability(1) +
                        (1.0 - logNormalPrior) * poissonDistribution.cumulativeProbability(1);

                weightedHistogramDensityNorm = 0;

                for (int i = 0; i < UmiCoverageStatistics.MAX_UMI_COVERAGE; i++) {
                    int x = (int) Math.pow(2, i);
                    weightedHistogramDensityNorm += computeCoverageHistogramDensity(x) * x;
                }
            }
        }

        public double computeCoverageFilteringProbability(int x) {
            double pP = poissonDistribution.cumulativeProbability(x),
                    pLN = logNormalDistribution.cumulativeProbability(x);
            return (1.0 - logNormalPrior) * pP / (logNormalPrior * pLN + (1.0 - logNormalPrior) * pP);
        }

        public double computeLogNormalDensity(int x) {
            return logNormalDistribution.density(x);
        }

        public double computePoissonDensity(int x) {
            return poissonDistribution.probability(x);
        }

        public int estimateThreshold() {
            int prevThreshold = -1;
            for (double pValue : new double[]{0.001, 0.01, 0.05}) {
                int threshold = estimateThreshold(0.05, pValue);
                if (threshold < 0) {
                    return prevThreshold;
                } else {
                    prevThreshold = threshold;
                }
            }
            return prevThreshold;
        }

        public int estimateThreshold(double falsePositiveThreshold, double falseNegativeThreshold) {
            int fpX, fnX;
            try {
                fpX = poissonDistribution.inverseCumulativeProbability(Math.min(1.0,
                        1.0 - falsePositiveThreshold +
                                logNormalPrior * logNormalDistribution.cumulativeProbability(1))); // missing density
                fnX = (int) logNormalDistribution.inverseCumulativeProbability(falseNegativeThreshold);
            } catch (MathInternalError e) {
                return -1;
            }

            if (fpX > fnX) {
                return -1;
            }

            return (fpX + fnX) / 2;
        }

        public double computeCoverageHistogramDensity(int x) {
            computeHistogramNorms();

            int from = (int) (Math.log(x) / LOG2), to = from + 1;

            from = (int) Math.pow(2.0, from);
            to = (int) Math.pow(2.0, to);

            double p = logNormalPrior * logNormalDistribution.probability(from, to) +
                    (1.0 - logNormalPrior) * poissonDistribution.cumulativeProbability(from, to);

            return p / (1.0 - missingDensity);
        }

        public double computeCoverageHistogramDensityWeighted(int x) {
            return computeCoverageHistogramDensity(x) * x / weightedHistogramDensityNorm;
        }

        public double getLambda() {
            return lambda;
        }

        public double getMu() {
            return mu;
        }

        public int getPeakPosition() {
            return (int) Math.exp(mu);
        }

        public double getSigma() {
            return sigma;
        }

        public double getLogNormalRatio() {
            return logNormalPrior;
        }

        @Override
        public String toString() {
            return (float) logNormalPrior + " * LN(" + mu + ", " + sigma + ") + " +
                    (1 - (float) logNormalPrior) + " * Poiss(" + lambda + ")";
        }
    }
}
