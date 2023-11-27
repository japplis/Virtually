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

import java.util.List;
import java.util.Map;

import com.japplis.virtually.demo.shop.PriceService;
import com.japplis.virtually.demo.shop.Product;
import com.japplis.virtually.demo.shop.ShopFactory;
import com.japplis.virtually.scope.ListTaskScope;

import org.junit.jupiter.api.Test;

import com.japplis.virtually.scope.WaitingFunction;

/**
 *
 * @author Anthony Goubard - Japplis
 */
public class ListTaskScopeDemos {

    private PriceService priceService = PriceService.getInstance();

    @Test
    void listTaskScope() {
        List<Product> products = ShopFactory.createManyProducts(15_000);
        WaitingFunction<Product, Double> productToPrice = (Product p) -> priceService.retreivePrice(p.id());
        try (ListTaskScope<Product, Double> scope = new ListTaskScope(productToPrice)) {
            // scope.setDefaultValue(0);
            // scope.setFailOnException(true);
            for (Product product : products) {
                scope.convert(product);
            }
            Map<Product, Double> productWithPrices = scope.getResultsAsMap();
            List<Double> prices = scope.getResultsAsList();
            System.out.println("Size: " + productWithPrices.size() + " & " + prices.size());
        }
    }
}
