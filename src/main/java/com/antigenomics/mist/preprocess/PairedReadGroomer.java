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

package com.antigenomics.mist.preprocess;

import com.milaboratory.core.io.sequence.PairedRead;
import com.milaboratory.core.io.sequence.SingleReadImpl;
import com.milaboratory.core.sequence.NSequenceWithQuality;

public class PairedReadGroomer extends ReadGroomer<PairedRead> {
    public PairedReadGroomer(boolean trim) {
        super(trim);
    }

    @Override
    protected PairedRead process(boolean reversed, int from, int to, String newDescription, ReadWrapper readWrapper) {
        NSequenceWithQuality data1 = readWrapper.getData(0, reversed),
                data2 = readWrapper.getData(1, reversed);
        
        long readId = readWrapper.getRead().getId();

        if (trim) {
            from = from < 0 ? 0 : from;
            to = to < 0 ? data2.size() : to;
            return new PairedRead(
                    new SingleReadImpl(readId,
                            data1.getRange(from, data1.size()),
                            newDescription),
                    new SingleReadImpl(readId,
                            data2.getRange(0, to),
                            newDescription));
        } else {
            return new PairedRead(
                    new SingleReadImpl(readId,
                            data1,
                            newDescription),
                    new SingleReadImpl(readId,
                            data2,
                            newDescription));
        }
    }
}
