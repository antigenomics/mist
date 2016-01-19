package com.antigenomics.mist.groomer;

import com.antigenomics.mist.primer.CompositePrimerSearcherResult;
import com.antigenomics.mist.primer.ReadWrapper;
import com.milaboratory.core.io.sequence.PairedRead;
import com.milaboratory.core.io.sequence.SequenceRead;
import com.milaboratory.core.io.sequence.SingleReadImpl;
import com.milaboratory.core.sequence.NSequenceWithQuality;

public class ReadGroomer {
    private final boolean trim;

    public ReadGroomer(boolean trim) {
        this.trim = trim;
    }

    public SequenceRead process(ReadWrapper readWrapper, CompositePrimerSearcherResult compositePrimerSearcherResult) {
        long id = readWrapper.getRead().getId();

        int from = compositePrimerSearcherResult.getLeftResult().getTo(),
                to = compositePrimerSearcherResult.getRightResult().getFrom();

        if (readWrapper.getRead().numberOfReads() == 1) {
            NSequenceWithQuality data = readWrapper.getData(0, compositePrimerSearcherResult.isReversed());

            if (trim) {
                from = from < 0 ? 0 : from;
                to = to < 0 ? data.size() : to;
                return new SingleReadImpl(id, data.getRange(from, to),
                        readWrapper.getRead().getRead(0).getDescription());
            } else {
                return new SingleReadImpl(id, data,
                        readWrapper.getRead().getRead(0).getDescription());
            }
        } else {
            NSequenceWithQuality data1 = readWrapper.getData(0, compositePrimerSearcherResult.isReversed()),
                    data2 = readWrapper.getData(1, compositePrimerSearcherResult.isReversed());

            if (trim) {
                from = from < 0 ? 0 : from;
                to = to < 0 ? data2.size() : to;
                return new PairedRead(
                        new SingleReadImpl(id, data1.getRange(from, data1.size()),
                                readWrapper.getRead().getRead(0).getDescription()),
                        new SingleReadImpl(id, data2.getRange(0, to),
                                readWrapper.getRead().getRead(1).getDescription()));
            } else {
                return new PairedRead(
                        new SingleReadImpl(id, data1,
                                readWrapper.getRead().getRead(0).getDescription()),
                        new SingleReadImpl(id, data2,
                                readWrapper.getRead().getRead(1).getDescription()));
            }
        }
    }
}
