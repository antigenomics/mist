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

import cc.redberry.pipe.Processor;
import com.antigenomics.mist.primer.PrimerSearcherResult;
import com.milaboratory.core.io.sequence.SequenceRead;

public abstract class ReadGroomer<T extends SequenceRead> implements Processor<PrimerSearcherResult, T> {
    protected final boolean trim;

    public ReadGroomer(boolean trim) {
        this.trim = trim;
    }

    protected abstract T process(boolean reversed,
                                 int from, int to, String newDescription, ReadWrapper readWrapper);

    @Override
    public T process(PrimerSearcherResult primerSearcherResult) {
        ReadWrapper readWrapper = primerSearcherResult.getReadWrapper();

        return process(primerSearcherResult.isReversed(),
                primerSearcherResult.getLeftResult().getTo(),
                primerSearcherResult.getRightResult().getFrom(),
                HeaderUtil.generateHeader(primerSearcherResult),
                readWrapper);
    }
}
