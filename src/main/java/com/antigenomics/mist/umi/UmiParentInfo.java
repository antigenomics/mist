package com.antigenomics.mist.umi;

import com.milaboratory.core.sequence.NucleotideSequence;

public class UmiParentInfo implements Comparable<UmiParentInfo> {
    private final UmiCoverageAndQuality parentCoverage;
    private final double errorPValue, independentAssemblyProb;

    public UmiParentInfo(UmiCoverageAndQuality parentCoverage, double errorPValue, double independentAssemblyProb) {
        this.parentCoverage = parentCoverage;
        this.errorPValue = errorPValue;
        this.independentAssemblyProb = independentAssemblyProb;
    }

    public NucleotideSequence getUmi() {
        return parentCoverage.getUmiTag().getSequence();
    }

    public UmiCoverageAndQuality getParentCoverage() {
        return parentCoverage;
    }

    public double getErrorPValue() {
        return errorPValue;
    }

    public double getIndependentAssemblyProb() {
        return independentAssemblyProb;
    }

    @Override
    public int compareTo(UmiParentInfo o) {
        return Integer.compare(parentCoverage.getCoverage(), o.parentCoverage.getCoverage());
    }
}
