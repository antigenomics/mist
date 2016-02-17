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

import cc.redberry.pipe.OutputPort;
import cc.redberry.pipe.blocks.FilteringPort;
import cc.redberry.pipe.blocks.ParallelProcessor;
import com.antigenomics.mist.PipelineBlock;
import com.antigenomics.mist.misc.Speaker;
import com.antigenomics.mist.primer.PrimerSearcherResult;
import com.milaboratory.core.io.sequence.SequenceRead;

public class PreprocessorPipeline<S extends SequenceRead> extends PipelineBlock<S> {
    private final SearchProcessor<S> searchProcessor;
    private final ReadGroomer<S> readGroomer;

    public PreprocessorPipeline(SearchProcessor<S> searchProcessor, ReadGroomer<S> readGroomer) {
        this.searchProcessor = searchProcessor;
        this.readGroomer = readGroomer;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected OutputPort<S> prepareProcessor() {
        final OutputPort<PrimerSearcherResult> searchResults = new ParallelProcessor<>(getInput(),
                searchProcessor, PipelineBlock.PROCESSOR_BUFFER_SIZE, numberOfThreads);

        final FilteringPort<PrimerSearcherResult> filteredResults = new FilteringPort<>(searchResults,
                new ResultFilter());

        filteredResults.attachDiscardPort(object -> getDiscarded().put((S) object.getReadWrapper().getRead()));

        return new ParallelProcessor<>(filteredResults,
                readGroomer, PipelineBlock.PROCESSOR_BUFFER_SIZE, numberOfThreads);
    }

    @Override
    protected void reportProgress() {
        Speaker.INSTANCE.sout("Processed " +
                searchProcessor.getProcessedReadsCount() + " reads, of them " +
                searchProcessor.getMatchedReadsCount() + " matched, " +
                (int) (100 * searchProcessor.getForwardOrientationRatio()) + "% forward orientation.");
    }
}
