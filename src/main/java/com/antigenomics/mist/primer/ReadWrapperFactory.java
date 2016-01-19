package com.antigenomics.mist.primer;

import com.milaboratory.core.io.sequence.SequenceRead;

public interface ReadWrapperFactory {
    ReadWrapper wrap(SequenceRead sequenceRead);
}
