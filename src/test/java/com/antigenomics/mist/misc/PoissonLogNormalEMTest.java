package com.antigenomics.mist.misc;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.distribution.PoissonDistribution;
import org.junit.Test;

import java.util.Random;

public class PoissonLogNormalEMTest {
    private static final Random rnd = new Random(6585);

    @Test
    public void mixtureTest() {
        double lambdaExpected = 0.3, muExpected = 4.0, sigmaExpected = 0.5,
                gaussianRatio = 0.7;

        NormalDistribution gaussianDistr = new NormalDistribution(muExpected, sigmaExpected);
        PoissonDistribution poissonDistr = new PoissonDistribution(lambdaExpected);
        PoissonLogNormalEM model = new PoissonLogNormalEM();

        int n = 1000;

        for (int i = 0; i < n; i++) {
            int x = rnd.nextDouble() < gaussianRatio ?
                    (int) Math.pow(2.0, gaussianDistr.sample()) :
                    poissonDistr.sample();

            if (x > 0) {  // in case we sample 0
                model.update(x);
            }
        }

        model.run(5);

        System.out.println(model.getLambda());
        System.out.println(model.getMu());
        System.out.println(model.getSigma());
        System.out.println(model.getGaussianRatio());
    }
}
