package com.antigenomics.mist.misc;

import org.apache.commons.math3.distribution.LogNormalDistribution;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.distribution.PoissonDistribution;
import org.apache.commons.math3.random.Well19937c;
import org.junit.Assert;
import org.junit.Test;

import java.util.Random;

public class PoissonLogNormalEMTest {
    private static final Random rnd = new Random(6585);

    @Test
    public void mixtureTest() {
        double lambdaExpected = 0.3, muExpected = 4.0, sigmaExpected = 0.5,
                logNormalRatio = 0.7;

        int expectedThreshold = 4;

        LogNormalDistribution gaussianDistr = new LogNormalDistribution(new Well19937c(51102),
                muExpected, sigmaExpected);
        PoissonDistribution poissonDistr = new PoissonDistribution(new Well19937c(51102),
                lambdaExpected, PoissonDistribution.DEFAULT_EPSILON, PoissonDistribution.DEFAULT_MAX_ITERATIONS);
        PoissonLogNormalEM em = new PoissonLogNormalEM();

        int n = 10000;

        for (int i = 0; i < n; i++) {
            int x = rnd.nextDouble() < logNormalRatio ?
                    (int) gaussianDistr.sample() :
                    poissonDistr.sample();

            if (x > 0)
                em.update(x);
        }

        PoissonLogNormalEM.PoissonLogNormalModel model =  em.run(expectedThreshold);

        Assert.assertTrue(Math.abs(model.getLambda() - lambdaExpected) < 0.05);
        Assert.assertTrue(Math.abs(model.getMu() - muExpected) < 0.3);
        Assert.assertTrue(Math.abs(model.getSigma() - sigmaExpected) < 0.05);
        Assert.assertTrue(Math.abs(model.getLogNormalRatio() - logNormalRatio) < 0.05);
    }
}
