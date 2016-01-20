package com.antigenomics.mist.index;

import com.antigenomics.mist.mig.Tag;
import com.antigenomics.mist.primer.CompositePrimerSearcherResult;
import com.milaboratory.core.sequence.NucleotideSequence;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UmiIndex {
    private final boolean ignoreSampleIds;
    private final Map<Tag, List<Long>> readIdsByUmi = new ConcurrentHashMap<>();

    public UmiIndex(boolean ignoreSampleIds) {
        this.ignoreSampleIds = ignoreSampleIds;
    }

    public void add(CompositePrimerSearcherResult searcherResult) {
        if (searcherResult.isMatched()) {
            add(searcherResult.getLeftResult().getUmi().getSequence(),
                    searcherResult.getRightResult().getUmi().getSequence(),
                    searcherResult.getPrimerId(),
                    searcherResult.getReadWrapper().getRead().getId());
        }
    }

    public void add(NucleotideSequence leftUmi, NucleotideSequence rightUmi, String sampleId, long readId) {
        add(createTag(leftUmi, rightUmi, sampleId), readId);
    }

    public void add(Tag tag, long readId) {
        List<Long> readList = readIdsByUmi.get(tag);

        if (readList == null) {
            readIdsByUmi.put(tag, readList = new ArrayList<>());
        }

        readList.add(readId);
    }

    private Tag createTag(NucleotideSequence leftUmi, NucleotideSequence rightUmi, String sampleId) {
        return ignoreSampleIds ?
                new Tag(leftUmi, rightUmi) :
                new TagWithSampleId(leftUmi, rightUmi, sampleId);
    }
}
