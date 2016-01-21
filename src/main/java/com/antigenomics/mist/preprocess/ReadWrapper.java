package com.antigenomics.mist.preprocess;

import com.milaboratory.core.io.sequence.SequenceRead;
import com.milaboratory.core.sequence.NSequenceWithQuality;

public interface ReadWrapper {
    SequenceRead getRead();

    NSequenceWithQuality getData(int index, boolean reverse);
}
