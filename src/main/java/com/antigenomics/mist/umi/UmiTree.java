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
import com.milaboratory.core.tree.SequenceTreeMap;
import com.milaboratory.core.tree.TreeSearchParameters;

public class UmiTree {
    private final SequenceTreeMap<NucleotideSequence, UmiCoverageAndQuality> umiTree =
            new SequenceTreeMap<>(NucleotideSequence.ALPHABET);
    private final TreeSearchParameters treeSearchParameters;
    private final int depth;

    public UmiTree(int maxMismatches, int depth) {
        this.treeSearchParameters = new TreeSearchParameters(maxMismatches, 0, 0);
        this.depth = depth;
    }

    public UmiTree() {
        this(2, -1);
    }

    public void update(UmiCoverageAndQuality umiCoverageAndQuality) {
        umiTree.put(umiCoverageAndQuality.getUmiTag().getSequence(), umiCoverageAndQuality);
    }
}
