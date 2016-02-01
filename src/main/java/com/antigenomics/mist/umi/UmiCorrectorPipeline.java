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

import cc.redberry.pipe.InputPort;
import cc.redberry.pipe.OutputPort;
import cc.redberry.pipe.blocks.FilteringPort;
import cc.redberry.pipe.blocks.Merger;
import cc.redberry.pipe.blocks.ParallelProcessor;
import cc.redberry.pipe.util.CountingOutputPort;
import cc.redberry.primitives.Filter;
import com.antigenomics.mist.misc.HeaderUtil;
import com.antigenomics.mist.misc.Reporter;
import com.antigenomics.mist.misc.Speaker;
import com.milaboratory.core.io.sequence.SequenceRead;
import com.milaboratory.core.io.sequence.SequenceReader;

public class UmiCorrectorPipeline<T extends SequenceRead> {
    private static final int INPUT_BUFFER_SIZE = 1024, PROCESSOR_BUFFER_SIZE = 2048;
    private final UmiCorrector<T> umiCorrector;
    private final int umiCoverageThreshold, threads;
    private final InputPort<T> output, unmatchedOutput;
    private final SequenceReader<T> reader;

    public UmiCorrectorPipeline(SequenceReader<T> reader, InputPort<T> output,
                                InputPort<T> unmatchedOutput, UmiCorrector<T> umiCorrector,
                                int umiCoverageThreshold, int threads) {
        this.umiCorrector = umiCorrector;
        this.reader = reader;
        this.output = output;
        this.unmatchedOutput = unmatchedOutput;
        this.umiCoverageThreshold = umiCoverageThreshold;
        this.threads = threads;
    }

    public void run() {
        final FilteringPort<T> filteredReads = new FilteringPort<>(reader,
                object -> {
                    UmiTag umiTag = HeaderUtil.parsedHeader(object).toUmiTag();
                    return umiCorrector.get(umiTag).getCoverage() >= umiCoverageThreshold;
                });

        if (unmatchedOutput != null) {
            filteredReads.attachDiscardPort(unmatchedOutput);
        }

        final Merger<T> bufferedInput = new Merger<>(INPUT_BUFFER_SIZE);

        bufferedInput.merge(filteredReads);
        bufferedInput.start();

        final CountingOutputPort<T> countingInput = new CountingOutputPort<>(bufferedInput);

        Thread reporter = new Thread(new Reporter(countingInput) {
            @Override
            protected void report() {
                Speaker.INSTANCE.sout("Loaded " + countingInput.getCount() + " reads: " +
                        filteredReads.getTotalCount() + " processed, " +
                        filteredReads.getRejectedCount() + " filtered, " +
                        umiCorrector.getCorrectedCount() + "corrected.");
            }
        });

        reporter.setDaemon(true);
        reporter.start();

        final OutputPort<T> correctorResults = new ParallelProcessor<>(bufferedInput,
                umiCorrector, PROCESSOR_BUFFER_SIZE, threads);

        T read;

        while ((read = correctorResults.take()) != null) {
            output.put(read);
        }

        output.put(null);

        reporter.interrupt();
    }
}
