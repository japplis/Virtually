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
package com.japplis.virtually.sync;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Set of static utilities methods that helps to use ReentrantLock instead of synchronized in
 * order to avoid pinning a virtual thread on a platform thread.
 *
 * @author Anthony Goubard - Japplis
 */
public class SyncUtils {

    private final static Map<Object, ReentrantLock> OBJECT_LOCKS = new HashMap<>();
    private final static ReentrantLock OBJECT_LOCK = new ReentrantLock();

    /**
     * Run the function in a synchronized way using a ReentrantLock instead of a synchronized block to avoid pinning a virtual thread.
     * Note that the function is synchronized on the caller.
     *
     * @param <R> the type returned by the function
     * @param function the synchronized block
     * @return the result of the synchronized block
     */
    public static <R> R runSynchronized(Supplier<R> function) {
        StackWalker callStack = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);
        String callerCannonicalClassName = callStack.getCallerClass().getCanonicalName(); // synchronized on the class level
        return runSynchronized(callerCannonicalClassName, function);
    }

    /**
     * Run the function in a synchronized way using a ReentrantLock instead of a synchronized block to avoid pinning a virtual thread.
     *
     * @param <R> the type returned by the function
     * @param lockKey the object to synchronized upon
     * @param function the synchronized block
     * @return the result of the synchronized block
     */
    public static <R> R runSynchronized(Object lockKey, Supplier<R> function) {
        ReentrantLock callLock = lock(lockKey);
        try {
            return function.get();
        } finally {
            unlock(callLock, lockKey);
        }
    }

    public static <E,R> R runSynchronized(Object lockKey, E element, Function<E,R> function) {
        ReentrantLock callLock = lock(lockKey);
        try {
            return function.apply(element);
        } finally {
            unlock(callLock, lockKey);
        }
    }

    public static <R> R callSynchronized(Object lockKey, Callable<R> function) throws Exception {
        ReentrantLock callLock = lock(lockKey);
        try {
            return function.call();
        } finally {
            unlock(callLock, lockKey);
        }
    }

    public static ReentrantLock lock(Object lockKey) {
        if (lockKey instanceof ReentrantLock lock) {
            lock.lock();
            return lock;
        }
        OBJECT_LOCK.lock();
        ReentrantLock objectLock = OBJECT_LOCKS.get(lockKey);
        try {
            if (objectLock == null) {
                objectLock = new ReentrantLock();
                OBJECT_LOCKS.put(lockKey, objectLock);
            }
        } finally {
            OBJECT_LOCK.unlock();
        }
        objectLock.lock();
        return objectLock;
    }

    public static void unlock(ReentrantLock lock, Object lockKey) {
        OBJECT_LOCK.lock();
        try{
            int waiting = lock.getQueueLength();
            lock.unlock();
            if (waiting == 0) {
                OBJECT_LOCKS.remove(lockKey);
            }
        } finally {
            OBJECT_LOCK.unlock();
        }
    }
}
