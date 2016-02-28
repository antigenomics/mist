package com.antigenomics.mist;

import cc.redberry.pipe.InputPort;
import com.milaboratory.core.io.sequence.SequenceRead;
import com.milaboratory.core.io.sequence.SequenceWriter;

public class SequenceWriterWrapper<S extends SequenceRead> implements InputPort<S> {
    private final SequenceWriter<S> writer;

    public SequenceWriterWrapper(SequenceWriter<S> writer) {
        this.writer = writer;
    }

    @Override
    public void put(S read) {
        if (read == null) {
            writer.close();
        } else {
            writer.write(read);
        }
    }
}
