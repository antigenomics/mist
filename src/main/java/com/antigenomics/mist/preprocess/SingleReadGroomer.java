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

import com.milaboratory.core.io.sequence.SingleRead;
import com.milaboratory.core.io.sequence.SingleReadImpl;
import com.milaboratory.core.sequence.NSequenceWithQuality;

public class SingleReadGroomer extends ReadGroomer<SingleRead> {

    public SingleReadGroomer(boolean trim) {
        super(trim);
    }

    @Override
    protected SingleRead process(boolean reversed, int from, int to, String newDescription, ReadWrapper readWrapper) {
        NSequenceWithQuality data = readWrapper.getData(0, reversed);

        long readId = readWrapper.getRead().getId();

        from = from < 0 ? 0 : from;
        to = to < 0 ? data.size() : to;

        if (trim && from < to) { // Need to protect from primer overlap here. By default no trimming for such case.
            return new SingleReadImpl(readId,
                    data.getRange(from, to),
                    newDescription);
        } else {
            return new SingleReadImpl(readId,
                    data,
                    newDescription);
        }
    }
}
