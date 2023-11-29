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
import java.util.concurrent.ThreadFactory;

/**
 * A StructuredTaskScope specialized for lists mapping.
 *
 * @author Anthony Goubard - Japplis
 */
public class ListTaskScope<E, R> extends EnhancedTaskScope<R> {

    private final Map<E, Subtask<? extends R>> elemToSubtask = new LinkedHashMap<>();
    private final CallableFunction<E, R> mapper;

    public ListTaskScope(CallableFunction<E, R> mapper) {
        this.mapper = mapper;
    }

    public ListTaskScope(String name, ThreadFactory factory, CallableFunction<E, R> mapper) {
        super(name, factory);
        this.mapper = mapper;
    }

    public Subtask<? extends R> convert(E elem) {
        var subtask = fork(() -> mapper.call(elem));
        elemToSubtask.put(elem, subtask);
        return subtask;
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
            } else if (getException() != null && getDefaultValue() != null) {
                results.put(elemSubstack.getKey(), getDefaultValue());
            }
        }
        return results;
    }
}
