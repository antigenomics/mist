package com.antigenomics.mist.assemble;

import cc.redberry.pipe.OutputPort;
import com.antigenomics.mist.ReadGenerator;
import com.antigenomics.mist.umi.UmiTag;
import com.milaboratory.core.io.sequence.SingleRead;
import com.milaboratory.core.io.sequence.SingleReadImpl;
import com.milaboratory.core.sequence.NSequenceWithQuality;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.random.Well19937c;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MigGenerator implements OutputPort<Mig<SingleRead>> {
    private final int umiLength, readLength, maxTrim5, maxTrim3;
    private final byte meanQual;
    private final ReadGenerator readGenerator;
    private final NormalDistribution norm;
    private final Random rnd;

    public MigGenerator(int umiLength, int readLength, int maxTrim5, int maxTrim3, byte meanQual,
                        double log2CoverageMean) {
        this(umiLength, readLength, maxTrim5, maxTrim3, meanQual, log2CoverageMean, 1.0, 51102);
    }

    public MigGenerator(int umiLength, int readLength, int maxTrim5, int maxTrim3, byte meanQual,
                        double log2CoverageMean,
                        double log2CoverageStd, int seed) {
        this.umiLength = umiLength;
        this.readLength = readLength;
        this.maxTrim5 = maxTrim5;
        this.maxTrim3 = maxTrim3;
        this.meanQual = meanQual;
        this.readGenerator = new ReadGenerator(seed);
        this.norm = new NormalDistribution(new Well19937c(seed),
                log2CoverageMean, log2CoverageStd);
        this.rnd = new Random(seed);
    }

    @Override
    public SyntheticMig take() {
        NSequenceWithQuality consensus = readGenerator.randomRead(readLength, meanQual);

        List<SingleRead> reads = new ArrayList<>();

        int count = (int) Math.pow(2, norm.sample());

        for (int i = 0; i < count; i++) {
            NSequenceWithQuality read = readGenerator.mutate(consensus);

            int trim5 = rnd.nextInt(maxTrim5 + 1),
                    trim3 = rnd.nextInt(maxTrim3 + 1);

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
}
