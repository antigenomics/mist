package com.antigenomics.mist.io;

import cc.redberry.pipe.InputPort;
import com.antigenomics.mist.primer.CompositePrimerSearcherResult;

import java.io.OutputStream;
import java.io.PrintWriter;

public class PrimerSearchResultWriter implements InputPort<CompositePrimerSearcherResult> {
    public static final String HEADER = "read_id\tprimer_id\tleft_umi\tright_umi\t" +
            "reversed\tleft_score\tleft_umi_qual\tright_umi_qual\t" +
            "left_primer_from\tleft_primer_to\tright_primer_from\tright_primer_to";
    /*
    0 read_id
    1 primer_id
    2 left_umi
    3 right_umi
    4 reversed
    5 left_score
    9 right_score
    6 left_umi_qual
    10 right_umi_qual
    7 left_primer_from
    8 left_primer_to
    11 right_primer_from
    12 right_primer_to
     */
    private final PrintWriter printWriter;

    public PrimerSearchResultWriter(OutputStream os) {
        this.printWriter = new PrintWriter(os);

        printWriter.println(HEADER);
    }

    @Override
    public void put(CompositePrimerSearcherResult compositePrimerSearcherResult) {
        if (compositePrimerSearcherResult == null) {
            printWriter.close();
        } else {
            printWriter.println(compositePrimerSearcherResult.getReadId() + "\t" +
                    compositePrimerSearcherResult.getPrimerId() + "\t" +
                    compositePrimerSearcherResult.getLeftUmi().toString() + "\t" +
                    compositePrimerSearcherResult.getRightUmi().toString() + "\t" +
                    compositePrimerSearcherResult.isMatched() + "\t" +
                    compositePrimerSearcherResult.getLeftResult().getUmi().getQuality().toString() + "\t" +
                    compositePrimerSearcherResult.getRightResult().getUmi().getQuality().toString() + "\t" +
                    compositePrimerSearcherResult.getLeftResult().getFrom() + "\t" +
                    compositePrimerSearcherResult.getLeftResult().getTo() + "\t" +
                    compositePrimerSearcherResult.getRightResult().getFrom() + "\t" +
                    compositePrimerSearcherResult.getRightResult().getTo()
            );
        }
    }
}
