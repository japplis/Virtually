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

import java.util.Collection;

/**
 *
 * @author Anthony Goubard - Japplis
 */
public class Threads {

    /**
     * Waits for all the threads (virtual or not) in the collection.
     * If some threads are interrupted, no exception will be thrown.
     *
     * @param threads the threads to wait for
     */
    public static void waitForAll(Collection<Thread> threads) {
        for (Thread thread : threads)  {
            waitFor(thread);
        }
    }

    /**
     * Wait for a thread (virtual or not) to finish or get interrupted.
     * If the thread is interrupted, no exception will be thrown.
     *
     * @param thread the thread to wait for
     */
    public static void waitFor(Thread thread) {
        try {
            thread.join();
        } catch (InterruptedException ex) {
            // Ignore
        }
    }

    /**
     * Pauses the current thread for x milliseconds.
     * If the thread is interrupted, no exception will be thrown and this method will return false.
     *
     * @param millis the number of milliseconds to wait for.
     * @return false if the thread was interrupted, true otherwise.
     */
    public static boolean sleep(int millis) {
        try {
            Thread.sleep(millis);
            return true;
        } catch (InterruptedException ex) {
            return false;
        }
    }
}
