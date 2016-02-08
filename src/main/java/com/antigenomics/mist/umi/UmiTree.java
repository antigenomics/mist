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

import cc.redberry.pipe.OutputPort;
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
    private final double errorPvalueThreshold;
    private final double independentAssemblyFdrThreshold;
    private final int observedDiversityEstimate;
    private int size;

    public UmiTree(int observedDiversityEstimate, int maxMismatches,
                   double errorPvalueThreshold, double independentAssemblyFdrThreshold) {
        this.observedDiversityEstimate = observedDiversityEstimate;
        this.treeSearchParameters = new TreeSearchParameters(maxMismatches, 0, 0);
        this.errorPvalueThreshold = errorPvalueThreshold;
        this.independentAssemblyFdrThreshold = independentAssemblyFdrThreshold;
    }

    public UmiTree(int observedDiversityEstimate) {
        this(observedDiversityEstimate, 1, 0.05, 0.1);
    }

    public void update(UmiCoverageAndQuality umiCoverageAndQuality) {
        NucleotideSequence umi = umiCoverageAndQuality.getUmiTag().getSequence();
        if (umiTree.get(umi) != null)
            throw new IllegalArgumentException("Duplicate UMIs are not allowed.");
        umiTree.put(umi, umiCoverageAndQuality);
        umiErrorAndDiversityModel.update(umiCoverageAndQuality);
        size++;
    }

    public void update(OutputPort<UmiCoverageAndQuality> umiInfoProvider) {
        UmiCoverageAndQuality umiCoverageAndQuality;

        while ((umiCoverageAndQuality = umiInfoProvider.take()) != null) {
            update(umiCoverageAndQuality);
        }
    }

    public UmiCoverageAndQuality get(NucleotideSequence umi) {
        return umiTree.get(umi);
    }

    public void traverseAndCorrect() {
        StreamSupport.stream(
                Spliterators.spliterator(umiTree.values().iterator(), size, Spliterator.IMMUTABLE),
                true
        ).forEach(this::setBestParent);
    }

    private void setBestParent(UmiCoverageAndQuality child) {
        NeighborhoodIterator<NucleotideSequence, UmiCoverageAndQuality> niter =
                umiTree.getNeighborhoodIterator(child.getUmiTag().getSequence(),
                        treeSearchParameters);

        UmiCoverageAndQuality parent;

        UmiParentInfo parentInfo, bestParentInfo = null;

        while ((parent = niter.next()) != null) {
            if (isEligiblePair(parent, child)) {
                parentInfo = computeParentInfo(parent, child);
                if (isGood(parentInfo) &&
                        (bestParentInfo == null || parentInfo.compareTo(bestParentInfo) > 0)) {
                    bestParentInfo = parentInfo;
                }
            }
        }

        child.setParentInfo(bestParentInfo);
    }

    private boolean isEligiblePair(UmiCoverageAndQuality parent, UmiCoverageAndQuality child) {
        return parent != child && (parent.getCoverage() > child.getCoverage()) ||
                (parent.getCoverage() == child.getCoverage() && // for singletons/doubletons
                        parent.getUmiTag().compareTo(child.getUmiTag()) > 0); // no dead loop
    }

    private boolean isGood(UmiParentInfo umiParentInfo) {
        return umiParentInfo.getIndependentAssemblyProb() * observedDiversityEstimate <= independentAssemblyFdrThreshold ||
                umiParentInfo.getErrorPValue() <= errorPvalueThreshold;
    }

    private UmiParentInfo computeParentInfo(UmiCoverageAndQuality parent, UmiCoverageAndQuality child) {
        return new UmiParentInfo(parent,
                umiErrorAndDiversityModel.errorPValue(parent, child),
                umiErrorAndDiversityModel.independentAssemblyProbability(parent, child));
    }

    public UmiParentInfo getTopParentInfo(NucleotideSequence umi) {
        UmiCoverageAndQuality umiCoverageAndQuality = umiTree.get(umi);

        if (umiCoverageAndQuality == null) {
            throw new IllegalArgumentException("UMI does not exist in the tree.");
        }

        UmiParentInfo parent = umiCoverageAndQuality.getParentInfo();

        if (parent != null) {
            UmiParentInfo nextParent;

            while ((nextParent = umiTree.get(parent.getUmi()).getParentInfo()) != null) {
                parent = nextParent;
            }
        }

        return parent;
    }

    public NucleotideSequence correct(NucleotideSequence umi) {
        UmiParentInfo parentInfo = getTopParentInfo(umi);
        return parentInfo == null ? umi : parentInfo.getUmi();
    }
}
