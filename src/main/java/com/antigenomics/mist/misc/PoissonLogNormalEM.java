package com.antigenomics.mist.misc;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.distribution.PoissonDistribution;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.HashMap;
import java.util.Map;

// non thread-safe
public class PoissonLogNormalEM {
    private static final int N_EM_PASSES = 100;
    private static final double JITTER = 1e-6;
    private final Map<Integer, Element> elements = new HashMap<>();
    private double lambda, mu, sigma, gaussianRatio;

    public PoissonLogNormalEM() {

    }

    public void update(int x) {
        Element element = elements.computeIfAbsent(x, tmp -> new Element(x));
        element.incrementCounter();
    }

    public void run(int thresholdGuess) {
        for (Element element : elements.values()) {
            element.logNormalProb = element.value < thresholdGuess ? 0.0 : 1.0;
        }

        // Estimate unseen species in order to properly compute Poisson mixture weight

        double f1 = elements.containsKey(1) ? elements.get(1).count : 0.0,
                f2 = elements.containsKey(2) ? elements.get(2).count : 0.0,
                unseenSpeciesCount = f1 * (f1 - 1) / 2 / (f2 + 1);

        for (int i = 0; i < N_EM_PASSES; i++) {
            double logNormalEstimateProbSum = 0,
                    logNormalProbSum = 0,
                    poissonProbSum = 0;


            // M-step

            mu = 0;
            lambda = 0;
            sigma = 0;

            for (Element element : elements.values()) {
                double logNormalFactor = element.count * element.logNormalProb,
                        poissonFactor = element.count * (1.0 - element.logNormalProb),
                        logNormalEstimateFactor = logNormalFactor * element.value;

                mu += logNormalEstimateFactor * element.getLog2Value();
                sigma += logNormalEstimateFactor * element.getLog2Value() * element.getLog2Value();
                lambda += poissonFactor * element.value;

                logNormalEstimateProbSum += logNormalEstimateFactor;
                logNormalProbSum += logNormalFactor;
                poissonProbSum += poissonFactor;
            }

            mu /= logNormalEstimateProbSum;     // <x>
            sigma /= logNormalEstimateProbSum;  // <x^2>
            sigma -= mu * mu;                   // <x^2> - <x>^2
            sigma = Math.sqrt(sigma);

            // Here we estimate the remainder of poissonProbSum for unobserved elements (x=0)

            poissonProbSum += unseenSpeciesCount > 0 ? unseenSpeciesCount *
                    solveLambda(
                            poissonProbSum / unseenSpeciesCount,
                            lambda / unseenSpeciesCount
                    ) :
                    0;

            lambda /= poissonProbSum;


            // E-step
            // Prior

            gaussianRatio = logNormalProbSum / (logNormalProbSum + poissonProbSum);

            NormalDistribution normalDistribution = new NormalDistribution(mu, sigma + JITTER);
            PoissonDistribution poissonDistribution = new PoissonDistribution(lambda + JITTER);

            for (Element element : elements.values()) {
                double logNormalProb = normalDistribution.density(element.getLog2Value()),
                        poissonProb = poissonDistribution.probability(element.value);

                // Bayesian update
                element.logNormalProb = logNormalProb * gaussianRatio /
                        (gaussianRatio * logNormalProb + (1.0 - gaussianRatio) * poissonProb);
            }

            /*
            System.out.println("[EM-iter#" + i + "]");
            System.out.println("priorG=" + gaussianRatio);
            System.out.println("mu=" + mu);
            System.out.println("sigma=" + sigma);
            System.out.println("lambda=" + lambda);
            */
        }
    }

    public double estimateThreshold() {
        throw new NotImplementedException();
    }

    protected boolean classify(int x) {
        // This is too straightforward implementation
        // we assume that no new 'x' will be introduced here
        assert elements.containsKey(x);
        return elements.get(x).logNormalProb >= 0.5;
    }

    public double getLambda() {
        return lambda;
    }

    public double getMu() {
        return mu;
    }

    public double getSigma() {
        return sigma;
    }

    public double getLogNormalRatio() {
        return gaussianRatio;
    }

    private static double solveLambda(double K, double T) {
        int nIter = 100;
        double tol = 0.001;
        double x = 1.0;

        // Newton - Rhapson variation
        for (int i = 0; i < nIter; i++) {
            double f = f(x, T, K), df = df(x, T), ddf = ddf(x);
            double xprev = x;
            x += 0.5 * ddf * f * f / df / df / df - f / df;

            if (Math.abs(x - xprev) < tol) {
                break;
            }
        }

        return Math.exp(-x);
    }

    private static double f(double x, double T, double K) {
        return x * Math.exp(-x) + x * T - K;
    }

    private static double df(double x, double T) {
        return Math.exp(-x) - x * Math.exp(-x) + T;
    }

    private static double ddf(double x) {
        return -2 * Math.exp(-x) + x * Math.exp(-x);
    }

    private class Element {
        private double logNormalProb = 0.5;
        private int count = 0;
        private final int value;

        public Element(int value) {
            this.value = value;
        }


        public double getLog2Value() {
            return Math.log(value) / Math.log(2.0);
        }

        public void incrementCounter() {
            count++;
        }
    }
}
