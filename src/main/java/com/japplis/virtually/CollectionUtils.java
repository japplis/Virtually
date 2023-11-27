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

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

import com.japplis.virtually.scope.WaitingFunction;

/**
 * A set of utilities to make code more virtual thread friendly.
 *
 * @author Anthony Goubard - Japplis
 */
public class CollectionUtils {

    /**
     * Converts a list to another list using the mapper using virtual thread to execute the mapper.
     * The mapper needs to a function that performs I/O or waits to make sense of using virtual thread.
     * The result list can be smaller as only the convertions that didn't throw an exception will be in the result;
     *
     * @param <E> the elements type
     * @param <R> the result type
     * @param elements the list of elements to convert
     * @param mapper the function to convert elements
     * @return the converted elements according to the mapper, or an empty list if the scope is interrupted.
     */
    public static <E, R> List<R> convertList(List<E> elements, Function<E, R> mapper) {
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<R>> futures = new  ArrayList<>();
            for (E elem : elements) {
                Future<R> task = executor.submit(() -> mapper.apply(elem));
                futures.add(task);
            }
            List<R> results = new ArrayList<>();
            for (Future<R> task : futures) {
                try {
                    R result = task.get();
                    results.add(result);
                } catch (Exception ex) {
                    // Ignore
                }
            }
            return results;
        }
        /*try (StructuredTaskScope scope = new StructuredTaskScope()) {
            List<StructuredTaskScope.Subtask<R>> subtasks = new ArrayList<>();
            for (E elem : elements) {
                var subtask = scope.fork(() -> mapper.apply(elem));
                subtasks.add(subtask);
            }
            try {
                scope.join();
            } catch (InterruptedException ex) {
                return new ArrayList<>();
            }
            List<R> results = new ArrayList<>();
            for (var subtask : subtasks) {
                if (subtask.state() == StructuredTaskScope.Subtask.State.SUCCESS) {
                    results.add(subtask.get());
                }
            }
            return results;
        }*/
    }

    /**
     * Converts all the elements in the list or throw an Exception if a convertion failed.
     *
     * @param <E> the elements type
     * @param <R> the result type
     * @param elements the list of elements to convert
     * @param mapper the function to convert elements
     * @return the converted elements according to the mapper
     * @throws Exception if a mapping failed or it was interruped or cancelled
     */
    public static <E, R> List<R> convertAll(List<E> elements, WaitingFunction<E, R> mapper) throws Exception {
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<R>> futures = new  ArrayList<>();
            for (E elem : elements) {
                Future<R> task = executor.submit(() -> mapper.call(elem));
                futures.add(task);
            }
            List<R> results = new ArrayList<>();
            for (Future<R> task : futures) {
                try {
                    R result = task.get();
                    results.add(result);
                } catch (Exception ex) {
                    if (ex.getCause() instanceof Exception cause && cause != null) {
                        throw cause;
                    }
                    throw ex;
                }
            }
            return results;
        }
        /*try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            List<StructuredTaskScope.Subtask<R>> subtasks = new ArrayList<>();
            for (E elem : elements) {
                var subtask = scope.fork(() -> mapper.call(elem));
                subtasks.add(subtask);
            }
            scope.join();
            try {
                scope.throwIfFailed();
            } catch (ExecutionException ex) {
                if (ex.getCause() instanceof Exception cause && cause != null) {
                    throw cause;
                }
                throw ex;
            }
            List<R> results = new ArrayList<>();
            for (var subtask : subtasks) {
                results.add(subtask.get());
            }
            return results;
        }*/
    }

    /**
     * Converts a list to a map where the key is the element and value the mapped function for the element using the mapper using virtual thread to execute the mapper.
     * The mapper needs to a function that performs I/O or waits to make sense of using virtual thread.
     * The result map can be smaller as only the successful mapped function  will be in the result.
     *
     * @param <E> the elements type
     * @param <R> the result type
     * @param elements the list of elements to convert
     * @param mapper the function to convert elements
     * @return the elements with converted elements according to the mapper, or an empty map if the scope is interrupted.
     */
    public static <E, R> Map<E, R> convertToMap(List<E> elements, Function<E, R> mapper) {
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            Map<E, Future<R>> futures = new LinkedHashMap<>();
            for (E elem : elements) {
                Future<R> task = executor.submit(() -> mapper.apply(elem));
                futures.put(elem, task);
            }
            Map<E, R> results = new LinkedHashMap<>();
            for (var elemToTask : futures.entrySet()) {
                try {
                    E elem = elemToTask.getKey();
                    R result = elemToTask.getValue().get();
                    results.put(elem, result);
                } catch (Exception ex) {
                    // Ignore
                }
            }
            return results;
        }
        /*try (StructuredTaskScope scope = new StructuredTaskScope()) {
            Map<E, StructuredTaskScope.Subtask<R>> subtasks = new HashMap<>();
            for (E elem : elements) {
                var subtask = scope.fork(() -> mapper.apply(elem));
                subtasks.put(elem, subtask);
            }
            try {
                scope.join();
            } catch (InterruptedException ex) {
                return new HashMap<>();
            }
            Map<E, R> results = new HashMap<>();
            for (var elemToSubtask : subtasks.entrySet()) {
                var subtask = elemToSubtask.getValue();
                if (subtask.state() == StructuredTaskScope.Subtask.State.SUCCESS) {
                    results.put(elemToSubtask.getKey(), subtask.get());
                }
            }
            return results;
        }*/
    }

    /**
     * Converts a list to a map where the key is the element and value the mapped function for the element using the mapper using virtual thread to execute the mapper.
     * The mapper needs to a function that performs I/O or waits to make sense of using virtual thread.
     * The result map should have the same size as the list unless it contains duplicate elements.
     *
     * @param <E> the elements type
     * @param <R> the result type
     * @param elements the list of elements to convert
     * @param mapper the function to convert elements
     * @return the elements with converted elements according to the mapper
     * @throws Exception if it failed
     */
    public static <E, R> Map<E, R> convertAllToMap(List<E> elements, WaitingFunction<E, R> mapper) throws Exception {
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            Map<E, Future<R>> futures = new LinkedHashMap<>();
            for (E elem : elements) {
                Future<R> task = executor.submit(() -> mapper.call(elem));
                futures.put(elem, task);
            }
            Map<E, R> results = new LinkedHashMap<>();
            for (var elemToTask : futures.entrySet()) {
                try {
                    E elem = elemToTask.getKey();
                    R result = elemToTask.getValue().get();
                    results.put(elem, result);
                } catch (Exception ex) {
                    if (ex.getCause() instanceof Exception cause && cause != null) {
                        throw cause;
                    }
                    throw ex;
                }
            }
            return results;
        }
        /*try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            Map<E, StructuredTaskScope.Subtask<R>> subtasks = new HashMap<>();
            for (E elem : elements) {
                var subtask = scope.fork(() -> mapper.apply(elem));
                subtasks.put(elem, subtask);
            }
            scope.join();
            try {
                scope.throwIfFailed();
            } catch (ExecutionException ex) {
                if (ex.getCause() instanceof Exception cause && cause != null) {
                    throw cause;
                }
                throw ex;
            }
            Map<E, R> results = new HashMap<>();
            for (var elemToSubtask : subtasks.entrySet()) {
                var subtask = elemToSubtask.getValue();
                results.put(elemToSubtask.getKey(), subtask.get());
            }
            return results;
        }*/
    }

    /**
     * Converts a list to a map where the key is the element and value the mapped function for the element using the mapper using virtual thread to execute the mapper.
     * The mapper needs to a function that performs I/O or waits to make sense of using virtual thread.
     * The result list can be smaller as only the successful mapped function  will be in the result.
     * <code>e.g. convertToMap(products, Product::getId, PriceService::getPrice);</code>
     *
     * @param <E> the elements type
     * @param <R> the result type
     * @param elements the list of elements to convert
     * @param keyMapper the function to convert elements to key used by the mapper. This function is not executed in a virtual thread.
     * @param mapper the function to convert keys
     * @return the elements with converted elements according to the mapper, or an empty map if the scope is interrupted.
     */
    /*public static <E, K, R> Map<E, R> convertToMap(List<E> elements, Function<E, K> keyMapper, Function<K, R> mapper) {
        try (StructuredTaskScope scope = new StructuredTaskScope()) {
            Map<E, StructuredTaskScope.Subtask<R>> subtasks = new HashMap<>();
            for (E elem : elements) {
                K key = keyMapper.apply(elem);
                var subtask = scope.fork(() -> mapper.apply(key));
                subtasks.put(elem, subtask);
            }
            try {
                scope.join();
            } catch (InterruptedException ex) {
                return new HashMap<>();
            }
            Map<E, R> results = new HashMap<>();
            for (var elemToSubtask : subtasks.entrySet()) {
                var subtask = elemToSubtask.getValue();
                if (subtask.state() == StructuredTaskScope.Subtask.State.SUCCESS) {
                    results.put(elemToSubtask.getKey(), subtask.get());
                }
            }
            return results;
        }
    }*/

    private final static Map<Map, Map<Object, ReentrantLock>> MAP_KEY_LOCKS = new HashMap<>();
    private final static ReentrantLock MAP_LOCK = new ReentrantLock();

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
    public static <E,R> R computeIfAbsent(Map<E,R> map, E key, WaitingFunction<E,R> mapper) throws Exception {
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

    public static void clearLocks(Map map) {
        synchronized (map) {
            MAP_KEY_LOCKS.remove(map);
        }
    }
}
