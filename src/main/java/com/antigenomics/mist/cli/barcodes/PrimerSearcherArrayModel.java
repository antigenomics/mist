package com.antigenomics.mist.cli.barcodes;

import com.antigenomics.mist.primer.PrimerSearcherArray;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PrimerSearcherArrayModel {
    private ArrayList<PrimerSearcherModel> primerSearchers;

    public PrimerSearcherArrayModel() {
    }

    public PrimerSearcherArrayModel(ArrayList<PrimerSearcherModel> primerSearchers) {
        this.primerSearchers = primerSearchers;
    }

    public PrimerSearcherArray create() {
        return new PrimerSearcherArray(
                primerSearchers.stream().map(PrimerSearcherModel::create).collect(Collectors.toList())
        );
    }

    public List<PrimerSearcherModel> getPrimerSearchers() {
        return primerSearchers;
    }

    public void setPrimerSearchers(ArrayList<PrimerSearcherModel> primerSearchers) {
        this.primerSearchers = primerSearchers;
    }
}
