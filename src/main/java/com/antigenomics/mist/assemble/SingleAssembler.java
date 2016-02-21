package com.antigenomics.mist.assemble;

import com.milaboratory.core.io.sequence.SingleRead;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class SingleAssembler extends Assembler<SingleRead> {
    public SingleAssembler() {
    }

    public SingleAssembler(float minSimilarity, int minAlignmentSize, float maxDiscardedReadsRatio, int maxAssemblePasses) {
        super(minSimilarity, minAlignmentSize, maxDiscardedReadsRatio, maxAssemblePasses);
    }

    @Override
    public AssemblyResult<SingleRead> process(Mig<SingleRead> input) {
        List<Consensus<SingleRead>> consensuses = new ArrayList<>();
        List<SingleRead> discardedReads = new ArrayList<>();

        List<AssemblyPassResult> results = assemble(input.getReads(), 0);

        for (int i = 0; i < results.size(); i++) {
            AssemblyPassResult result = results.get(i);
            consensuses.add(new SingleConsensus(new HashSet<>(result.getAssembledReads()),
                    result.getConsensus(), 0, i, input.getUmiTag()));
            discardedReads.addAll(result.getDiscardedReads());
        }

        return new AssemblyResult<>(consensuses, discardedReads);
    }
}
