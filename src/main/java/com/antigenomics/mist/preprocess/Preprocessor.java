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

import cc.redberry.pipe.InputPort;
import cc.redberry.pipe.OutputPort;
import cc.redberry.pipe.blocks.FilteringPort;
import cc.redberry.pipe.blocks.Merger;
import cc.redberry.pipe.blocks.ParallelProcessor;
import cc.redberry.pipe.util.CountingOutputPort;
import com.antigenomics.mist.misc.Speaker;
import com.antigenomics.mist.primer.PrimerSearcherResult;
import com.milaboratory.core.io.sequence.SequenceRead;
import com.milaboratory.core.io.sequence.SequenceReader;

public class Preprocessor<T extends SequenceRead> implements Runnable {
    private static final int INPUT_BUFFER_SIZE = 1024, PROCESSOR_BUFFER_SIZE = 2048;
    private final int threads;
    private final SequenceReader<T> reader;
    private final InputPort<T> output, unmatchedOutput;
    private final SearchProcessor<T> searchProcessor;
    private final ReadGroomer<T> readGroomer;

    @SuppressWarnings("unchecked")
    public Preprocessor(SequenceReader<T> reader, InputPort<T> output,
                        SearchProcessor<T> searchProcessor, ReadGroomer<T> readGroomer) {
        this(reader, output, searchProcessor, readGroomer, null, Runtime.getRuntime().availableProcessors());
    }

    public Preprocessor(SequenceReader<T> reader, InputPort<T> output,
                        SearchProcessor<T> searchProcessor, ReadGroomer<T> readGroomer,
                        InputPort<T> unmatchedOutput,
                        int threads) {
        this.reader = reader;
        this.output = output;
        this.unmatchedOutput = unmatchedOutput;
        this.searchProcessor = searchProcessor;
        this.readGroomer = readGroomer;
        this.threads = threads;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void run() {
        final Merger<T> bufferedInput = new Merger<>(INPUT_BUFFER_SIZE);
        bufferedInput.merge(reader);
        bufferedInput.start();

        final CountingOutputPort<T> countingInput = new CountingOutputPort<>(bufferedInput);

        Thread reporter = new Thread(new Runnable() {
            long prevCount = -1;

            @Override
            public void run() {
                try {
                    while (!countingInput.isClosed()) {
                        long count = countingInput.getCount();
                        if (prevCount != count) {
                            report();
                            prevCount = count;
                        }
                        Thread.sleep(10000);
                    }
                } catch (InterruptedException e) {
                    report();
                }
            }

            private void report() {
                Speaker.INSTANCE.sout("Loaded " + countingInput.getCount() + " reads: " +
                        searchProcessor.getProcessedReadsCount() + " processed, " +
                        searchProcessor.getMatchedReadsCount() + " matched, " +
                        (int) (100 * searchProcessor.getForwardOrientationRatio()) + "% forward orientation.");
            }
        });

        reporter.setDaemon(true);
        reporter.start();

        final OutputPort<PrimerSearcherResult> searchResults = new ParallelProcessor<>(bufferedInput,
                searchProcessor, PROCESSOR_BUFFER_SIZE, threads);

        final FilteringPort<PrimerSearcherResult> filteredResults = new FilteringPort<>(searchResults,
                new ResultFilter());

        if (unmatchedOutput != null) {
            filteredResults.attachDiscardPort(object -> unmatchedOutput.put((T) object.getReadWrapper().getRead()));
        }

        final OutputPort<T> groomedReads = new ParallelProcessor<>(filteredResults,
                readGroomer, PROCESSOR_BUFFER_SIZE, threads);

        T read;

        while ((read = groomedReads.take()) != null) {
            output.put(read);
        }

        output.put(null);

        reporter.interrupt();
    }
}
