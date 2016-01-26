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

package com.antigenomics.mist.assemble;

import com.milaboratory.core.io.sequence.SequenceRead;

import java.util.Collections;
import java.util.List;

public class AssemblyResult<T extends SequenceRead> {
    private final List<Consensus<T>> consensuses;
    private final List<T> discardedReads;

    public AssemblyResult(List<Consensus<T>> consensuses, List<T> discardedReads) {
        this.consensuses = consensuses;
        this.discardedReads = discardedReads;
    }

    public List<Consensus<T>> getConsensuses() {
        return Collections.unmodifiableList(consensuses);
    }

    public List<T> getDiscardedReads() {
        return Collections.unmodifiableList(discardedReads);
    }
}
