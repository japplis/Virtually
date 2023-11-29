package com.japplis.virtually.demo;


import com.japplis.virtually.ThreadBucket;
import com.japplis.virtually.Threads;

import org.junit.jupiter.api.Test;

import static com.japplis.virtually.sync.SyncUtils.*;

class SyncUtilsDemos {
    private int index;
    private Object lock = new Object();

    @Test
    void runSynchronizedDemo() {
        ThreadBucket threads = new ThreadBucket("runSynchronized");
        for (int i = 1; i <= 10; i++) {
            int loopIndex = i;
            threads.startVitualThread(() -> printIndex(loopIndex));
        }
        threads.waitForAll();
    }

    private void printIndex(int loopIndex) {
        runSynchronized(() -> {
            index = loopIndex;
            Threads.sleep(100);
            System.out.println(index + " -> " + loopIndex);
            return null;
        });
    }

    private void printIndexLock(int loopIndex) {
        runSynchronized(lock, () -> {
            index = loopIndex;
            Threads.sleep(100);
            System.out.println(index + " -> " + loopIndex);
            return null;
        });
    }

    private void printIndexUnlocked(int loopIndex) {
        index = loopIndex;
        Threads.sleep(100);
        System.out.println(index + " -> " + loopIndex);
    }
}
