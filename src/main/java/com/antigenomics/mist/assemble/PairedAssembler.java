package com.antigenomics.mist.assemble;

import com.milaboratory.core.io.sequence.PairedRead;
import com.milaboratory.core.io.sequence.SingleRead;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PairedAssembler extends Assembler<PairedRead>{
    public PairedAssembler(float minSimilarity, int minAlignmentSize, float maxDiscardedReadsRatio, int maxAssemblePasses) {
        super(minSimilarity, minAlignmentSize, maxDiscardedReadsRatio, maxAssemblePasses);
    }

    @Override
    public AssemblyResult<PairedRead> process(Mig<PairedRead> input) {
        List<Consensus<PairedRead>> consensuses = new ArrayList<>();
        Set<PairedRead> discardedReads = new HashSet<>();

        List<AssemblyPassResult> resultsLeft = assemble(input.getReads(), 0),
                resultsRight = assemble(input.getReads(), 1);

        if (resultsLeft.size() > resultsRight.size()) {
            for (int i = 0; i < results.size(); i++) {

            }
        }else {

        }

        for (int i = 0; i < results.size(); i++) {
            AssemblyPassResult result = results.get(i);
            consensuses.add(new SingleConsensus(new HashSet<>(result.getAssembledReads()),
                    result.getConsensus(), 0, i, input.getUmiTag()));
            discardedReads.addAll(result.getDiscardedReads());
        }

        return new AssemblyResult<>(consensuses, discardedReads);
    }
}
