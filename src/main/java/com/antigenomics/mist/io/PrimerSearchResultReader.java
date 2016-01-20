package com.antigenomics.mist.io;

import cc.redberry.pipe.OutputPort;
import com.antigenomics.mist.primer.CompositePrimerSearcherResult;
import com.antigenomics.mist.primer.pattern.PatternSearchResult;
import com.milaboratory.core.sequence.NSequenceWithQuality;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class PrimerSearchResultReader implements OutputPort<CompositePrimerSearcherResult> {
    private final BufferedReader reader;

    public PrimerSearchResultReader(InputStream is) {
        this.reader = new BufferedReader(new InputStreamReader(is));

        try {
            String header = reader.readLine();
            if (!header.equals(PrimerSearchResultWriter.HEADER)) {
                throw new IOException("Wrong primer search result table header");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public CompositePrimerSearcherResult take() {
        String line;
        try {
            line = reader.readLine();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (line == null) {
            return null;
        }

        String[] splitLine = line.split("\t");

        /*
        0 read_id
        1 primer_id
        2 left_umi
        3 right_umi
        4 reversed
        5 left_score
        6 left_umi_qual
        7 left_primer_from
        8 left_primer_to
        9 right_score
        10 right_umi_qual
        11 right_primer_from
        12 right_primer_to
         */

        return new CompositePrimerSearcherResult(
                new PatternSearchResult(
                        Integer.parseInt(splitLine[7]), Integer.parseInt(splitLine[8]),
                        new NSequenceWithQuality(splitLine[2], splitLine[6]),
                        Byte.parseByte(splitLine[5])),
                new PatternSearchResult(
                        Integer.parseInt(splitLine[11]), Integer.parseInt(splitLine[12]),
                        new NSequenceWithQuality(splitLine[3], splitLine[10]),
                        Byte.parseByte(splitLine[9])),
                splitLine[1], Long.parseLong(splitLine[0]), Boolean.parseBoolean(splitLine[4])
        );
    }
}
