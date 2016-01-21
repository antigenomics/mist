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

import com.antigenomics.mist.preprocess.ReadWrapperFactory;
import com.antigenomics.mist.primer.pattern.FuzzyPatternSearcher;
import com.milaboratory.core.io.sequence.PairedRead;
import com.milaboratory.core.io.sequence.SingleReadImpl;
import com.milaboratory.core.sequence.NSequenceWithQuality;
import org.junit.Assert;
import org.junit.Test;

public class PrimerSearcherTest {
    private final NSequenceWithQuality read1 = new NSequenceWithQuality(
            /*
             0000000000111111111122222222223333333333444444444455555555556666
             0123456789012345678901234567890123456789012345678901234567890123 */
            "AGTCGAAAAATCGTAGCTAGGGCGCTAGTCGATCACCGCGGGGAAAACTGCTTTCAGATCGACT",
            "IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII"
    ), read2 = new NSequenceWithQuality(
            /*
             0000000000111111111122222222223333333333444444444455555555556666
             0123456789012345678901234567890123456789012345678901234567890123 */
            "ACGATCGACTGACTAGACTGCTACGCCAAAATGCAAGGGATGCCGCATTGCATATAAGCTATAT",
            "IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII"
    ).getReverseComplement();

    @Test
    public void singleReadTestForward() {
        FuzzyPatternSearcher fpsLeft = new FuzzyPatternSearcher("GTCGAAAAATNNNNNN", 0),
                fpsRight = new FuzzyPatternSearcher("NNNNNNCTTTCAGATC", 0);

        PrimerSearcher primerSearcher = new PrimerSearcher("dummy", fpsLeft, fpsRight, true);

        ReadWrapperFactory readWrapperFactory = new ReadWrapperFactory(true);

        PrimerSearcherResult result = primerSearcher.search(readWrapperFactory.wrap(
                new SingleReadImpl(0, read1, "")));

        Assert.assertTrue(result.isMatched());
        Assert.assertTrue(!result.isReversed());
        Assert.assertEquals("CGTAGC", result.getLeftResult().getUmi().getSequence().toString());
        Assert.assertEquals("AAACTG", result.getRightResult().getUmi().getSequence().toString());
    }

    @Test
    public void singleReadTestReverse() {
        FuzzyPatternSearcher fpsLeft = new FuzzyPatternSearcher("GTCGAAAAATNNNNNN", 0),
                fpsRight = new FuzzyPatternSearcher("NNNNNNCTTTCAGATC", 0);

        PrimerSearcher primerSearcher = new PrimerSearcher("dummy", fpsLeft, fpsRight, true);

        ReadWrapperFactory readWrapperFactory = new ReadWrapperFactory(true);

        PrimerSearcherResult result = primerSearcher.search(readWrapperFactory.wrap(
                new SingleReadImpl(0, read1.getReverseComplement(), "")));

        Assert.assertTrue(result.isMatched());
        Assert.assertTrue(result.isReversed());
        Assert.assertEquals("CGTAGC", result.getLeftResult().getUmi().getSequence().toString());
        Assert.assertEquals("AAACTG", result.getRightResult().getUmi().getSequence().toString());
    }

    @Test
    public void pairedReadTestForward() {
        FuzzyPatternSearcher fpsLeft = new FuzzyPatternSearcher("GTCGAAAAATNNNNNN", 0),
                fpsRight = new FuzzyPatternSearcher("NNNNNNTATAAGCTA", 0);

        PrimerSearcher primerSearcher = new PrimerSearcher("dummy", fpsLeft, fpsRight, true);

        ReadWrapperFactory readWrapperFactory = new ReadWrapperFactory(true);

        PrimerSearcherResult result = primerSearcher.search(readWrapperFactory.wrap(
                new PairedRead(new SingleReadImpl(0, read1, ""),
                        new SingleReadImpl(0, read2, ""))
        ));

        Assert.assertTrue(result.isMatched());
        Assert.assertTrue(!result.isReversed());
        Assert.assertEquals("CGTAGC", result.getLeftResult().getUmi().getSequence().toString());
        Assert.assertEquals("ATTGCA", result.getRightResult().getUmi().getSequence().toString());
    }

    @Test
    public void pairedReadTestReverse() {
        FuzzyPatternSearcher fpsLeft = new FuzzyPatternSearcher("GTCGAAAAATNNNNNN", 0),
                fpsRight = new FuzzyPatternSearcher("NNNNNNTATAAGCTA", 0);

        PrimerSearcher primerSearcher = new PrimerSearcher("dummy", fpsLeft, fpsRight, true);

        ReadWrapperFactory readWrapperFactory = new ReadWrapperFactory(true);
        
        PrimerSearcherResult result = primerSearcher.search(readWrapperFactory.wrap(
                new PairedRead(new SingleReadImpl(0, read2, ""),
                        new SingleReadImpl(0, read1, ""))
        ));

        Assert.assertTrue(result.isMatched());
        Assert.assertTrue(result.isReversed());
        Assert.assertEquals("CGTAGC", result.getLeftResult().getUmi().getSequence().toString());
        Assert.assertEquals("ATTGCA", result.getRightResult().getUmi().getSequence().toString());
    }
}
