package com.antigenomics.mist.mig;

import com.milaboratory.core.sequence.NucleotideSequence;

public interface Tag {
    NucleotideSequence getLeftUmi();

    NucleotideSequence getRightUmi();

    String getPrimerId();
}
