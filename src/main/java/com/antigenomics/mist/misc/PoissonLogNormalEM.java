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
        element.increment();
    }

    public void run(int thresholdGuess) {
        for (Element element : elements.values()) {
            element.gaussianProb = element.x < thresholdGuess ? 0.1 : 0.9;
        }

        double gaussianProbSum = 0, poissonProbSum = 0;
        for (int i = 0; i < N_EM_PASSES; i++) {
            // M-step
            mu = 0;
            lambda = 0;
            sigma = 0;
            for (Element element : elements.values()) {
                double gaussianProb = element.weight * element.gaussianProb,
                        poissonProb = element.weight * (1.0 - element.gaussianProb);
                mu += gaussianProb * element.logX;
                sigma += gaussianProb * element.logX * element.logX;
                lambda += poissonProb * element.x;

                gaussianProbSum += gaussianProb;
                poissonProbSum += poissonProb;
            }

            mu /= gaussianProbSum;     // <x>
            sigma /= gaussianProbSum;  // <x^2>
            sigma -= mu * mu;          // <x^2> - <x>^2
            sigma = Math.sqrt(sigma);
            lambda /= poissonProbSum;

            // E-step
            // Prior
            gaussianRatio = gaussianProbSum / (gaussianProbSum + poissonProbSum);

            System.out.println("[iter#" + i + "]");
            System.out.println("priorG=" + gaussianRatio);
            System.out.println("mu=" + mu);
            System.out.println("sigma=" + sigma);
            System.out.println("lambda=" + lambda);

            NormalDistribution normalDistribution = new NormalDistribution(mu, sigma + JITTER);
            PoissonDistribution poissonDistribution = new PoissonDistribution(lambda + JITTER);

            for (Element element : elements.values()) {
                double gaussianProb = normalDistribution.density(element.logX),
                        poissonProb = poissonDistribution.probability(element.x);

                // Bayesian update
                element.gaussianProb = gaussianProb * gaussianRatio /
                        (gaussianRatio * gaussianProb + (1.0 - gaussianRatio) * poissonProb);
            }
        }
    }

    public double estimateThreshold() {
        throw new NotImplementedException();
    }

    protected boolean classify(int x) {
        // This is too straightforward implementation
        // we assume that no new 'x' will be introduced here
        assert elements.containsKey(x);
        return elements.get(x).gaussianProb >= 0.5;
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

    public double getGaussianRatio() {
        return gaussianRatio;
    }

    private class Element {
        private double gaussianProb;
        private final int x;
        private final double logX;
        private int weight = 0;

        public Element(int x) {
            this.x = x;
            this.logX = Math.log(x) / Math.log(2.0);
            this.gaussianProb = 0;
        }

        public void increment() {
            weight += x;
        }
    }
}
