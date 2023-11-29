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
package com.japplis.virtually;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * A group of related virtual threads.
 *
 * @author Anthony Goubard - Japplis
 */
public class ThreadBucket {

    private List<Thread> threads = new ArrayList<>();
    private final Thread.Builder.OfVirtual threadFactory;

    public ThreadBucket(String name) {
        threadFactory = Thread.ofVirtual().name(name, 1);
    }

    public Thread startVitualThread(Runnable run) {
        Thread thread = threadFactory.start(run);
        threads.add(thread);
        return thread;
    }

    public List<Thread> getThreads() {
        return threads;
    }

    public void interrupt() {
        for (Thread thread : threads) {
            if (thread.isAlive()) {
                thread.interrupt();
            }
        }
    }

    public void waitForAll() {
        Threads.waitForAll(threads);
    }

    public List<Thread> waitFor(Duration maxWait) {
        List<Thread> finishedThreads = new ArrayList<>();
        long startWaitMillis = System.currentTimeMillis();
        long maxWaitMillis = maxWait.get(ChronoUnit.NANOS) / 1000;
        for (Thread thread : threads) {
            long runningTime = System.currentTimeMillis() - startWaitMillis;
            long remainingTime = runningTime - maxWaitMillis;
            if (remainingTime > 0) {
                try {
                    thread.join(remainingTime);
                    finishedThreads.add(thread);
                } catch (InterruptedException ex) {
                    // Ignore
                }
            } else if (thread.isAlive()) {
                thread.interrupt();
            } else {
                finishedThreads.add(thread);
            }
        }
        return finishedThreads;
    }
}
