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

import java.util.ArrayList;
import java.util.List;

import com.japplis.virtually.Threads;
import com.japplis.virtually.sync.BlockLock;

import org.junit.jupiter.api.Test;

/**
 *
 * @author Anthony Goubard - Japplis
 */
public class BlockLockDemos {

    private int index;
    private BlockLock blockLock = new BlockLock();

    @Test
    void tryBlock() {
        List<Thread> threads = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            int loopIndex = i;
            Thread thread = Thread.startVirtualThread(() -> printIndex(loopIndex));
            threads.add(thread);
        }
        Threads.waitForAll(threads);
    }

    private void printIndex(int loopIndex) {
        try (var lock = blockLock.lockBlock()) {
            index = loopIndex;
            Threads.sleep(100);
            System.out.println(index + " -> " + loopIndex);
        }
    }

    private void printIndexUsingReentrantLock(int loopIndex) {
        blockLock.lock();
        try {
            index = loopIndex;
            Threads.sleep(100);
            System.out.println(index + " -> " + loopIndex);
        } finally {
            blockLock.unlock();
        }
    }
}
