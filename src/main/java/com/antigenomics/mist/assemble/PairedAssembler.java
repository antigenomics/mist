package com.antigenomics.mist.assemble;

import com.milaboratory.core.io.sequence.PairedRead;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PairedAssembler extends Assembler<PairedRead> {
    private final float minOverlapRatio;

    public PairedAssembler(float minSimilarity, int minAlignmentSize,
                           float maxDiscardedReadsRatio, int maxAssemblePasses,
                           float minOverlapRatio) {
        super(minSimilarity, minAlignmentSize, maxDiscardedReadsRatio, maxAssemblePasses);
        this.minOverlapRatio = minOverlapRatio;
    }

    @Override
    public AssemblyResult<PairedRead> process(Mig<PairedRead> input) {
        List<Consensus<PairedRead>> consensuses = new ArrayList<>();
        Set<PairedRead> discardedReads = new HashSet<>();

        List<AssemblyPassResult> resultsLeft = assemble(input.getReads(), 0),
                resultsRight = assemble(input.getReads(), 1);

        int k = 0;
        for (int i = 0; i < resultsLeft.size(); i++) {
            for (int j = 0; j < resultsRight.size(); j++) {
                AssemblyPassResult leftResult = resultsLeft.get(i),
                        rightResult = resultsRight.get(i);

                PairedConsensus pairedConsensus = new PairedConsensus(
                        new SingleConsensus(new HashSet<>(leftResult.getAssembledReads()),
                                leftResult.getConsensus(), 0, i, input.getUmiTag()),
                        new SingleConsensus(new HashSet<>(rightResult.getAssembledReads()),
                                rightResult.getConsensus(), 1, i, input.getUmiTag()),
                        k++);

                if (pairedConsensus.getOverlap() >= minOverlapRatio) {
                    consensuses.add(pairedConsensus);

                    // TODO: how to represent discarded reads in this case?

                    discardedReads.addAll(leftResult.getDiscardedReads());
                    discardedReads.addAll(rightResult.getDiscardedReads());
                }
            }
        }

        return new AssemblyResult<>(consensuses, new ArrayList<>(discardedReads));
    }
}
