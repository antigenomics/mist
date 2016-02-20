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

package com.antigenomics.mist.assemble.ioadapter;

import com.antigenomics.mist.assemble.Mig;
import com.antigenomics.mist.misc.HeaderUtil;
import com.antigenomics.mist.umi.UmiTag;
import com.milaboratory.core.io.sequence.SequenceRead;
import com.milaboratory.core.io.sequence.SequenceReader;

import java.util.*;

public class InMemoryMigProvider<S extends SequenceRead> implements MigProvider<S> {
    private final Iterator<Map.Entry<UmiTag, List<S>>> iter;

    public InMemoryMigProvider(SequenceReader<S> reader) {
        S read;

        Map<UmiTag, List<S>> readsByTag = new HashMap<>();
        while ((read = reader.take()) != null) {
            UmiTag umiTag = HeaderUtil.parsedHeader(read.getRead(0).getDescription()).toUmiTag();
            List<S> reads = readsByTag.computeIfAbsent(umiTag, tmp -> new ArrayList<>());
            reads.add(read);
        }

        this.iter = readsByTag.entrySet().iterator();
    }

    @Override
    public Mig<S> take() {
        if (!iter.hasNext()) {
            return null;
        }
        Map.Entry<UmiTag, List<S>> entry = iter.next();

        return new Mig<>(entry.getKey(), entry.getValue());
    }
}
