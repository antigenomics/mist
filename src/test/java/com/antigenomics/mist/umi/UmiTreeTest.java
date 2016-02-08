package com.antigenomics.mist.umi;

import org.junit.Assert;
import org.junit.Test;

public class UmiTreeTest {
    private static class TestResult {
        private final int totalErrors, correctedErrors, weakCorrectedErrors,
                totalGood, miscorrectedGood;
        private final SyntheticUmiStats subject;

        public TestResult(SyntheticUmiStats subject,
                          int totalErrors, int correctedErrors, int weakCorrectedErrors,
                          int totalGood, int miscorrectedGood) {
            this.subject = subject;
            this.totalErrors = totalErrors;
            this.correctedErrors = correctedErrors;
            this.weakCorrectedErrors = weakCorrectedErrors;
            this.totalGood = totalGood;
            this.miscorrectedGood = miscorrectedGood;
        }

        public SyntheticUmiStats getSubject() {
            return subject;
        }

        public double getTruePositiveRate() {
            return correctedErrors / (double) totalErrors;
        }

        public double getWeakTruePositiveRate() {
            return (weakCorrectedErrors + correctedErrors) / (double) totalErrors;
        }

        public double getFalsePositiveRate() {
            return miscorrectedGood / (double) totalGood;
        }

        public int getTotalErrors() {
            return totalErrors;
        }

        public int getCorrectedErrors() {
            return correctedErrors;
        }

        public int getWeakCorrectedErrors() {
            return weakCorrectedErrors;
        }

        public int getTotalGood() {
            return totalGood;
        }

        public int getMiscorrectedGood() {
            return miscorrectedGood;
        }
    }

    private TestResult test(int umiLen, byte meanQual, int numberOfUmis, double log2CoverageMean) {

        SyntheticUmiStats syntheticUmiStats = new SyntheticUmiStats(numberOfUmis, umiLen, meanQual,
                log2CoverageMean, 1.0);

        UmiTree umiTree = new UmiTree(numberOfUmis);

        umiTree.update(syntheticUmiStats.getUmiAccumulator().getUmiInfoProvider());

        umiTree.traverseAndCorrect();

        int totalErrors = 0, correctedErrors = 0, weakCorrectedErrors = 0,
                totalGood = 0, miscorrectedGood = 0;

        for (SyntheticUmiStats.UmiParentChildPair umiParentChildPair : syntheticUmiStats.getReads()) {
            if (umiParentChildPair.isError()) {
                totalErrors++;
                if (correct(umiTree, umiParentChildPair)) {
                    correctedErrors++;
                } else if (weakCorrect(umiTree, umiParentChildPair)) {
                    weakCorrectedErrors++;
                }
            } else {
                totalGood++;
                if (!correct(umiTree, umiParentChildPair)) {
                    miscorrectedGood++;
                }
            }
        }

        return new TestResult(syntheticUmiStats, totalErrors, correctedErrors, weakCorrectedErrors,
                totalGood, miscorrectedGood);
    }

    @Test
    public void syntheticLowCoverageUmiTest() {
        TestResult result = test(12, (byte) 30, 10000, 3.0);

        System.out.println("Low coverage UMI tree correction test\nTP=" +
                result.getTruePositiveRate() + ", TP*=" + result.getWeakTruePositiveRate() +
                ", FP=" + result.getFalsePositiveRate());

        Assert.assertTrue(result.getTruePositiveRate() > 0.95);
        Assert.assertTrue(result.getFalsePositiveRate() < 0.01);
    }

    @Test
    public void syntheticVeryLowCoverageUmiTest() {
        TestResult result = test(12, (byte) 30, 10000, 1.0);

        System.out.println("Very low coverage UMI tree correction test\nTP=" +
                result.getTruePositiveRate() + ", TP*=" + result.getWeakTruePositiveRate() +
                ", FP=" + result.getFalsePositiveRate());

        //System.out.println(result.getSubject().getUmiCoverageStatistics());

        Assert.assertTrue(result.getTruePositiveRate() > 0.65);
        Assert.assertTrue(result.getFalsePositiveRate() < 0.05);
    }


    @Test
    public void syntheticHighCoverageUmiTest() {
        TestResult result = test(12, (byte) 30, 10000, 6.0);

        System.out.println("High coverage UMI tree correction test\nTP=" +
                result.getTruePositiveRate() + ", TP*=" + result.getWeakTruePositiveRate() +
                ", FP=" + result.getFalsePositiveRate());

        Assert.assertTrue(result.getTruePositiveRate() > 0.95);
        Assert.assertTrue(result.getFalsePositiveRate() < 0.01);
    }

    @Test
    public void syntheticLowCoverageLowQualityUmiTest() {
        TestResult result = test(12, (byte) 25, 10000, 3.0);

        System.out.println("Low coverage low quality UMI tree correction test\nTP=" +
                result.getTruePositiveRate() + ", TP*=" + result.getWeakTruePositiveRate() +
                ", FP=" + result.getFalsePositiveRate());

        Assert.assertTrue(result.getTruePositiveRate() > 0.85);
        Assert.assertTrue(result.getFalsePositiveRate() < 0.01);
    }

    @Test
    public void syntheticShortUmiHighCoverageTest() {
        TestResult result = test(6, (byte) 30, 100, 6.0);

        System.out.println("High coverage short UMI tree correction test\nTP=" +
                result.getTruePositiveRate() + ", TP*=" + result.getWeakTruePositiveRate() +
                ", FP=" + result.getFalsePositiveRate());

        Assert.assertTrue(result.getWeakTruePositiveRate() > 0.9);
        Assert.assertTrue(result.getFalsePositiveRate() < 0.2);
    }

    private static boolean correct(UmiTree umiTree, SyntheticUmiStats.UmiParentChildPair umiParentChildPair) {
        return umiTree.correct(umiParentChildPair.getChild().getSequence())
                .equals(umiParentChildPair.getParent());
    }

    private static boolean weakCorrect(UmiTree umiTree, SyntheticUmiStats.UmiParentChildPair umiParentChildPair) {
        return !umiTree.correct(umiParentChildPair.getChild().getSequence())
                .equals(umiParentChildPair.getChild().getSequence());
    }
}
