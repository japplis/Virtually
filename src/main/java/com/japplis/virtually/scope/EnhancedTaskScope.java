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

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.StructuredTaskScope;
import java.util.concurrent.ThreadFactory;

/**
 * A StructuredTaskScope with extra methods.
 *
 * @author Anthony Goubard - Japplis
 */
public class EnhancedTaskScope<T> extends StructuredTaskScope<T> {

    private Set<Subtask<? extends T>> criticalTasks = new HashSet<>();
    private Exception criticalException;

    public EnhancedTaskScope() {
    }

    public EnhancedTaskScope(String name, ThreadFactory factory) {
        super(name, factory);
    }

    public <U extends T> Subtask<U> forkCritical(Callable<? extends U> task) {
        Subtask<U> subtask = fork(task);
        criticalTasks.add(subtask);
        return subtask;
    }

    @Override
    protected void handleComplete(Subtask<? extends T> subtask) {
        super.handleComplete(subtask);
        if (subtask.state() == Subtask.State.FAILED && criticalTasks.contains(subtask)) {
            shutdown();
        }
    }

    public Exception getCriticalException() {
        return criticalException;
    }

    public <U extends T> Subtask<U> forkWithDefault(Callable<? extends U> task, U defaultValue) {
        Callable<? extends U> newTask = () -> {
            try {
                return task.call();
            } catch (Exception ex) {
                return defaultValue;
            }
        };
        Subtask<U> subtask = fork(newTask);
        return subtask;
    }
}
