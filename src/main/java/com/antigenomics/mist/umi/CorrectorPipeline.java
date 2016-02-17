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

package com.antigenomics.mist.umi;

import cc.redberry.pipe.OutputPort;
import cc.redberry.pipe.blocks.FilteringPort;
import cc.redberry.pipe.blocks.ParallelProcessor;
import com.antigenomics.mist.PipelineBlock;
import com.antigenomics.mist.misc.HeaderUtil;
import com.milaboratory.core.io.sequence.SequenceRead;

public class CorrectorPipeline<T extends SequenceRead> extends PipelineBlock<T> {
    private final UmiCorrector<T> umiCorrector;

    public CorrectorPipeline(UmiCorrector<T> umiCorrector) {
        this.umiCorrector = umiCorrector;
    }

    @Override
    protected OutputPort<T> prepareProcessor() {
        OutputPort<T> correctorResults = new ParallelProcessor<>(getInput(),
                umiCorrector, PipelineBlock.PROCESSOR_BUFFER_SIZE, numberOfThreads);

        FilteringPort<T> filteredReads = new FilteringPort<>(correctorResults,
                read -> read.getRead(0).getDescription().contains(HeaderUtil.LOW_COVERAGE_TAG));

        filteredReads.attachDiscardPort(getDiscarded());

        return filteredReads;
    }

    @Override
    protected void reportProgress() {
        // TODO
    }
}
