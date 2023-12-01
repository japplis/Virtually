package com.japplis.virtually.demo;

import java.util.*;
import java.util.stream.IntStream;

import com.japplis.virtually.Maps;
import com.japplis.virtually.Threads;
import com.japplis.virtually.demo.shop.PriceService;
import com.japplis.virtually.demo.shop.Product;
import com.japplis.virtually.demo.shop.ShopFactory;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MapDemos {
    private PriceService priceService = PriceService.getInstance();

    @Test
    void computeIfAbsent() {
        priceService.resetPriceCallCount();
        List<Product> products = ShopFactory.createManyProducts(5_000);
        Map<Product, Double> prices = new HashMap<>(); //
        int[] randomNumbers = new Random().ints(0, 5000).limit(15_000).toArray();
        long uniqueNumbersCount = IntStream.of(randomNumbers).distinct().count();
        List<Thread> startedThreads = new ArrayList<>();
        for (int number : randomNumbers) {
            Product product = products.get(number);
            Thread priceThread = Thread.startVirtualThread(() -> {
                try {
                    Maps.computeIfAbsent(prices, product, priceService::retreivePrice);
                } catch (Exception ex) {
                    System.err.println("Failed for product " + number + "; " + ex.getMessage());
                }
            });
            startedThreads.add(priceThread);
        }
        Threads.waitForAll(startedThreads);
        System.out.println("Price service \"Network\" calls: " + priceService.getPriceCallCount());
        System.out.println("Number of prices: " + prices.size());
        System.out.println("Number of unique products: " + uniqueNumbersCount);
        assertEquals(priceService.getPriceCallCount(), prices.size());
        assertEquals(uniqueNumbersCount, prices.size());
        //System.out.println("locks: " + CollectionUtils.MAP_KEY_LOCKS.get(prices).size());
    }
}
