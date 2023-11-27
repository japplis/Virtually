# Virtually - Make your code Java virtual threads friendly

## Introduction
### Virtually is a library that contains classes to easily migrate code to be more virtual thread friendly. One of the goal is to do it with the less boilingplate code.

For workable demo of the API go to src/test/com/japplis/virtually/demo

At the moment, most of the methods requires `--enable-preview` JVM start-up parameter.
Also note that this is early days for this library, so backward compatibility is not guaranteed.

One of the problem is **pinning virtual threads** to carrier/platform thread.

This library provides many tools to migrate to a more virtual thread friendly code:
* Utility methods and annotations to replace the `synchronized` keyword 
* Utility methods to convert list elements in parallel using virtual threads
* Replacement methods that don't pin the virtual thread
* Classes to execute multiple tasks in virtual threads

For full demo code go to [Demo directory](src/test/java/com/japplis/virtually/demo)

## Synchronized
Synchronized code is pinning the virtual thead to the platform/carrier thread, so it should be avoided around I/O operation and replace with ReentrantLock for example.

```java
import com.japplis.virtually.sync.BlockLock;

BlockLock blockLock = new BlockLock();
void main() {
    // BlockLock is an AutoCloseable ReentrantLock
    try (var _ = blockLock.lockBlock()) { 
        // Synchronized block with a ReentrantLock
    }
}
```

```java
import static com.japplis.virtually.sync.SyncUtils.*;

void main() {
    String text = runSynchronized(() -> {
        // Synchronized block on the calling class
        return "test";
    });
    runSynchronized(this, () -> { // 'this' can be replaced with any object (also a ReentrantLock)
        // Synchronized block with a ReentrantLock
    });
    callSynchronized(this, () -> {
        // Synchronized block for Callable that may throw an exception
    });
}
```

## Collections
```java
import static com.japplis.virtually.CollectionUtils.*;

void main() throws Exception {
    // convert a list to another one using one virtual thread per element
    List<Double> prices = convertAll(products, p -> priceService.retreivePrice(p.id()));
    // Get per product the price
    Map<Product, Double> productPrice = convertToMap(products, p -> priceService.retreivePrice(p.id()));
    // Get price for other products if not already in the map
    computeIfAbsent(productPrice, newProduct, p -> priceService.retreivePrice(p.id()));
}
```

## Task scopes

```java
import static com.japplis.virtually.scope.ListTaskScope;

void main() throws Exception {
    // A WaitingFunction is a Function that can throw an exception
    WaitingFunction<Product, Double> productToPrice = (Product p) -> priceService.retreivePrice(p.id());
    try (ListTaskScope<Product, Double> scope = new ListTaskScope(productToPrice)) {
        // scope.setDefaultValue(0);
        // scope.setFailOnException(true);
        for (Product product : products) {
            scope.convert(product);
        }
        // getting the result will call scope.join()
        Map<Product, Double> productWithPrices = scope.getResultsAsMap();
    }
}
```

## Annotations
```java
import com.japplis.virtually.sync.Synchronized;

@Synchronized // similar to synchronized keywork but with ReentrantLock, requires AspectJ library
void doSomethingSynchronized() {
    // do stuff
}

@SynchronizedMethod // synchronized with ReentrantLock to avoid multiple threads to enter this method at the same time, requires AspectJ library
void doSomethingElseSynchronized() {
    // do stuff
}

```
