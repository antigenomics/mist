/*
 * Copyright 2015 Mikhail Shugay (mikhail.shugay@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.antigenomics.mist.preprocess;

import cc.redberry.pipe.Processor;
import com.antigenomics.mist.primer.PrimerSearcherArray;
import com.antigenomics.mist.primer.PrimerSearcherResult;
import com.antigenomics.mist.umi.UmiAccumulator;
import com.milaboratory.core.io.sequence.SequenceRead;

import java.util.concurrent.atomic.AtomicLong;

public class SearchProcessor<T extends SequenceRead> implements Processor<T, PrimerSearcherResult> {
    private final ReadWrapperFactory readWrapperFactory;
    private final PrimerSearcherArray primerSearcherArray;
    private final AtomicLong totalReadsCounter = new AtomicLong(),
            matchedReadsCounter = new AtomicLong(),
            reverseFoundCounter = new AtomicLong();
    private final UmiAccumulator umiAccumulator;

    public SearchProcessor(ReadWrapperFactory readWrapperFactory,
                           PrimerSearcherArray primerSearcherArray) {
        this.readWrapperFactory = readWrapperFactory;
        this.primerSearcherArray = primerSearcherArray;
        this.umiAccumulator = new UmiAccumulator();
    }

    @Override
    public PrimerSearcherResult process(T read) {
        PrimerSearcherResult result = primerSearcherArray.search(readWrapperFactory.wrap(read));
        totalReadsCounter.incrementAndGet();
        if (result.isMatched()) {
            matchedReadsCounter.incrementAndGet();

            if (result.getLeftResult().getUmi().size() +
                    result.getRightResult().getUmi().size() > 0) {
                umiAccumulator.update(result.getPrimerId(),
                        result.getLeftResult().getUmi(),
                        result.getRightResult().getUmi());
            }

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

    public UmiAccumulator getUmiAccumulator() {
        return umiAccumulator;
    }
}
