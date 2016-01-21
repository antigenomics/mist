package com.antigenomics.mist.io;

import cc.redberry.pipe.InputPort;
import com.antigenomics.mist.primer.PrimerSearcherResult;

import java.io.OutputStream;
import java.io.PrintWriter;

public class PrimerSearchResultWriter implements InputPort<PrimerSearcherResult> {
    public static final String HEADER = "read_id\tprimer_id\t" +
            "left_umi\tright_umi\t" +
            "reversed\t" +
            "left_score\tleft_umi_qual\tleft_primer_from\tleft_primer_to\t" +
            "right_score\tright_umi_qual\tright_primer_from\tright_primer_to";

    private final PrintWriter printWriter;

    public PrimerSearchResultWriter(OutputStream os) {
        this.printWriter = new PrintWriter(os);

        printWriter.println(HEADER);
    }

    @Override
    public void put(PrimerSearcherResult primerSearcherResult) {
        if (primerSearcherResult == null) {
            printWriter.close();
        } else {
            printWriter.println(
                    primerSearcherResult.getReadId() + "\t" +
                            primerSearcherResult.getPrimerId() + "\t" +

                            primerSearcherResult.getLeftUmi().toString() + "\t" +
                            primerSearcherResult.getRightUmi().toString() + "\t" +

                            primerSearcherResult.isReversed() + "\t" +

                            primerSearcherResult.getLeftResult().getScore() + "\t" +
                            primerSearcherResult.getLeftResult().getUmi().getQuality().toString() + "\t" +
                            primerSearcherResult.getLeftResult().getFrom() + "\t" +
                            primerSearcherResult.getLeftResult().getTo() + "\t" +

                            primerSearcherResult.getRightResult().getScore() + "\t" +
                            primerSearcherResult.getRightResult().getUmi().getQuality().toString() + "\t" +
                            primerSearcherResult.getRightResult().getFrom() + "\t" +
                            primerSearcherResult.getRightResult().getTo()
            );
        }
    }
}
