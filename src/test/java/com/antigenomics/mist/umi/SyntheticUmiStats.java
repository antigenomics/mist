package com.antigenomics.mist.umi;

import cc.redberry.pipe.CUtils;
import com.antigenomics.mist.TestUtil;
import com.milaboratory.core.sequence.NSequenceWithQuality;
import com.milaboratory.core.sequence.NucleotideSequence;
import com.milaboratory.core.sequence.SequenceQuality;
import org.apache.commons.math3.distribution.NormalDistribution;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SyntheticUmiStats {
    private final int numberOfUmis, umiLength;
    private final byte meanQual;
    private final double log2CoverageMean, log2CoverageStd;
    private final UmiCoverageStatistics umiCoverageStatistics;
    private final UmiAccumulator umiAccumulator;
    private final UmiErrorAndDiversityModel umiErrorAndDiversityModel;
    private final List<UmiParentChildPair> reads;
    private final List<NucleotideSequence> umis;

    public SyntheticUmiStats(int numberOfUmis, int umiLength, byte meanQual, double log2CoverageMean, double log2CoverageStd) {
        this.numberOfUmis = numberOfUmis;
        this.umiLength = umiLength;
        this.meanQual = meanQual;
        this.log2CoverageMean = log2CoverageMean;
        this.log2CoverageStd = log2CoverageStd;
        this.reads = new ArrayList<>();
        this.umiAccumulator = new UmiAccumulator();
        this.umiErrorAndDiversityModel = new UmiErrorAndDiversityModel();
        this.umis = new ArrayList<>();

        NormalDistribution norm = new NormalDistribution(TestUtil.randomGenerator,
                log2CoverageMean, log2CoverageStd);

        for (int i = 0; i < numberOfUmis; i++) {
            int count = (int) Math.pow(2, norm.sample());

            NucleotideSequence umiSeq = TestUtil.randomSequence(umiLength);
            SequenceQuality quality = TestUtil.randomQuality(umiLength, meanQual);
            umis.add(umiSeq);

            for (int j = 0; j < count; j++) {
                NSequenceWithQuality read = TestUtil.mutate(new NSequenceWithQuality(umiSeq, quality));
                umiAccumulator.update("test", read);
                reads.add(new UmiParentChildPair(umiSeq, read));
            }
        }

        UmiCoverageStatistics umiCoverageStatistics = new UmiCoverageStatistics();

        umiCoverageStatistics.put(umiAccumulator.getUmiInfoProvider());

        CUtils.drain();

        this.umiCoverageStatistics = umiStatistics.getUmiCoverageStatistics("test");

        umiErrorAndDiversityModel.update(umiAccumulator.getUmiInfoProvider());
    }

    public int getNumberOfUmis() {
        return numberOfUmis;
    }

    public int getUmiLength() {
        return umiLength;
    }

    public byte getMeanQual() {
        return meanQual;
    }

    public double getLog2CoverageMean() {
        return log2CoverageMean;
    }

    public double getLog2CoverageStd() {
        return log2CoverageStd;
    }

    public UmiCoverageStatistics getUmiCoverageStatistics() {
        return umiCoverageStatistics;
    }

    public List<UmiParentChildPair> getReads() {
        return Collections.unmodifiableList(reads);
    }

    public List<NucleotideSequence> getUmis() {
        return Collections.unmodifiableList(umis);
    }

    public UmiAccumulator getUmiAccumulator() {
        return umiAccumulator;
    }

    public UmiErrorAndDiversityModel getUmiErrorAndDiversityModel() {
        return umiErrorAndDiversityModel;
    }

    public class UmiParentChildPair {
        private final NucleotideSequence parent;
        private final NSequenceWithQuality child;

        public UmiParentChildPair(NucleotideSequence parent, NSequenceWithQuality child) {
            this.parent = parent;
            this.child = child;
        }

        public boolean isError() {
            return !parent.equals(child.getSequence());
        }

        public NucleotideSequence getParent() {
            return parent;
        }

        public NSequenceWithQuality getChild() {
            return child;
        }


        public UmiCoverageAndQuality getParentCoverageAndQuality() {
            return umiAccumulator.getAt(new UmiTag("test", parent));
        }

        public UmiCoverageAndQuality getChildCoverageAndQuality() {
            return umiAccumulator.getAt(new UmiTag("test", child.getSequence()));
        }
    }
}
