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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import com.japplis.virtually.scope.CallableFunction;

/**
 * Virtual thread friendly utility methods related to Map.
 * 
 * @author Anthony Goubard - Japplis
 */
public class Maps {

    private final static Map<Map, Map<Object, ReentrantLock>> MAP_KEY_LOCKS = new HashMap<>();
    private final static ReentrantLock MAP_LOCK = new ReentrantLock();

    // Only static methods
    private Maps() {
    }

    /**
     * Replacement for ConcurrentHashMap#computeIfAbsent as it's using synchronized block.
     * This method is more virtual thread friendly as it uses ReentrantLock.
     * Only use this method if the mapper performs I/O.
     *
     * Note that this method is not exactly the same as ConcurentHashMap#computeIfAbsent
     * in the sense that the synchronized block is not blocking the methods of the Map objects (like put).
     *
     * @param map the map to get the value from. It doesn't need to be a ConcurrentHashMap
     * @param key the map key to get the value from
     * @param mapper the way to compute the value if the key is not in the map. It should never return <code>null</code>.
     * @return the value from the map, never <code>null</code>
     */
    public static <E,R> R computeIfAbsent(Map<E,R> map, E key, CallableFunction<E,R> mapper) throws Exception {
        MAP_LOCK.lock();
        ReentrantLock keyLock;
        Map<Object, ReentrantLock> keyLocks;
        try {
            keyLocks = MAP_KEY_LOCKS.get(map);
            if (keyLocks == null) {
                keyLocks = new HashMap<>();
                MAP_KEY_LOCKS.put(map, keyLocks);
            }
            if (keyLocks.containsKey(key)) {
                keyLock = keyLocks.get(key);
            } else {
                keyLock = new ReentrantLock();
                keyLocks.put(key, keyLock);
            }
        } finally {
            MAP_LOCK.unlock();
        }
        keyLock.lock();
        try {
            R value;
            synchronized (map) {
                value = map.get(key);
            }
            if (value == null) {
                value = mapper.call(key);
                if (value == null) throw new IllegalStateException("Mapper is not allowed to return null values");
                synchronized (map) {
                    map.put(key, value);
                }
            }
            return value;
        } finally {
            MAP_LOCK.lock();
            try {
                int waiting = keyLock.getQueueLength();
                keyLock.unlock();
                if (waiting == 0) {
                    keyLocks.remove(key);
                    if (keyLocks.isEmpty()) {
                        MAP_KEY_LOCKS.remove(map);
                    }
                }
            } finally {
                MAP_LOCK.unlock();
            }
        }
    }

}
