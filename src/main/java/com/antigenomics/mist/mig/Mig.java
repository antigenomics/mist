package com.antigenomics.mist.mig;

import com.milaboratory.core.io.sequence.SequenceRead;

import java.util.List;

public class Mig {
    private final Tag tag;
    private final List<SequenceRead> reads;

    public Mig(Tag tag, List<SequenceRead> reads) {
        this.tag = tag;
        this.reads = reads;
    }

    public Tag getTag() {
        return tag;
    }
}
