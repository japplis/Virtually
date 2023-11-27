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
package com.japplis.virtually.scope;

import java.util.*;
import java.util.concurrent.StructuredTaskScope;
import java.util.concurrent.ThreadFactory;

/**
 *
 * @author Anthony Goubard - Japplis
 */
public class ListTaskScope<E, R> extends StructuredTaskScope<R> {

    private final Map<E, Subtask<? extends R>> elemToSubtask = new LinkedHashMap<>();
    private final WaitingFunction<E, R> mapper;
    private R defaultValue;
    private boolean failOnException;
    private Throwable failedException;

    public ListTaskScope(WaitingFunction<E, R> mapper) {
        this.mapper = mapper;
    }

    public ListTaskScope(String name, ThreadFactory factory, WaitingFunction<E, R> mapper) {
        super(name, factory);
        this.mapper = mapper;
    }

    public R getDefaultValue() {
        return defaultValue;
    }

    /**
     * Sets a default value if the call fails. Note that <code>null</code> is not allowed as default value.
     * Also it's only used if <code>setFailOnException(true)</code> is not called.
     *
     * @param defaultValue
     */
    public void setDefaultValue(R defaultValue) {
        if (defaultValue == null) throw new IllegalArgumentException("null is not allowed as default value.");
        this.defaultValue = defaultValue;
    }

    public boolean isFailOnException() {
        return failOnException;
    }

    public void setFailOnException(boolean failOnException) {
        this.failOnException = failOnException;
    }

    /**
     * When failOnException is set returns the first exception that failed this scope.
     *
     * @return the exception or <code>null</code> if getFailOnException is false or no exception occurred.
     */
    public Throwable getFailedException() {
        return failedException;
    }

    public Subtask<? extends R> convert(E elem) {
        var subtask = fork(() -> mapper.call(elem));
        elemToSubtask.put(elem, subtask);
        return subtask;
    }

    @Override
    protected void handleComplete(Subtask<? extends R> subtask) {
        super.handleComplete(subtask);
        if (failOnException && subtask.state() == Subtask.State.FAILED) {
            if (failedException == null) failedException = subtask.exception();
            shutdown();
        }
    }

    public List<R> getResultsAsList() {
        return new ArrayList<>(getResultsAsMap().values());
    }

    public Map<E, R> getResultsAsMap() {
        try {
            join();
        } catch (InterruptedException ex) {
            return new HashMap<>();
        }
        ensureOwnerAndJoined();
        Map<E, R> results = new LinkedHashMap<>();
        for (var elemSubstack : elemToSubtask.entrySet()) {
            var subtask = elemSubstack.getValue();
            if (subtask.state() == Subtask.State.SUCCESS) {
                results.put(elemSubstack.getKey(), subtask.get());
            } else if (!failOnException && defaultValue != null) {
                results.put(elemSubstack.getKey(), defaultValue);
            }
        }
        return results;
    }
}
