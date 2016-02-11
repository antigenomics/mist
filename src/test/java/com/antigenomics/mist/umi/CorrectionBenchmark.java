package com.antigenomics.mist.umi;

public abstract class CorrectionBenchmark {
    protected final SyntheticUmiReadout syntheticUmiReadout;

    public CorrectionBenchmark(int numberOfUmis, int umiLength, byte meanQual, double log2CoverageMean,
                               double log2CoverageStd) {
        this.syntheticUmiReadout = new SyntheticUmiReadout(numberOfUmis, umiLength, meanQual,
                log2CoverageMean, log2CoverageStd);
    }


    public CorrectionBenchmark(SyntheticUmiReadout syntheticUmiReadout) {
        this.syntheticUmiReadout = syntheticUmiReadout;
    }

    public CorrectionBenchmarkStatistics run() throws InterruptedException {
        int totalErrors = 0, correctedErrors = 0, weakCorrectedErrors = 0,
                totalGood = 0, miscorrectedGood = 0;

        for (SyntheticUmiReadout.UmiParentChildPair umiParentChildPair : syntheticUmiReadout.getReads()) {
            if (umiParentChildPair.isError()) {
                totalErrors++;
                if (corrected(umiParentChildPair)) {
                    correctedErrors++;
                } else if (weaklyCorrected(umiParentChildPair)) {
                    weakCorrectedErrors++;
                }
            } else {
                totalGood++;
                if (!corrected(umiParentChildPair)) {
                    miscorrectedGood++;
                }
            }
        }

        return new CorrectionBenchmarkStatistics(totalErrors, correctedErrors, weakCorrectedErrors,
                totalGood, miscorrectedGood);
    }

    protected abstract boolean corrected(SyntheticUmiReadout.UmiParentChildPair umiParentChildPair);

    protected abstract boolean weaklyCorrected(SyntheticUmiReadout.UmiParentChildPair umiParentChildPair);
}
