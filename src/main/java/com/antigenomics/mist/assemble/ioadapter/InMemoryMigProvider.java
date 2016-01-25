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
import com.antigenomics.mist.preprocess.HeaderUtil;
import com.antigenomics.mist.umi.UmiTag;
import com.milaboratory.core.io.sequence.SequenceRead;
import com.milaboratory.core.io.sequence.SequenceReader;

import java.util.*;

public class InMemoryMigProvider<T extends SequenceRead> implements MigProvider<T> {
    private final Map<UmiTag, List<T>> readsByTag = new HashMap<>();
    private final Iterator<Map.Entry<UmiTag, List<T>>> iter;

    public InMemoryMigProvider(SequenceReader<T> reader) {
        T read;

        while ((read = reader.take()) != null) {
            UmiTag umiTag = HeaderUtil.parsedHeader(read.getRead(0).getDescription()).toUmiTag();
            List<T> reads = readsByTag.computeIfAbsent(umiTag, tmp -> new ArrayList<>());
            reads.add(read);
        }

        this.iter = readsByTag.entrySet().iterator();
    }

    @Override
    public Mig<T> take() {
        if (!iter.hasNext()) {
            return null;
        }
        Map.Entry<UmiTag, List<T>> entry = iter.next();
        
        return new Mig<>(entry.getKey(), entry.getValue());
    }
}
