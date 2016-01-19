package com.antigenomics.mist.primer;

import com.milaboratory.core.io.sequence.SequenceRead;
import com.milaboratory.core.sequence.NSequenceWithQuality;

public interface ReadWrapper {
    SequenceRead getRead();

    NSequenceWithQuality getData(int index, boolean reverse);
}
