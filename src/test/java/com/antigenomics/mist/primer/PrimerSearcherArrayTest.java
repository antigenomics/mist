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

package com.antigenomics.mist.primer;

import com.antigenomics.mist.TestUtil;
import com.antigenomics.mist.preprocess.ReadWrapperFactory;
import com.antigenomics.mist.primer.pattern.FuzzyPatternSearcher;
import com.milaboratory.core.io.sequence.PairedRead;
import com.milaboratory.core.io.sequence.fastq.PairedFastqReader;
import org.junit.Assert;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class PrimerSearcherArrayTest {
    @Test
    public void realDataTest() throws IOException {
        BufferedReader barcodesReader = new BufferedReader(new InputStreamReader(TestUtil.resourceAsStream("barcodes.txt")));

        String line;

        List<PrimerSearcher> primerSearchers = new ArrayList<>();

        while ((line = barcodesReader.readLine()) != null) {
            String[] splitLine = line.split("\t");
            primerSearchers.add(new PrimerSearcher(splitLine[0],
                    new FuzzyPatternSearcher(splitLine[1], 3),
                    new FuzzyPatternSearcher(splitLine[2], 3),
                    true));
        }

        PrimerSearcherArray primerSearcherArray = new PrimerSearcherArray(primerSearchers);

        PairedFastqReader reader = new PairedFastqReader(
                TestUtil.resourceAsStream("sample_R1.fastq.gz"),
                TestUtil.resourceAsStream("sample_R2.fastq.gz")
        );

        PairedRead read;
        ReadWrapperFactory readWrapperFactory = new ReadWrapperFactory(true);

        int total = 0;
        while ((read = reader.take()) != null) {
            total++;
            //try {
                primerSearcherArray.search(readWrapperFactory.wrap(read));
            //} catch (Exception e) {
            //    System.out.println(read.getR1().getData().getSequence());
            //    System.out.println(read.getR2().getData().getSequence());
            //}
        }

        List<PrimerSearcherStats> stats = primerSearcherArray.getStats();

        int minCount = (int) (0.4 * total / (float) primerSearchers.size());
        int totalMatch = 0;

        for (PrimerSearcherStats stat : stats) {
            totalMatch += stat.getBothMatchCount();
            Assert.assertTrue(stat.getBothMatchCount() > minCount);
            Assert.assertTrue(stat.getPartialMatchRatio() < 0.2);
        }

        Assert.assertTrue(totalMatch > 0.8 * total);
    }
}
