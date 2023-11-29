package com.japplis.virtually.demo;

import java.util.List;
import java.util.Map;

import com.japplis.virtually.ListConverter;
import com.japplis.virtually.demo.shop.PriceService;
import com.japplis.virtually.demo.shop.Product;
import com.japplis.virtually.demo.shop.ShopFactory;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ListDemos {

    private PriceService priceService = PriceService.getInstance();

    @Test
    void convertDemo() {
        long start = System.currentTimeMillis();
        List<Product> products = ShopFactory.createManyProducts(20_000);
        List<Double> prices = ListConverter.convertList(products, p -> priceService.retreivePrice(p.id()));
        double totalPrice = prices.stream().mapToDouble(Double::doubleValue).sum();
        System.out.println("Total price: " + totalPrice + " in " + (System.currentTimeMillis() - start) + " ms.");
    }

    @Test
    void convertAllDemo() throws Exception {
        long start = System.currentTimeMillis();
        List<Product> products = ShopFactory.createManyProducts(5_000);
        List<Double> prices = ListConverter.convertAll(products, p -> priceService.retreivePrice(p.id()));
        double totalPrice = prices.stream().mapToDouble(Double::doubleValue).sum();
        System.out.println("Total price: " + totalPrice + " in " + (System.currentTimeMillis() - start) + " ms.");
    }

    @Test
    void convertAllDemoFailed() {
        List<Product> products = ShopFactory.createManyProducts(15_000);
        assertThrows(IllegalStateException.class, () -> {
            ListConverter.convertAll(products, p -> priceService.retreivePrice(p.id()));
        });
    }

    @Test
    void convertToMap() {
        List<Product> products = ShopFactory.createManyProducts(15_000);
        Map<Product, Double> productPrice = ListConverter.convertToMap(products, p -> priceService.retreivePrice(p.id()));
        assertEquals(10_000, productPrice.size());
        System.out.println("Price for product 5,000: " + productPrice.get(products.get(4_999)));
    }

    public void main() {
        convertDemo();
    }
}
