package com.antigenomics.mist.umi;

import org.junit.Assert;
import org.junit.Test;

public class UmiTreeTest {
    @Test
    public void syntheticLowCoverageUmiTest() throws InterruptedException {
        CorrectionBenchmarkStatistics result = new TreeCorrectionBenchmark(10000, 12, (byte) 30, 3.0, 1.0).run();

        System.out.println("Low coverage UMI tree correction test");
        System.out.println(result);

        Assert.assertTrue(result.getTruePositiveRate() > 0.90);
        Assert.assertTrue(result.getFalsePositiveRate() < 0.01);
    }

    @Test
    public void syntheticVeryLowCoverageUmiTest() throws InterruptedException {
        CorrectionBenchmarkStatistics result = new TreeCorrectionBenchmark(10000, 12, (byte) 30, 1.0, 1.0).run();

        System.out.println("Very low coverage UMI tree correction test");
        System.out.println(result);

        //System.out.println(result.getSubject().getUmiCoverageStatistics());

        Assert.assertTrue(result.getTruePositiveRate() > 0.65);
        Assert.assertTrue(result.getFalsePositiveRate() < 0.05);
    }


    @Test
    public void syntheticHighCoverageUmiTest() throws InterruptedException {
        CorrectionBenchmarkStatistics result = new TreeCorrectionBenchmark(10000, 12, (byte) 30, 6.0, 1.0).run();

        System.out.println("High coverage UMI tree correction test");
        System.out.println(result);

        Assert.assertTrue(result.getTruePositiveRate() > 0.95);
        Assert.assertTrue(result.getFalsePositiveRate() < 0.01);
    }

    @Test
    public void syntheticLowCoverageLowQualityUmiTest() throws InterruptedException {
        CorrectionBenchmarkStatistics result = new TreeCorrectionBenchmark(10000, 12, (byte) 25, 3.0, 1.0).run();

        System.out.println("Low coverage low quality UMI tree correction test");
        System.out.println(result);

        Assert.assertTrue(result.getTruePositiveRate() > 0.85);
        Assert.assertTrue(result.getFalsePositiveRate() < 0.01);
    }

    @Test
    public void syntheticShortUmiHighCoverageTest() throws InterruptedException {
        CorrectionBenchmarkStatistics result = new TreeCorrectionBenchmark(100, 6, (byte) 30, 6.0, 1.0).run();

        System.out.println("High coverage short UMI tree correction test");
        System.out.println(result);

        Assert.assertTrue(result.getWeakTruePositiveRate() > 0.8);
        Assert.assertTrue(result.getFalsePositiveRate() < 0.2);
    }
}
