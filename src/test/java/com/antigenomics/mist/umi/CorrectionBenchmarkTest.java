package com.antigenomics.mist.umi;

import org.junit.Assert;
import org.junit.Test;

public class CorrectionBenchmarkTest {
    @Test
    public void compareCoverageAndTreeBasedCorrection() throws InterruptedException {
        byte[] qualityValues = new byte[]{(byte) 25, (byte) 30, (byte) 35};
        double[] peakPositionValues = new double[]{2.0, 4.0, 6.0};

        int i = 0;
        for (byte quality : qualityValues) {
            for (double peakPosition : peakPositionValues) {
                System.out.println("Synthetic UMI correction method comparison #" + (++i) + ":\n" +
                        "quality=" + quality + "\n" +
                        "peakPosition=" + peakPosition);

                SyntheticUmiReadout syntheticUmiReadout = new SyntheticUmiReadout(1000,
                        12, quality, peakPosition, 1.0);

                CoverageCorrectionBenchmark coverageBenchmark = new CoverageCorrectionBenchmark(syntheticUmiReadout);
                TreeCorrectionBenchmark treeBenchmark = new TreeCorrectionBenchmark(syntheticUmiReadout);

                CorrectionBenchmarkStatistics covStats = coverageBenchmark.run(),
                        treeStats = treeBenchmark.run();

                System.out.println("Coverage-based:" + covStats);
                System.out.println("Tree-based:" + treeStats);
                System.out.println();

                if (covStats.getCorrectedErrors() > 0) {
                    Assert.assertTrue(covStats.getFalsePositiveRate() > 1.5 * treeStats.getFalsePositiveRate());
                    Assert.assertTrue(Math.abs(covStats.getTruePositiveRate() - treeStats.getTruePositiveRate()) < 0.05);
                } else {
                    // Coverage-based fails
                    Assert.assertTrue(treeStats.getFalsePositiveRate() < 0.01);
                    Assert.assertTrue(treeStats.getTruePositiveRate() > 0.80);
                }
            }
        }
    }

    // TODO: multi-peak tests
}
