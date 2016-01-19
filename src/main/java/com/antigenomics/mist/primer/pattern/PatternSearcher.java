package com.antigenomics.mist.primer.pattern;

import com.milaboratory.core.sequence.NSequenceWithQuality;

public interface PatternSearcher {
    PatternSearchResult searchFirst(NSequenceWithQuality read);

    PatternSearchResult searchLast(NSequenceWithQuality read);
}
