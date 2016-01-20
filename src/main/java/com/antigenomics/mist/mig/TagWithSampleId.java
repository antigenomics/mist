package com.antigenomics.mist.mig;

import com.milaboratory.core.sequence.NucleotideSequence;

public class TagWithSampleId extends Tag {
    private final String sampleId;

    public TagWithSampleId(NucleotideSequence leftUmi, NucleotideSequence rightUmi, String sampleId) {
        super(leftUmi, rightUmi);
        this.sampleId = sampleId;
    }

    public String getSampleId() {
        return sampleId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TagWithSampleId tag = (TagWithSampleId) o;

        return leftUmi.equals(tag.leftUmi) && rightUmi.equals(tag.rightUmi) && sampleId.equals(tag.sampleId);
    }

    @Override
    public int hashCode() {
        return 31 * (31 * leftUmi.hashCode() + rightUmi.hashCode()) + sampleId.hashCode();
    }
}
