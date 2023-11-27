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
package com.japplis.virtually.demo.shop;

import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author Anthony Goubard - Japplis
 */
public class PriceService {

    private static PriceService INSTANCE;
    private static AtomicInteger CALL_COUNTER = new AtomicInteger();

    private PriceService() {
    }

    public synchronized static PriceService getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new PriceService();
        }
        return INSTANCE;
    }

    public double retreivePrice(int productId) {
        try {
            Thread.sleep(500);
            CALL_COUNTER.incrementAndGet();
        } catch (InterruptedException ex) {
        }
        double price = productId / 100.0;
        if (price > 100) throw new IllegalStateException("Too expensive!!!");
        return price;
    }

    public int getPriceCallCount() {
        return CALL_COUNTER.get();
    }

    public void resetPriceCallCount() {
        CALL_COUNTER.set(0);
    }
}
