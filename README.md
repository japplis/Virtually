# Virtually - Make your code Java virtual threads friendly

## Introduction
### Virtually is a library that contains classes to ease the migration of code to be more virtual threads friendly. One of the goal is to do it with the less boilingplate code.

For workable full demo code of the API, go to [Demo directory](src/test/java/com/japplis/virtually/demo)

This library provides many tools to migrate to a more virtual threads friendly code:
* Utility methods and annotations to replace the `synchronized` keyword 
* Utility methods to convert list elements in parallel using virtual threads
* Classes to execute multiple tasks in virtual threads

Also note that this is early days for this library, so backward compatibility is not guaranteed.

## Why virtual threads

| For Managers  | For Developers |
| ------------- | -------------- |
| Less memory needed (save costs) | Easy to debug |
| Accept more requests per server | Readable stack trace |
| More memory for caching (faster response  time) | Easier to develop with |
| Apply a bit of all of the above | Good pretext to migrate to Java 21 |

## Maven
```xml
<dependency>
  <groupId>com.japplis</groupId>
  <artifactId>virtually</artifactId>
  <version>0.1</version>
</dependency>
```
[Gradle & more](https://mvnrepository.com/artifact/com.japplis/virtually/0.1)

## Synchronized
Synchronized code is pinning the virtual thead to the platform/carrier thread, so it should be avoided around I/O operation and replace with ReentrantLock for example.

```java
import com.japplis.virtually.sync.BlockLock;

BlockLock blockLock = new BlockLock();
void main() {
    // BlockLock is an AutoCloseable ReentrantLock
    try (var sync = blockLock.lockBlock()) { 
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
    callSynchronized(this, () -> { // will rethrow the Exception of the Callable lambda
        // Synchronized block for Callable that may throw an exception
    });
}
```

## Collections
```java
import static com.japplis.virtually.ListConverter.*;
import static com.japplis.virtually.MapUtils.*;

void main() throws Exception {
    // convert a list to another one using one virtual thread per element
    List<Double> prices = convertAll(products, priceService::retreivePrice);
    // Get per product the price
    Map<Product, Double> productPrice = convertToMap(products, priceService::retreivePrice);
    // Get price for other products if not already in the map
    computeIfAbsent(productPrice, newProduct, priceService::retreivePrice);
}
```

## Task scopes
At the moment (JDK 21), the task scopes require `--enable-preview` JVM start-up parameter.

```java
import static com.japplis.virtually.scope.ListTaskScope;

void main() throws Exception {
    // A CallableFunction is a Function that can throw an exception
    CallableFunction<Product, Double> productToPrice = (Product p) -> priceService.retreivePrice(p.id());
    try (ListTaskScope<Product, Double> scope = new ListTaskScope(productToPrice)) {
        // scope.setDefaultValue(0);
        // scope.setFailOnException(true);
        // scope.setMaxConcurrentTasks(10_000);
        // scope.setMaxConsecutiveFails(50);
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
import com.japplis.virtually.sync.*;

@Synchronized // similar to synchronized keyword but with ReentrantLock, requires AspectJ library
void doSomethingSynchronized() {
    // do stuff
}

@SynchronizedMethod // synchronized with ReentrantLock to avoid multiple threads to enter this method at the same time, requires AspectJ library
void doSomethingElseSynchronized() {
    // do stuff
}

```

## Libraries

Here is a list of frameworks and libraries that are virtual-threads friendly

| Name              | Version | Remark |
| ----------------- | ------- | ------ |
| Spring Boot       |   3.2.0 | spring.threads.virtual.enabled=true |
| Quarkus           |   3.4.0 | [@RunOnVirtualThread](https://quarkus.io/guides/virtual-threads) |
| Micronaut         |   4.0.0 | @Executes(BLOCKING) |
| Tomcat            |      11 | <Connector ... useVirtualThreads="true" /> |
| Jettty            |      12 | [Details](https://webtide.com/jetty-12-virtual-threads-support/) |
| Helidon           |   4.0.0 | [Helidon Níma](https://helidon.io/nima) |
| pgjdbc (Postgres) |  42.6.0 |  |
| Oracle driver     |     21c |  |
| MariaDB connector |   3.3.0 |  |


## Sponsors
<a href="https://www.antcommander.com/">![Ant Commander Pro Logo](https://www.antcommander.com/images/AntCommanderProSponsor100.png)</a>

This open-source project is sponsored by [Ant Commander Pro File Manager](https://www.antcommander.com/). A professional file manager for Windows, MacOS and Linux for developers and more.

You can also [hire me](mailto:anthony.goubard@japplis.com) to help you migrate to virtual threads.