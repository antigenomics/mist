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

import cc.redberry.pipe.InputPort;

import java.io.OutputStream;
import java.io.PrintWriter;

public class UmiInfoWriter implements InputPort<UmiInfo> {
    private final PrintWriter writer;

    public UmiInfoWriter(OutputStream outputStream) {
        this.writer = new PrintWriter(outputStream);
    }

    @Override
    public void put(UmiInfo umiInfo) {
        if (umiInfo == null) {
            writer.close();
        } else {
            writer.println(umiInfo.getUmiTag().getPrimerId() + "\t" +
                            umiInfo.getUmiTag().getLeftUmi().toString() + "\t" +
                            umiInfo.getUmiTag().getRightUmi().toString() + "\t" +
                            umiInfo.getCount() + "\t" +
                            umiInfo.getAverageQualityLeft() + "\t" +
                            umiInfo.getAverageQualityRight()
            );
        }
    }
}
