package com.antigenomics.mist.preprocess;

import com.milaboratory.core.io.sequence.SequenceRead;
import com.milaboratory.core.sequence.NSequenceWithQuality;

public class PairedReadWrapper implements ReadWrapper {
    private final SequenceRead read;
    private NSequenceWithQuality[] cache = new NSequenceWithQuality[4];

    public PairedReadWrapper(SequenceRead read) {
        this.read = read;
        cache[0] = read.getRead(0).getData();
        cache[1] = read.getRead(1).getData();
    }

    @Override
    public SequenceRead getRead() {
        return read;
    }

    @Override
    public NSequenceWithQuality getData(int index, boolean reversed) {
        if (reversed && cache[2] == null) {
            cache[2] = read.getRead(1).getData().getReverseComplement();
            cache[3] = read.getRead(0).getData().getReverseComplement();
        }
        return cache[reversed ? 2 + index : index];
    }
}