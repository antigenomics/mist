package com.antigenomics.mist.umi;

public class CoverageCorrectionBenchmark extends CorrectionBenchmark {
    private final int threshold;

    public CoverageCorrectionBenchmark(int numberOfUmis, int umiLength, byte meanQual, double log2CoverageMean, double log2CoverageStd) {
        super(numberOfUmis, umiLength, meanQual, log2CoverageMean, log2CoverageStd);
        this.threshold = syntheticUmiReadout.getUmiCoverageStatistics().getThresholdEstimate();
    }

    public CoverageCorrectionBenchmark(SyntheticUmiReadout syntheticUmiReadout) {
        super(syntheticUmiReadout);
        this.threshold = syntheticUmiReadout.getUmiCoverageStatistics().getThresholdEstimate();
    }

    @Override
    protected boolean corrected(SyntheticUmiReadout.UmiParentChildPair umiParentChildPair) {
        return umiParentChildPair.isError() ?
                (umiParentChildPair.getChildCoverageAndQuality().getCoverage() < threshold) :
                (umiParentChildPair.getChildCoverageAndQuality().getCoverage() >= threshold);
    }

    @Override
    protected boolean weaklyCorrected(SyntheticUmiReadout.UmiParentChildPair umiParentChildPair) {
        return false;
    }

    public int getThreshold() {
        return threshold;
    }
}
