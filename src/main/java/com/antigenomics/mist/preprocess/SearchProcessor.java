package com.antigenomics.mist.preprocess;

import cc.redberry.pipe.Processor;
import com.antigenomics.mist.primer.PrimerSearcherArray;
import com.antigenomics.mist.primer.PrimerSearcherResult;
import com.milaboratory.core.io.sequence.SequenceRead;

import java.util.concurrent.atomic.AtomicLong;

public class SearchProcessor implements Processor<SequenceRead, PrimerSearcherResult> {
    private final ReadWrapperFactory readWrapperFactory;
    private final PrimerSearcherArray primerSearcherArray;
    private final AtomicLong totalReadsCounter = new AtomicLong(),
            matchedReadsCounter = new AtomicLong(),
            reverseFoundCounter = new AtomicLong();

    public SearchProcessor(ReadWrapperFactory readWrapperFactory, PrimerSearcherArray primerSearcherArray) {
        this.readWrapperFactory = readWrapperFactory;
        this.primerSearcherArray = primerSearcherArray;
    }

    @Override
    public PrimerSearcherResult process(SequenceRead read) {
        PrimerSearcherResult result = primerSearcherArray.search(readWrapperFactory.wrap(read));
        totalReadsCounter.incrementAndGet();
        if (result.isMatched()) {
            matchedReadsCounter.incrementAndGet();
            if (result.isReversed()) {
                reverseFoundCounter.incrementAndGet();
            }
        }
        return result;
    }

    public long getProcessedReadsCount() {
        return totalReadsCounter.get();
    }

    public long getMatchedReadsCount() {
        return matchedReadsCounter.get();
    }

    public long getFoundInReverseCount() {
        return reverseFoundCounter.get();
    }

    public float getForwardOrientationRatio() {
        return 1.0f - getFoundInReverseCount() / (float) getMatchedReadsCount();
    }
}
