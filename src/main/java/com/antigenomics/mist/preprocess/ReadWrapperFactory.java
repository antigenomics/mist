package com.antigenomics.mist.preprocess;

import com.milaboratory.core.io.sequence.SequenceRead;

public class ReadWrapperFactory {
    private final boolean illuminaReads;

    public ReadWrapperFactory(boolean illuminaReads) {
        this.illuminaReads = illuminaReads;
    }

    public ReadWrapper wrap(SequenceRead read) {
        if (read.numberOfReads() == 1) {
            return new SingleReadWrapper(read);
        } else {
            return illuminaReads ? new PairedIlluminaReadWrapper(read) : new PairedReadWrapper(read);
        }
    }
}
