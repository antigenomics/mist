package com.antigenomics.mist.preprocess;

import cc.redberry.pipe.Processor;
import com.antigenomics.mist.primer.PrimerSearcherResult;
import com.milaboratory.core.io.sequence.PairedRead;
import com.milaboratory.core.io.sequence.SequenceRead;
import com.milaboratory.core.io.sequence.SingleReadImpl;
import com.milaboratory.core.sequence.NSequenceWithQuality;

public class ReadGroomer implements Processor<PrimerSearcherResult, SequenceRead> {
    private final boolean trim;

    public ReadGroomer(boolean trim) {
        this.trim = trim;
    }

    public SequenceRead process(PrimerSearcherResult primerSearcherResult) {
        ReadWrapper readWrapper = primerSearcherResult.getReadWrapper();

        long id = readWrapper.getRead().getId();

        int from = primerSearcherResult.getLeftResult().getTo(),
                to = primerSearcherResult.getRightResult().getFrom();

        String description = HeaderUtil.generateHeader(primerSearcherResult);

        if (readWrapper.getRead().numberOfReads() == 1) {
            NSequenceWithQuality data = readWrapper.getData(0, primerSearcherResult.isReversed());

            if (trim) {
                from = from < 0 ? 0 : from;
                to = to < 0 ? data.size() : to;
                return new SingleReadImpl(id, data.getRange(from, to),
                        readWrapper.getRead().getRead(0).getDescription() + description);
            } else {
                return new SingleReadImpl(id, data,
                        readWrapper.getRead().getRead(0).getDescription() + description);
            }
        } else {
            NSequenceWithQuality data1 = readWrapper.getData(0, primerSearcherResult.isReversed()),
                    data2 = readWrapper.getData(1, primerSearcherResult.isReversed());

            if (trim) {
                from = from < 0 ? 0 : from;
                to = to < 0 ? data2.size() : to;
                return new PairedRead(
                        new SingleReadImpl(id, data1.getRange(from, data1.size()),
                                readWrapper.getRead().getRead(0).getDescription() + description),
                        new SingleReadImpl(id, data2.getRange(0, to),
                                readWrapper.getRead().getRead(1).getDescription() + description));
            } else {
                return new PairedRead(
                        new SingleReadImpl(id, data1,
                                readWrapper.getRead().getRead(0).getDescription() + description),
                        new SingleReadImpl(id, data2,
                                readWrapper.getRead().getRead(1).getDescription() + description));
            }
        }
    }
}
