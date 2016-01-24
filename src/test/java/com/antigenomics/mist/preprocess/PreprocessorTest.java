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
import com.antigenomics.mist.TestUtil;
import com.antigenomics.mist.primer.PrimerSearcherArray;
import com.milaboratory.core.io.sequence.fastq.PairedFastqReader;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

public class PreprocessorTest {
    @SuppressWarnings("unchecked")
    @Test
    public void readDataTest() throws IOException {
        PrimerSearcherArray primerSearcherArray = TestUtil.readBarcodes("umi_barcodes.txt");

        SearchProcessor searchProcessor = new SearchProcessor(
                new ReadWrapperFactory(false), // this is a sample from MIGEC/Checkout, reads already reversed
                primerSearcherArray);

        PairedFastqReader reader = new PairedFastqReader(
                TestUtil.resourceAsStream("umi_sample_R1.fastq.gz"),
                TestUtil.resourceAsStream("umi_sample_R2.fastq.gz")
        );

        int totalReads = 0;

        while (reader.take() != null) {
            totalReads++;
        }

        reader = new PairedFastqReader(
                TestUtil.resourceAsStream("umi_sample_R1.fastq.gz"),
                TestUtil.resourceAsStream("umi_sample_R2.fastq.gz")
        );

        CountingInputPort matchedReads = new CountingInputPort();

        Preprocessor preprocessor = new Preprocessor(reader,
                matchedReads, searchProcessor, new PairedReadGroomer(true));

        preprocessor.run();

        Assert.assertTrue(totalReads > 0);
        Assert.assertEquals(totalReads, searchProcessor.getProcessedReadsCount());
        Assert.assertTrue(matchedReads.getCount() > 0);
        Assert.assertEquals(matchedReads.getCount(), searchProcessor.getMatchedReadsCount());
        Assert.assertTrue(matchedReads.getCount() / (double) totalReads > 0.99);
    }


    /*
     * TODO: awaiting fix (add getter to counter) from Dima Bolotin 
     */
    private final class CountingInputPort<T> implements InputPort<T> {
        private final AtomicInteger counter = new AtomicInteger();
        private final InputPort<T> inherited;

        public CountingInputPort() {
            this.inherited = null;
        }

        public CountingInputPort(InputPort<T> inherited) {
            this.inherited = inherited;
        }

        @Override
        public void put(T object) {
            if (object != null)
                counter.incrementAndGet();
            if (inherited != null)
                inherited.put(object);
        }

        public long getCount() {
            return counter.get();
        }
    }
}
