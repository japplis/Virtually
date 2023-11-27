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

import com.japplis.virtually.CollectionUtils;
import com.japplis.virtually.demo.shop.PriceService;
import com.japplis.virtually.demo.shop.Product;
import com.japplis.virtually.demo.shop.ShopFactory;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
/**
 *
 * @author Anthony Goubard - Japplis
 */
class ListDemos {

    private PriceService priceService = PriceService.getInstance();

    @Test
    void convertDemo() {
        long start = System.currentTimeMillis();
        List<Product> products = ShopFactory.createManyProducts(20_000);
        List<Double> prices = CollectionUtils.convertList(products, p -> priceService.retreivePrice(p.id()));
        double totalPrice = prices.stream().mapToDouble(Double::doubleValue).sum();
        System.out.println("Total price: " + totalPrice + " in " + (System.currentTimeMillis() - start) + " ms.");
    }

    @Test
    void convertAllDemo() throws Exception {
        long start = System.currentTimeMillis();
        List<Product> products = ShopFactory.createManyProducts(5_000);
        List<Double> prices = CollectionUtils.convertAll(products, p -> priceService.retreivePrice(p.id()));
        double totalPrice = prices.stream().mapToDouble(Double::doubleValue).sum();
        System.out.println("Total price: " + totalPrice + " in " + (System.currentTimeMillis() - start) + " ms.");
    }

    @Test
    void convertAllDemoFailed() {
        List<Product> products = ShopFactory.createManyProducts(15_000);
        assertThrows(IllegalStateException.class, () -> {
            CollectionUtils.convertAll(products, p -> priceService.retreivePrice(p.id()));
        });
    }

    @Test
    void convertToMap() {
        List<Product> products = ShopFactory.createManyProducts(15_000);
        Map<Product, Double> productPrice = CollectionUtils.convertToMap(products, p -> priceService.retreivePrice(p.id()));
        assertEquals(10_000, productPrice.size());
        System.out.println("Price for product 5,000: " + productPrice.get(products.get(4_999)));
    }

    public void main() {
        convertDemo();
    }
}
