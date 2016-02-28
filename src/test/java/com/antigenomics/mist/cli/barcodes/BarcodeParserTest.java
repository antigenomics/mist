package com.antigenomics.mist.cli.barcodes;

import com.antigenomics.mist.primer.PrimerSearcher;
import com.antigenomics.mist.primer.PrimerSearcherArray;
import com.antigenomics.mist.primer.pattern.DummyPatternSearcher;
import com.antigenomics.mist.primer.pattern.FuzzyPatternSearcher;
import com.antigenomics.mist.primer.pattern.PositionalUmiExtractor;
import com.antigenomics.mist.primer.pattern.StrictPatternMatcher;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;

public class BarcodeParserTest {
    @Test
    public void test1() throws IOException {
        PrimerSearcherArray primerSearcherArray = new PrimerSearcherArray(
                Arrays.asList(
                        new PrimerSearcher("fuzzy",
                                new FuzzyPatternSearcher("ATGC", 1), new DummyPatternSearcher(), false),
                        new PrimerSearcher("strict",
                                new StrictPatternMatcher(-1, 1, "ATGC"), new StrictPatternMatcher(-1, 1, "ATGC"), false),
                        new PrimerSearcher("positional",
                                new DummyPatternSearcher(), new PositionalUmiExtractor(0, 10), false)
                )
        );

        String json = BarcodesParser.write(primerSearcherArray);

        System.out.println(json);

        PrimerSearcherArray recovered = BarcodesParser.read(json);

        for (int i = 0; i < primerSearcherArray.getPrimerSearchers().size(); i++) {
            Assert.assertEquals(primerSearcherArray.getPrimerSearchers().get(i),
                    recovered.getPrimerSearchers().get(i));
        }
    }
}
