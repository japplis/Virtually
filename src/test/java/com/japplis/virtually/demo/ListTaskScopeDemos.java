package com.japplis.virtually.demo;

import java.util.List;
import java.util.Map;

import com.japplis.virtually.demo.shop.PriceService;
import com.japplis.virtually.demo.shop.Product;
import com.japplis.virtually.demo.shop.ShopFactory;
import com.japplis.virtually.scope.ListTaskScope;

import org.junit.jupiter.api.Test;

import com.japplis.virtually.scope.CallableFunction;

public class ListTaskScopeDemos {

    private PriceService priceService = PriceService.getInstance();

    @Test
    void listTaskScope() {
        List<Product> products = ShopFactory.createManyProducts(15_000);
        CallableFunction<Product, Double> productToPrice = (Product p) -> priceService.retreivePrice(p.id());
        try (ListTaskScope<Product, Double> scope = new ListTaskScope(productToPrice)) {
            // scope.setDefaultValue(0);
            // scope.setFailOnException(true);
            // scope.setMaxConcurrentTasks(3_000);
            for (Product product : products) {
                scope.convert(product);
            }
            Map<Product, Double> productWithPrices = scope.getResultsAsMap();
            List<Double> prices = scope.getResultsAsList();
            System.out.println("Size: " + productWithPrices.size() + " & " + prices.size());
        }
    }
}
