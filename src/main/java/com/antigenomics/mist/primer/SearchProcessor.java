package com.antigenomics.mist.primer;

import cc.redberry.pipe.Processor;
import com.milaboratory.core.io.sequence.SequenceRead;

public class SearchProcessor implements Processor<SequenceRead, CompositePrimerSearcherResult> {
    private final ReadWrapperFactory readWrapperFactory;
    private final SearcherArray searcherArray;

    public SearchProcessor(ReadWrapperFactory readWrapperFactory, SearcherArray searcherArray) {
        this.readWrapperFactory = readWrapperFactory;
        this.searcherArray = searcherArray;
    }

    @Override
    public CompositePrimerSearcherResult process(SequenceRead read) {
        return searcherArray.search(readWrapperFactory.wrap(read));
    }
}
