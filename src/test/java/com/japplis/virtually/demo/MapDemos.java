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
package com.japplis.virtually.demo;

import java.util.*;
import java.util.stream.IntStream;

import com.japplis.virtually.CollectionUtils;
import com.japplis.virtually.Threads;
import com.japplis.virtually.demo.shop.PriceService;
import com.japplis.virtually.demo.shop.Product;
import com.japplis.virtually.demo.shop.ShopFactory;

import org.junit.jupiter.api.Test;

/**
 *
 * @author Anthony Goubard - Japplis
 */
public class MapDemos {
    private PriceService priceService = PriceService.getInstance();

    @Test
    void computeIfAbsent() {
        priceService.resetPriceCallCount();
        List<Product> products = ShopFactory.createManyProducts(5_000);
        Map<Product, Double> prices = new HashMap<>(); //
        int[] randomNumbers = new Random().ints(0, 5000).limit(15_000).toArray();
        List<Thread> startedThreads = new ArrayList<>();
        for (int number : randomNumbers) {
            Product product = products.get(number);
            Thread priceThread = Thread.startVirtualThread(() -> {
                try {
                    CollectionUtils.computeIfAbsent(prices, product, p -> priceService.retreivePrice(p.id()));
                } catch (Exception ex) {
                    System.err.println("Failed for product " + number + "; " + ex.getMessage());
                }
            });
            startedThreads.add(priceThread);
        }
        Threads.waitForAll(startedThreads);
        System.out.println("Price service \"Network\" calls: " + priceService.getPriceCallCount());
        System.out.println("Number of prices: " + prices.size());
        System.out.println("Number of unique products: " + IntStream.of(randomNumbers).distinct().count());
        //System.out.println("locks: " + CollectionUtils.MAP_KEY_LOCKS.get(prices).size());
    }
}
