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

import cc.redberry.pipe.OutputPort;
import com.antigenomics.mist.assemble.Mig;
import com.milaboratory.core.io.sequence.SequenceRead;

/**
 * Mig provider is thread-unsafe, and no synchronizaiton is added to "take" method.
 * So it should be used via buffer.
 *
 * @param <S>
 */
public interface MigProvider<S extends SequenceRead> extends OutputPort<Mig<S>> {
}
