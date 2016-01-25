/*
 * Copyright 2015 Mikhail Shugay (mikhail.shugay@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.antigenomics.mist.umi;

import com.milaboratory.core.sequence.NucleotideSequence;
import com.milaboratory.core.tree.NeighborhoodIterator;
import com.milaboratory.core.tree.SequenceTreeMap;
import com.milaboratory.core.tree.TreeSearchParameters;

import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.StreamSupport;

public class UmiTree {
    private final SequenceTreeMap<NucleotideSequence, UmiCoverageAndQuality> umiTree =
            new SequenceTreeMap<>(NucleotideSequence.ALPHABET);
    private final TreeSearchParameters treeSearchParameters;
    private final UmiErrorAndDiversityModel umiErrorAndDiversityModel = new UmiErrorAndDiversityModel();
    private final double errorLogOddsRatioThreshold;
    private int size;

    public UmiTree(int maxMismatches, double errorLogOddsRatioThreshold) {
        this.treeSearchParameters = new TreeSearchParameters(maxMismatches, 0, 0);
        this.errorLogOddsRatioThreshold = errorLogOddsRatioThreshold;
    }

    public UmiTree() {
        this(2, 0.5);
    }

    public void update(UmiCoverageAndQuality umiCoverageAndQuality) {
        NucleotideSequence umi = umiCoverageAndQuality.getUmiTag().getSequence();
        if (umiTree.get(umi) != null)
            throw new IllegalArgumentException("Duplicate UMIs are not allowed.");
        umiTree.put(umi, umiCoverageAndQuality);
        umiErrorAndDiversityModel.update(umiCoverageAndQuality);
        size++;
    }

    public UmiCoverageAndQuality get(NucleotideSequence umi) {
        return umiTree.get(umi);
    }

    public void traverseAndCorrect() {
        StreamSupport.stream(
                Spliterators.spliterator(umiTree.values().iterator(), size, Spliterator.IMMUTABLE),
                true
        ).forEach(this::correct);
    }

    private void correct(UmiCoverageAndQuality umiCoverageAndQuality) {
        NeighborhoodIterator<NucleotideSequence, UmiCoverageAndQuality> niter =
                umiTree.getNeighborhoodIterator(umiCoverageAndQuality.getUmiTag().getSequence(),
                        treeSearchParameters);

        UmiCoverageAndQuality bestParent = null;

        UmiCoverageAndQuality parent;

        while ((parent = niter.next()) != null) {
            if (isParent(parent, umiCoverageAndQuality) &&
                    (bestParent == null || bestParent.getCoverage() < parent.getCoverage())) {
                bestParent = parent;
            }
        }

        if (bestParent != null) {
            umiCoverageAndQuality.setParent(bestParent.getUmiTag());
        }
    }

    private boolean isParent(UmiCoverageAndQuality parent, UmiCoverageAndQuality child) {
        return parent.getCoverage() > child.getCoverage() && 
                umiErrorAndDiversityModel.getErrorLogOddsRatio(parent, child) < errorLogOddsRatioThreshold;
    }

    public NucleotideSequence correct(NucleotideSequence umi) {
        UmiCoverageAndQuality umiCoverageAndQuality = umiTree.get(umi);

        if (umiCoverageAndQuality == null) {
            throw new IllegalArgumentException("UMI does not exist in the tree.");
        }

        NucleotideSequence parent = umi, nextParent;

        while ((nextParent = umiTree.get(parent).getParent().getSequence()) != null) {
            parent = nextParent;
        }

        return parent;
    }
}
