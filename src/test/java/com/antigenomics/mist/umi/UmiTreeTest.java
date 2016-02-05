package com.antigenomics.mist.umi;

import org.junit.Assert;
import org.junit.Test;

public class UmiTreeTest {
    private static class TestResult {
        private final int totalErrors, correctedErrors,
                totalGood, miscorrectedGood;

        public TestResult(int totalErrors, int correctedErrors, int totalGood, int miscorrectedGood) {
            this.totalErrors = totalErrors;
            this.correctedErrors = correctedErrors;
            this.totalGood = totalGood;
            this.miscorrectedGood = miscorrectedGood;
        }

        public double getTruePositiveRate() {
            return correctedErrors / (double) totalErrors;
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

        public int getTotalGood() {
            return totalGood;
        }

        public int getMiscorrectedGood() {
            return miscorrectedGood;
        }
    }

    private TestResult test(double log2CoverageMean, double log2CoverageStd) {
        int numberOfUmis = 10000;

        SyntheticUmiStats syntheticUmiStats = new SyntheticUmiStats(numberOfUmis, 10, (byte) 30,
                log2CoverageMean, log2CoverageStd);

        UmiTree umiTree = new UmiTree(numberOfUmis);

        umiTree.update(syntheticUmiStats.getUmiAccumulator().getUmiInfoProvider());

        umiTree.traverseAndCorrect();

        int totalErrors = 0, correctedErrors = 0,
                totalGood = 0, miscorrectedGood = 0;

        for (SyntheticUmiStats.UmiParentChildPair umiParentChildPair : syntheticUmiStats.getReads()) {
            if (umiParentChildPair.isError()) {
                totalErrors++;
                if (correct(umiTree, umiParentChildPair)) {
                    correctedErrors++;
                }
            } else {
                totalGood++;
                if (!correct(umiTree, umiParentChildPair)) {
                    miscorrectedGood++;
                }
            }
        }

        return new TestResult(totalErrors, correctedErrors, totalGood, miscorrectedGood);
    }

    @Test
    public void syntheticLowCoverageUmiTest() {
        TestResult result = test(3.0, 1.0);

        System.out.println("Low coverage UMI tree correction test\nTP=" +
                result.getTruePositiveRate() + ", FP=" + result.getFalsePositiveRate());

        Assert.assertTrue(result.getTruePositiveRate() > 0.8);
        Assert.assertTrue(result.getFalsePositiveRate() < 0.1);
    }


    @Test
    public void syntheticHighCoverageUmiTest() {
        TestResult result = test(6.0, 1.0);

        System.out.println("Low coverage UMI tree correction test\nTP=" +
                result.getTruePositiveRate() + ", FP=" + result.getFalsePositiveRate());

        Assert.assertTrue(result.getTruePositiveRate() > 0.8);
        Assert.assertTrue(result.getFalsePositiveRate() < 0.1);
    }

    private static boolean correct(UmiTree umiTree, SyntheticUmiStats.UmiParentChildPair umiParentChildPair) {
        return umiTree.correct(umiParentChildPair.getChild().getSequence())
                .equals(umiParentChildPair.getParent());
    }
}
