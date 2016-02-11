package com.antigenomics.mist.umi;

import cc.redberry.pipe.OutputPort;
import com.antigenomics.mist.ReadGenerator;
import com.milaboratory.core.sequence.NSequenceWithQuality;
import com.milaboratory.core.sequence.NucleotideSequence;
import com.milaboratory.core.sequence.SequenceQuality;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.random.Well19937c;

import java.util.*;

public class SyntheticUmiReadout {
    private final int numberOfUmis, umiLength;
    private final byte meanQual;
    private final double log2CoverageMean, log2CoverageStd;
    private final UmiCoverageStatistics umiCoverageStatistics;
    private final UmiAccumulator umiAccumulator;
    private final UmiErrorAndDiversityModel umiErrorAndDiversityModel;
    private final List<UmiParentChildPair> reads;
    private final Set<NucleotideSequence> umis;

    public SyntheticUmiReadout(int numberOfUmis, int umiLength, byte meanQual, double log2CoverageMean,
                               double log2CoverageStd) {
        this(numberOfUmis, umiLength, meanQual, log2CoverageMean, log2CoverageStd, 480011);
    }

    public SyntheticUmiReadout(int numberOfUmis, int umiLength, byte meanQual, double log2CoverageMean,
                               double log2CoverageStd, int seed) {
        this.numberOfUmis = numberOfUmis;
        this.umiLength = umiLength;
        this.meanQual = meanQual;
        this.log2CoverageMean = log2CoverageMean;
        this.log2CoverageStd = log2CoverageStd;
        this.reads = new ArrayList<>();
        this.umiAccumulator = new UmiAccumulator();
        this.umiErrorAndDiversityModel = new UmiErrorAndDiversityModel();
        this.umiCoverageStatistics = new UmiCoverageStatistics();
        this.umis = new HashSet<>();

        ReadGenerator readGenerator = new ReadGenerator(seed);

        NormalDistribution norm = new NormalDistribution(new Well19937c(seed),
                log2CoverageMean, log2CoverageStd);

        for (int i = 0; i < numberOfUmis; i++) {
            int count = (int) Math.pow(2, norm.sample());

            NucleotideSequence umiSeq = readGenerator.randomSequence(umiLength);
            SequenceQuality quality = readGenerator.randomQuality(umiLength, meanQual);
            umis.add(umiSeq);

            for (int j = 0; j < count; j++) {
                NSequenceWithQuality read = readGenerator.mutate(new NSequenceWithQuality(umiSeq, quality));
                umiAccumulator.put(read);
                reads.add(new UmiParentChildPair(umiSeq, read));
            }
        }

        OutputPort<UmiCoverageAndQuality> op = umiAccumulator.getOutputPort();
        UmiCoverageAndQuality umiCoverageAndQuality;
        while ((umiCoverageAndQuality = op.take()) != null) {
            umiCoverageStatistics.put(umiCoverageAndQuality);
            umiErrorAndDiversityModel.put(umiCoverageAndQuality);
        }
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

    public Set<NucleotideSequence> getUmis() {
        return Collections.unmodifiableSet(umis);
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
            UmiTag parentTag = new UmiTag(parent);
            return umiAccumulator.hasTag(parentTag) ? umiAccumulator.getAt(new UmiTag(parent)) :
                    new UmiCoverageAndQuality(parentTag, 0, new SequenceQuality(new byte[parent.size()]));
        }

        public UmiCoverageAndQuality getChildCoverageAndQuality() {
            return umiAccumulator.getAt(new UmiTag(child.getSequence()));
        }
    }
}
