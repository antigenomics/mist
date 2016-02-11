package com.antigenomics.mist;

import com.milaboratory.core.sequence.NSequenceWithQuality;
import com.milaboratory.core.sequence.NucleotideSequence;
import com.milaboratory.core.sequence.SequenceQuality;
import org.apache.commons.math3.distribution.PoissonDistribution;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well19937c;

import java.util.Random;

public class ReadGenerator {
    private final RandomGenerator randomGenerator;
    private final Random rnd;

    public ReadGenerator() {
        this(480011);
    }

    public ReadGenerator(int seed) {
        this.randomGenerator = new Well19937c(seed);
        this.rnd = new Random(seed);
    }

    public NucleotideSequence randomSequence(int length) {
        byte[] data = new byte[length];

        for (int i = 0; i < length; i++) {
            data[i] = (byte) rnd.nextInt(4);
        }

        return new NucleotideSequence(data);
    }

    public SequenceQuality randomQuality(int length, byte mean) {
        byte[] qual = new byte[length];
        PoissonDistribution poissonDistribution = new PoissonDistribution(randomGenerator,
                Math.max(1, 40 - mean), PoissonDistribution.DEFAULT_EPSILON, PoissonDistribution.DEFAULT_MAX_ITERATIONS);

        for (int i = 0; i < length; i++) {
            qual[i] = (byte) Math.min(Math.max(2, 40 - poissonDistribution.sample()), 40);
        }

        return new SequenceQuality(qual);
    }

    public NSequenceWithQuality mutate(NSequenceWithQuality nSequenceWithQuality) {
        byte[] bases = new byte[nSequenceWithQuality.size()];

        for (int i = 0; i < nSequenceWithQuality.size(); i++) {
            if (rnd.nextDouble() > 4 * nSequenceWithQuality.getQuality().probabilityOfErrorAt(i) / 3) {
                bases[i] = nSequenceWithQuality.getSequence().codeAt(i);
            } else {
                bases[i] = (byte) rnd.nextInt(4);
            }
        }

        return new NSequenceWithQuality(new NucleotideSequence(bases), nSequenceWithQuality.getQuality());
    }
}
