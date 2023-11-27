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

import java.util.concurrent.locks.ReentrantLock;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

/**
 *
 * @author Anthony Goubard - Japplis
 */
@Aspect
public class SynchronizedAspect {

    @Around("@annotation(com.japplis.virtually.sync.Synchronized) && execution(* *(..))")
    public Object executeSynchronized(ProceedingJoinPoint pjp) throws Throwable {
        ReentrantLock lock = SyncUtils.lock(pjp.getThis());
        try {
            return pjp.proceed();
        } finally {
            lock.unlock();
        }
    }

    @Around("@annotation(com.japplis.virtually.sync.SynchronizedMethod) && execution(* *(..))")
    public Object executeSynchronizedMethod(ProceedingJoinPoint pjp) throws Throwable {
        ReentrantLock lock = SyncUtils.lock(pjp.getSignature().toLongString());
        try {
            return pjp.proceed();
        } finally {
            lock.unlock();
        }
    }
}
