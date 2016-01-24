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
    private static final double LOG2 = Math.log(2);
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
            double lambda = 0, mu = 0, sigma = 0,
                    logNormalEstimateProbSum = 0, logNormalProbSum = 0, poissonProbSum = 0;

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

            double logNormalPrior = logNormalProbSum / (logNormalProbSum + poissonProbSum);

            model = new PoissonLogNormalModel(lambda, mu, sigma, logNormalPrior);

            for (Element element : elements.values()) {
                double logNormalProb = model.computeLogNormalDensity(element.value),
                        poissonProb = model.computePoissonDensity(element.value);

                // Bayesian update
                element.logNormalProb = logNormalProb * logNormalPrior /
                        (logNormalPrior * logNormalProb + (1.0 - logNormalPrior) * poissonProb);
            }

            System.out.println("[EM-iter#" + i + "]");
            System.out.println("priorG=" + logNormalPrior);
            System.out.println("mu=" + mu);
            System.out.println("sigma=" + sigma);
            System.out.println("lambda=" + lambda);
        }

        return model;
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
        return -2.0 * Math.exp(-x) + x * Math.exp(-x);
    }

    private class Element {
        private double logNormalProb = 0.5;
        private int count = 0;
        private final int value;

        public Element(int value) {
            this.value = value;
        }

        public double getLog2Value() {
            return Math.log(value) / LOG2;
        }

        public void incrementCounter() {
            count++;
        }
    }

    public static class PoissonLogNormalModel {
        private final double lambda, mu, sigma, logNormalPrior;
        private final NormalDistribution normalDistribution;
        private final PoissonDistribution poissonDistribution;

        public PoissonLogNormalModel(double lambda, double mu, double sigma, double logNormalPrior) {
            this.lambda = lambda;
            this.mu = mu;
            this.sigma = sigma;
            this.logNormalPrior = logNormalPrior;
            this.normalDistribution = new NormalDistribution(mu, sigma + JITTER);
            this.poissonDistribution = new PoissonDistribution(lambda + JITTER);
        }

        public double computeLogNormalDensity(int x) {
            return normalDistribution.density(Math.log(x) / LOG2);
        }

        public double computePoissonDensity(int x) {
            return poissonDistribution.probability(x);
        }

        public int estimateThreshold() {
            throw new NotImplementedException();
        }

        public double computeDensity(int x) {
            return logNormalPrior * computeLogNormalDensity(x) + (1.0 - logNormalPrior) * computePoissonDensity(x);
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
            return logNormalPrior;
        }
    }
}
