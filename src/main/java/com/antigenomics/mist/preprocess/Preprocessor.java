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
import cc.redberry.pipe.blocks.Merger;
import cc.redberry.pipe.blocks.ParallelProcessor;
import cc.redberry.pipe.util.CountingOutputPort;
import com.antigenomics.mist.misc.Speaker;
import com.antigenomics.mist.primer.PrimerSearcherResult;
import com.antigenomics.mist.umi.UmiSetInfo;
import com.milaboratory.core.io.sequence.SequenceRead;
import com.milaboratory.core.io.sequence.SequenceReader;
import com.milaboratory.core.io.sequence.SequenceWriter;
import com.milaboratory.core.io.sequence.fastq.PairedFastqReader;
import com.milaboratory.core.io.sequence.fastq.PairedFastqWriter;
import com.milaboratory.core.io.sequence.fastq.SingleFastqReader;
import com.milaboratory.core.io.sequence.fastq.SingleFastqWriter;

public class Preprocessor implements Runnable {
    private static final int INPUT_BUFFER_SIZE = 1024, PROCESSOR_BUFFER_SIZE = 2048;
    private final int threads;
    private final SequenceReader reader;
    private final SequenceWriter writer, unmatchedWriter;
    private final SearchProcessor searchProcessor;
    private final ReadGroomer readGroomer;

    public Preprocessor(SingleFastqReader reader, SingleFastqWriter writer,
                        SingleFastqWriter unmatchedWriter,
                        SearchProcessor searchProcessor, ReadGroomer readGroomer,
                        int threads) {
        this.reader = reader;
        this.writer = writer;
        this.unmatchedWriter = unmatchedWriter;
        this.searchProcessor = searchProcessor;
        this.readGroomer = readGroomer;
        this.threads = threads;
    }

    public Preprocessor(PairedFastqReader reader, PairedFastqWriter writer,
                        PairedFastqWriter unmatchedWriter,
                        SearchProcessor searchProcessor, ReadGroomer readGroomer,
                        int threads) {
        this.reader = reader;
        this.writer = writer;
        this.unmatchedWriter = unmatchedWriter;
        this.searchProcessor = searchProcessor;
        this.readGroomer = readGroomer;
        this.threads = threads;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void run() {
        final Merger<SequenceRead> bufferedInput = new Merger<>(INPUT_BUFFER_SIZE);
        bufferedInput.merge(reader);
        bufferedInput.start();

        final CountingOutputPort<SequenceRead> countingInput = new CountingOutputPort<>(bufferedInput);

        Thread reporter = new Thread(new Runnable() {
            long prevCount = -1;

            @Override
            public void run() {
                try {
                    while (!countingInput.isClosed()) {
                        long count = countingInput.getCount();
                        if (prevCount != count) {
                            Speaker.INSTANCE.sout("Loaded " + countingInput.getCount() + " reads: " +
                                    searchProcessor.getProcessedReadsCount() + " processed, " +
                                    searchProcessor.getMatchedReadsCount() + " matched, " +
                                    (int) (100 * searchProcessor.getForwardOrientationRatio()) + "% forward orientation.");
                            prevCount = count;
                        }
                        Thread.sleep(10000);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        reporter.setDaemon(true);
        reporter.start();

        final OutputPort<PrimerSearcherResult> searchResults = new ParallelProcessor<>(bufferedInput,
                searchProcessor, PROCESSOR_BUFFER_SIZE, threads);

        final FilteringPort<PrimerSearcherResult> filteredResults = new FilteringPort<>(searchResults,
                new ResultFilter());

        if (unmatchedWriter != null) {
            filteredResults.attachDiscardPort(object -> unmatchedWriter.write(object.getReadWrapper().getRead()));
        }

        final OutputPort<SequenceRead> groomedReads = new ParallelProcessor<>(filteredResults,
                readGroomer, PROCESSOR_BUFFER_SIZE, threads);

        SequenceRead read;

        while ((read = groomedReads.take()) != null) {
            writer.write(read);
        }

        writer.close();

        if (unmatchedWriter != null) {
            unmatchedWriter.close();
        }

        reporter.interrupt();
    }
}
