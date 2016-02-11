package com.antigenomics.mist.umi;

import cc.redberry.pipe.CUtils;

public final class TreeCorrectionBenchmark extends CorrectionBenchmark {
    private final UmiTree umiTree;

    public TreeCorrectionBenchmark(int numberOfUmis, int umiLength, byte meanQual, double log2CoverageMean, double log2CoverageStd) {
        super(numberOfUmis, umiLength, meanQual, log2CoverageMean, log2CoverageStd);
        this.umiTree = new UmiTree(syntheticUmiReadout.getNumberOfUmis());
    }

    public TreeCorrectionBenchmark(SyntheticUmiReadout syntheticUmiReadout) throws InterruptedException {
        super(syntheticUmiReadout);
        this.umiTree = new UmiTree(syntheticUmiReadout.getNumberOfUmis());
    }

    @Override
    public CorrectionBenchmarkStatistics run() throws InterruptedException {
        CUtils.drain(syntheticUmiReadout.getUmiAccumulator().getOutputPort(), umiTree);

        umiTree.traverseAndCorrect();

        return super.run();
    }

    @Override
    protected boolean corrected(SyntheticUmiReadout.UmiParentChildPair umiParentChildPair) {
        return umiTree.correct(umiParentChildPair.getChild().getSequence())
                .equals(umiParentChildPair.getParent());
    }

    @Override
    protected boolean weaklyCorrected(SyntheticUmiReadout.UmiParentChildPair umiParentChildPair) {
        return syntheticUmiReadout.getUmis().contains(umiTree.correct(umiParentChildPair.getChild().getSequence()));
    }
}
