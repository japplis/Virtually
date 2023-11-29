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
import java.util.function.Function;

import com.japplis.virtually.scope.CallableFunction;

/**
 * A set of utilities to convert lists in parallel in virtual threads.
 *
 * @author Anthony Goubard - Japplis
 */
public class ListConverter {

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
    }

    /**
     * Converts all the elements in the list or throw an Exception if a convertion failed.
     *
     * @param <E> the elements type
     * @param <R> the result type
     * @param elements the list of elements to convert
     * @param mapper the function to convert elements
     * @return the converted elements according to the mapper
     * @throws Exception if a mapping failed or it was interrupted or cancelled
     */
    public static <E, R> List<R> convertAll(List<E> elements, CallableFunction<E, R> mapper) throws Exception {
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
    public static <E, R> Map<E, R> convertAllToMap(List<E> elements, CallableFunction<E, R> mapper) throws Exception {
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
    }
}
