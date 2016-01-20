package com.antigenomics.mist.mig;

import com.milaboratory.core.sequence.NucleotideSequence;

public class Tag {
    protected final NucleotideSequence leftUmi, rightUmi;

    public Tag(NucleotideSequence leftUmi, NucleotideSequence rightUmi) {
        this.leftUmi = leftUmi;
        this.rightUmi = rightUmi;
    }

    public NucleotideSequence getLeftUmi() {
        return leftUmi;
    }

    public NucleotideSequence getRightUmi() {
        return rightUmi;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Tag tag = (Tag) o;

        return leftUmi.equals(tag.leftUmi) && rightUmi.equals(tag.rightUmi);
    }

    @Override
    public int hashCode() {
        return 31 * leftUmi.hashCode() + rightUmi.hashCode();
    }
}
