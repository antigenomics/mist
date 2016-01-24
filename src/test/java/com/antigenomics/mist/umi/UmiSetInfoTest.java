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
import cc.redberry.pipe.blocks.ParallelProcessor;
import com.antigenomics.mist.TestUtil;
import com.antigenomics.mist.preprocess.ReadWrapperFactory;
import com.antigenomics.mist.preprocess.SearchProcessor;
import com.antigenomics.mist.primer.PrimerSearcherArray;
import com.antigenomics.mist.primer.PrimerSearcherResult;
import com.milaboratory.core.io.sequence.PairedRead;
import com.milaboratory.core.io.sequence.fastq.PairedFastqReader;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class UmiSetInfoTest {
    @Test
    public void concurrentTest() throws IOException {
        PrimerSearcherArray primerSearcherArray = TestUtil.readBarcodes("umi_barcodes.txt");

        SearchProcessor searchProcessor = new SearchProcessor(
                new ReadWrapperFactory(false), // this is a sample from MIGEC/Checkout, reads already reversed
                primerSearcherArray);

        PairedFastqReader reader = new PairedFastqReader(
                TestUtil.resourceAsStream("umi_sample_R1.fastq.gz"),
                TestUtil.resourceAsStream("umi_sample_R2.fastq.gz")
        );

        ParallelProcessor<PairedRead, PrimerSearcherResult> processor = new ParallelProcessor(
                reader, searchProcessor, Runtime.getRuntime().availableProcessors()
        );

        int matchedReads = 0;

        PrimerSearcherResult result;

        while ((result = processor.take()) != null) {
            if (result.isMatched())
                matchedReads++;
        }

        UmiSetInfo umiSetInfo = searchProcessor.getUmiSetInfo();
        OutputPort<UmiInfo> umiInfoProvider = umiSetInfo.getUmiInfoProvider();

        UmiInfo umiInfo;
        int readsInUmis = 0;
        while ((umiInfo = umiInfoProvider.take()) != null) {
            readsInUmis += umiInfo.getCount();
        }

        Assert.assertTrue(matchedReads > 0);
        Assert.assertEquals(matchedReads, readsInUmis);
    }
}
