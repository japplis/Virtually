/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2023 Japplis.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.japplis.virtually.demo;


import com.japplis.virtually.ThreadBucket;
import com.japplis.virtually.Threads;

import org.junit.jupiter.api.Test;

import static com.japplis.virtually.sync.SyncUtils.*;

/**
 *
 * @author Anthony Goubard - Japplis
 */
public class SyncUtilsDemos {
    private int index;
    private Object lock = new Object();

    @Test
    void runSynchronizedDemo() {
        ThreadBucket threads = new ThreadBucket("runSynchronized");
        for (int i = 1; i <= 10; i++) {
            int loopIndex = i;
            threads.startVitualThread(() -> printIndex(loopIndex));
        }
        threads.waitForAll();
    }

    private void printIndex(int loopIndex) {
        runSynchronized(() -> {
            index = loopIndex;
            Threads.sleep(100);
            System.out.println(index + " -> " + loopIndex);
            return null;
        });
    }

    private void printIndexLock(int loopIndex) {
        runSynchronized(lock, () -> {
            index = loopIndex;
            Threads.sleep(100);
            System.out.println(index + " -> " + loopIndex);
            return null;
        });
    }

    private void printIndexUnlocked(int loopIndex) {
        index = loopIndex;
        Threads.sleep(100);
        System.out.println(index + " -> " + loopIndex);
    }
}
