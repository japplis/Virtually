package com.japplis.virtually.demo;

import java.util.ArrayList;
import java.util.List;

import com.japplis.virtually.Threads;
import com.japplis.virtually.sync.BlockLock;

import org.junit.jupiter.api.Test;

public class BlockLockDemos {

    private int index;
    private BlockLock blockLock = new BlockLock();

    @Test
    void tryBlock() {
        List<Thread> threads = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            int loopIndex = i;
            Thread thread = Thread.startVirtualThread(() -> printIndex(loopIndex));
            threads.add(thread);
        }
        Threads.waitForAll(threads);
    }

    private void printIndex(int loopIndex) {
        try (var lock = blockLock.lockBlock()) {
            index = loopIndex;
            Threads.sleep(100);
            System.out.println(index + " -> " + loopIndex);
        }
    }

    private void printIndexUsingReentrantLock(int loopIndex) {
        blockLock.lock();
        try {
            index = loopIndex;
            Threads.sleep(100);
            System.out.println(index + " -> " + loopIndex);
        } finally {
            blockLock.unlock();
        }
    }
}
