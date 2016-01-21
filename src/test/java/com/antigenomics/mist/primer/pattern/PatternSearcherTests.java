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

package com.antigenomics.mist.primer.pattern;

import com.milaboratory.core.sequence.NSequenceWithQuality;
import org.junit.Assert;
import org.junit.Test;

public class PatternSearcherTests {
    @Test
    public void dummySearcherTest() {
        NSequenceWithQuality nsq = new NSequenceWithQuality("", "");

        DummyPatternSearcher dummyPatternSearcher = new DummyPatternSearcher();

        Assert.assertTrue(dummyPatternSearcher.searchFirst(nsq).isMatching());
        Assert.assertTrue(dummyPatternSearcher.searchLast(nsq).isMatching());
    }

    @Test
    public void strictPatternSearcherTestOutside() {
        NSequenceWithQuality nsq = new NSequenceWithQuality(
                /*    000000000011111111112222222222333333
                      012345678901234567890123456789012345
                  */ "ATGATGAACGCCAGTCGATCGATCGAATGAACGTCG",
                /**/ "IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII");

        StrictPatternMatcher strictPatternMatcher = new StrictPatternMatcher(0, 4, "ATGAACG");

        PatternSearchResult result = strictPatternMatcher.searchFirst(nsq);

        Assert.assertTrue(result.isMatching());
        Assert.assertEquals(3, result.getFrom());
        Assert.assertEquals(10, result.getTo());

        result = strictPatternMatcher.searchLast(nsq);

        Assert.assertTrue(result.isMatching());
        Assert.assertEquals(26, result.getFrom());
        Assert.assertEquals(33, result.getTo());
    }

    @Test
    public void strictPatternSearcherTestNegative() {
        NSequenceWithQuality nsq = new NSequenceWithQuality(
                /*    000000000011111111112222222222333333
                      012345678901234567890123456789012345
                  */ "ATGAATGCAACGCCAGTCGATCGATCGAATGAAACA",
                /**/ "IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII");

        StrictPatternMatcher strictPatternMatcher = new StrictPatternMatcher(-4, 4, "ATGAACG");

        PatternSearchResult result = strictPatternMatcher.searchFirst(nsq);

        Assert.assertTrue(!result.isMatching());

        result = strictPatternMatcher.searchLast(nsq);

        Assert.assertTrue(!result.isMatching());
    }

    @Test
    public void strictPatternSearcherTestInside() {
        NSequenceWithQuality nsq = new NSequenceWithQuality(
                /*    0000000000111111111122222222
                      0123456789012345678901234567
                  */ "TGAACGCCAGTCGATCGATCGAATGAAC",
                /**/ "IIIIIIIIIIIIIIIIIIIIIIIIIIII");

        StrictPatternMatcher strictPatternMatcher = new StrictPatternMatcher(-4, 0, "ATGAACG");

        PatternSearchResult result = strictPatternMatcher.searchFirst(nsq);

        Assert.assertTrue(result.isMatching());
        Assert.assertEquals(0, result.getFrom());
        Assert.assertEquals(6, result.getTo());

        result = strictPatternMatcher.searchLast(nsq);

        Assert.assertTrue(result.isMatching());
        Assert.assertEquals(22, result.getFrom());
        Assert.assertEquals(28, result.getTo());
    }

    @Test
    public void strictPatternSearcherTestWildcard() {
        NSequenceWithQuality nsq = new NSequenceWithQuality(
                /*    000000000011111111112222222222333333
                      012345678901234567890123456789012345
                  */ "ATGATGAACGCCAGTCGATCGATCGAATGAACGTCG",
                /**/ "IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII");

        StrictPatternMatcher strictPatternMatcher = new StrictPatternMatcher(0, 4, "RWGRACG");

        PatternSearchResult result = strictPatternMatcher.searchFirst(nsq);

        Assert.assertTrue(result.isMatching());
        Assert.assertEquals(3, result.getFrom());
        Assert.assertEquals(10, result.getTo());
    }

    @Test
    public void strictPatternSearcherTestNNNOutside() {
        NSequenceWithQuality nsq = new NSequenceWithQuality(
                /*    000000000011111111112222222222333333
                      012345678901234567890123456789012345
                  */ "ATGATGAACGCCAGTCGATCGATCGAATGAACGTCG",
                /**/ "IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII");

        StrictPatternMatcher strictPatternMatcher = new StrictPatternMatcher(0, 4, "NNATGAACGNN");

        PatternSearchResult result = strictPatternMatcher.searchFirst(nsq);

        Assert.assertTrue(result.isMatching());
        Assert.assertEquals("TGCC", result.getUmi().getSequence().toString());
        Assert.assertEquals(1, result.getFrom());
        Assert.assertEquals(12, result.getTo());
    }

    @Test
    public void strictPatternSearcherTestNNNInside() {
        NSequenceWithQuality nsq = new NSequenceWithQuality(
                /*    00000000001111111111222222222
                      01234567890123456789012345678
                  */ "TGAACGCCAGTCGATCGATCGAATGAACG",
                /**/ "IIIIIIIIIIIIIIIIIIIIIIIIIIIII");

        StrictPatternMatcher strictPatternMatcher = new StrictPatternMatcher(-4, 0, "NNATGAACGNN");

        PatternSearchResult result = strictPatternMatcher.searchFirst(nsq);

        Assert.assertTrue(result.isMatching());
        Assert.assertEquals("NNCC", result.getUmi().getSequence().toString());
        Assert.assertEquals(0, result.getFrom());
        Assert.assertEquals(8, result.getTo());

        result = strictPatternMatcher.searchLast(nsq);

        Assert.assertTrue(result.isMatching());
        Assert.assertEquals("GANN", result.getUmi().getSequence().toString());
        Assert.assertEquals(20, result.getFrom());
        Assert.assertEquals(29, result.getTo());
    }

    @Test
    public void fuzzyPatternSearcherTest() {
        NSequenceWithQuality nsq = new NSequenceWithQuality(
                /*    0000000000111111111122222222223333333333444444
                      0123456789012345678901234567890123456789012345
                  */ "ACTCGACAGTCGATCGAAATCCCGATTATAAATGCATCACATCCCC",
                /**/ "IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII");

        FuzzyPatternSearcher fuzzyPatternSearcher = new FuzzyPatternSearcher("GACAGTCGA", 3);

        Assert.assertTrue(fuzzyPatternSearcher.searchFirst(nsq).isMatching());

        fuzzyPatternSearcher = new FuzzyPatternSearcher("CACAGTCGA", 3);

        Assert.assertTrue(!fuzzyPatternSearcher.searchFirst(nsq).isMatching());

        fuzzyPatternSearcher = new FuzzyPatternSearcher("gAgAGTCGA", 3);

        Assert.assertTrue(fuzzyPatternSearcher.searchFirst(nsq).isMatching());
    }

    @Test
    public void fuzzyPatternSearcherTestBorderCases() {
        NSequenceWithQuality nsq = new NSequenceWithQuality(
                /*    0000000000111111111122222222223333333333444444
                      0123456789012345678901234567890123456789012345
                  */ "ACTCGACAGTCGATCGAAATCCCGATTATAAATGCATCACATCCCC",
                /**/ "IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII");

        FuzzyPatternSearcher fuzzyPatternSearcher = new FuzzyPatternSearcher("AACTCGACA", 3);

        Assert.assertTrue(!fuzzyPatternSearcher.searchFirst(nsq).isMatching());

        fuzzyPatternSearcher = new FuzzyPatternSearcher("aACTCGACA", 3);

        Assert.assertTrue(!fuzzyPatternSearcher.searchFirst(nsq).isMatching());

        fuzzyPatternSearcher = new FuzzyPatternSearcher("aTaGACAGTCGA", 3);

        PatternSearchResult result = fuzzyPatternSearcher.searchFirst(nsq);

        Assert.assertTrue(result.isMatching());
        Assert.assertEquals(1, result.getFrom());
        Assert.assertEquals(13, result.getTo());

        result = fuzzyPatternSearcher.searchLast(nsq);

        Assert.assertTrue(result.isMatching());
        Assert.assertEquals(1, result.getFrom());
        Assert.assertEquals(13, result.getTo());
    }

    @Test
    public void fuzzyPatternSearcherTestUmi() {
        NSequenceWithQuality nsq = new NSequenceWithQuality(
                /*    0000000000111111111122222222223333333333444444
                      0123456789012345678901234567890123456789012345
                  */ "ACTCGACAGTCGATCGAAATCCCGATTATAAATGCATCACATCCCC",
                /**/ "IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII");

        FuzzyPatternSearcher fuzzyPatternSearcher = new FuzzyPatternSearcher("AtcCCNNNNNTAcA", 3);

        PatternSearchResult result = fuzzyPatternSearcher.searchFirst(nsq);

        Assert.assertTrue(result.isMatching());
        Assert.assertEquals(18, result.getFrom());
        Assert.assertEquals(32, result.getTo());
        Assert.assertEquals("GATTA", result.getUmi().getSequence().toString());
    }

    @Test
    public void positionalUmiExtractorTest() {
        NSequenceWithQuality nsq = new NSequenceWithQuality(
                /*    0000000000111111111122222222223333333333444444444455555555
                      0123456789012345678901234567890123456789012345678901234567
                      
                      5555555544444444443333333333222222222211111111110000000000
                      7654321098765432109876543210987654321098765432109876543210                                                         
                  */ "ACTCGACAGTCGAAAATCGAAATCCACGATCCGATTGGGATAAATGCATCACATCCCC",
                /**/ "IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII");

        PositionalUmiExtractor positionalUmiExtractor = new PositionalUmiExtractor(1, 5);

        PatternSearchResult result = positionalUmiExtractor.searchFirst(nsq);

        Assert.assertTrue(result.isMatching());
        Assert.assertEquals(1, result.getFrom());
        Assert.assertEquals(5, result.getTo());
        Assert.assertEquals("CTCG", result.getUmi().getSequence().toString());

        result = positionalUmiExtractor.searchLast(nsq);

        Assert.assertTrue(result.isMatching());
        Assert.assertEquals(53, result.getFrom());
        Assert.assertEquals(57, result.getTo());
        Assert.assertEquals("TCCC", result.getUmi().getSequence().toString());
    }
}
