package com.antigenomics.mist.cli.barcodes;

import com.antigenomics.mist.primer.PrimerSearcher;

public class PrimerSearcherModel {
    private String primerId;
    private PatternSearcherModel fivePrimeSearcher, threePrimeSearcher;
    private boolean reverseAllowed;

    public PrimerSearcherModel() {
    }

    public PrimerSearcherModel(String primerId,
                               PatternSearcherModel fivePrimeSearcher,
                               PatternSearcherModel threePrimeSearcher,
                               boolean reverseAllowed) {
        this.primerId = primerId;
        this.fivePrimeSearcher = fivePrimeSearcher;
        this.threePrimeSearcher = threePrimeSearcher;
        this.reverseAllowed = reverseAllowed;
    }

    public PrimerSearcher create() {
        return new PrimerSearcher(primerId,
                fivePrimeSearcher.create(),
                threePrimeSearcher.create(),
                reverseAllowed);
    }

    public String getPrimerId() {
        return primerId;
    }

    public PatternSearcherModel getFivePrimeSearcher() {
        return fivePrimeSearcher;
    }

    public PatternSearcherModel getThreePrimeSearcher() {
        return threePrimeSearcher;
    }

    public boolean isReverseAllowed() {
        return reverseAllowed;
    }

    public void setPrimerId(String primerId) {
        this.primerId = primerId;
    }

    public void setFivePrimeSearcher(PatternSearcherModel fivePrimeSearcher) {
        this.fivePrimeSearcher = fivePrimeSearcher;
    }

    public void setThreePrimeSearcher(PatternSearcherModel threePrimeSearcher) {
        this.threePrimeSearcher = threePrimeSearcher;
    }

    public void setReverseAllowed(boolean reverseAllowed) {
        this.reverseAllowed = reverseAllowed;
    }
}