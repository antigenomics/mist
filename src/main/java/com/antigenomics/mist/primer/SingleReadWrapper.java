package com.antigenomics.mist.primer;

import com.milaboratory.core.io.sequence.SequenceRead;
import com.milaboratory.core.sequence.NSequenceWithQuality;

public class SingleReadWrapper implements ReadWrapper {
    private final SequenceRead read;

    public SingleReadWrapper(SequenceRead read) {
        this.read = read;
    }

    @Override
    public SequenceRead getRead() {
        return read;
    }

    @Override
    public NSequenceWithQuality getData(int index, boolean reverse) {
        if (index > 1 || index < 0)
            throw new IndexOutOfBoundsException("Allowed indexes for read wrapper are 0 and 1.");
        return read.getRead(0).getData();
    }
}
