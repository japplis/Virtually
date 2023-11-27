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

import java.lang.annotation.*;

/**
 * Annotation that specifies that the method can only be accessed by one thread at a time.
 * This annotation uses ReentrantLock that avoids pinning the virtual thread to the platform thread.
 *
 * @author Anthony Goubard - Japplis
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SynchronizedMethod {

}
