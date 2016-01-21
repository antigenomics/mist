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

package com.antigenomics.mist.misc;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class Speaker {
    private Date start = null;

    public static final Speaker INSTANCE = new Speaker();

    public void sout(String message) {
        if (start == null)
            start = new Date();

        Date now = new Date();
        System.out.println("[" + now.toString() + " +" + timePassed(now.getTime() - start.getTime()) + "] " +
                message);
    }

    private String timePassed(long millis) {
        return String.format("%02dm%02ds",
                TimeUnit.MILLISECONDS.toMinutes(millis),
                TimeUnit.MILLISECONDS.toSeconds(millis) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))
        );
    }


    private Speaker() {

    }
}
