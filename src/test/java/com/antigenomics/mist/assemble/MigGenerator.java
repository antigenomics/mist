package com.antigenomics.mist.assemble;

import cc.redberry.pipe.OutputPort;
import com.antigenomics.mist.ReadGenerator;
import com.antigenomics.mist.umi.UmiTag;
import com.milaboratory.core.io.sequence.SingleRead;
import com.milaboratory.core.io.sequence.SingleReadImpl;
import com.milaboratory.core.sequence.NSequenceWithQuality;
import org.apache.commons.math3.distribution.AbstractIntegerDistribution;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.distribution.PoissonDistribution;
import org.apache.commons.math3.random.Well19937c;

import java.util.ArrayList;
import java.util.List;

public class MigGenerator implements OutputPort<Mig<SingleRead>> {
    private final int umiLength, readLength;
    private final byte meanQual;
    private final ReadGenerator readGenerator;
    private final NormalDistribution norm;
    private final AbstractIntegerDistribution trim5Rng, trim3Rng;

    public MigGenerator(int umiLength, int readLength, double meanTrim5, double meanTrim3, byte meanQual,
                        double log2CoverageMean) {
        this(umiLength, readLength, meanTrim5, meanTrim3, meanQual, log2CoverageMean, 1.0, 51102);
    }

    public MigGenerator(int umiLength, int readLength, double meanTrim5, double meanTrim3, byte meanQual,
                        double log2CoverageMean,
                        double log2CoverageStd, int seed) {
        this.umiLength = umiLength;
        this.readLength = readLength;
        this.meanQual = meanQual;
        this.readGenerator = new ReadGenerator(seed);
        this.norm = new NormalDistribution(new Well19937c(seed),
                log2CoverageMean, log2CoverageStd);
        this.trim5Rng = meanTrim5 == 0 ? new DummyRandomGenerator() :
                new PoissonDistribution(new Well19937c(seed), meanTrim5,
                        PoissonDistribution.DEFAULT_EPSILON, PoissonDistribution.DEFAULT_MAX_ITERATIONS);
        this.trim3Rng = meanTrim3 == 0 ? new DummyRandomGenerator() :
                new PoissonDistribution(new Well19937c(seed), meanTrim3,
                        PoissonDistribution.DEFAULT_EPSILON, PoissonDistribution.DEFAULT_MAX_ITERATIONS);
    }

    @Override
    public SyntheticMig take() {
        NSequenceWithQuality consensus = readGenerator.randomRead(readLength, meanQual);

        List<SingleRead> reads = new ArrayList<>();

        int count = (int) Math.pow(2, norm.sample());

        for (int i = 0; i < count; i++) {
            NSequenceWithQuality read = readGenerator.mutate(consensus);

            int trim5 = trim5Rng.sample(),
                    trim3 = trim3Rng.sample();

            reads.add(new SingleReadImpl(-1,
                    read.getRange(trim5, read.size() - trim3),
                    ""));
        }

        return new SyntheticMig(new UmiTag(readGenerator.randomSequence(umiLength)),
                reads, consensus);
    }

    public class SyntheticMig extends Mig<SingleRead> {
        private final NSequenceWithQuality consensus;

        public SyntheticMig(UmiTag umiTag, List<SingleRead> reads, NSequenceWithQuality consensus) {
            super(umiTag, reads);
            this.consensus = consensus;
        }

        public NSequenceWithQuality getConsensus() {
            return consensus;
        }
    }

    private class DummyRandomGenerator extends AbstractIntegerDistribution {
        public DummyRandomGenerator() {
            super(null);
        }

        @Override
        public double probability(int x) {
            return 0;
        }

        @Override
        public double cumulativeProbability(int x) {
            return 0;
        }

        @Override
        public double getNumericalMean() {
            return 0;
        }

        @Override
        public double getNumericalVariance() {
            return 0;
        }

        @Override
        public int getSupportLowerBound() {
            return 0;
        }

        @Override
        public int getSupportUpperBound() {
            return 0;
        }

        @Override
        public boolean isSupportConnected() {
            return false;
        }

        @Override
        public int sample() {
            return 0;
        }
    }
}
