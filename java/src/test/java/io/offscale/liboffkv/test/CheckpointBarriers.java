package io.offscale.liboffkv.test;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;

public class CheckpointBarriers<T> {
    private final int n;
    private ConcurrentMap<T, CountDownLatch> barriers = new ConcurrentHashMap<>();

    public CheckpointBarriers(int numThreads) {
        n = numThreads;
    }

    public void point(T key) throws InterruptedException {
        CountDownLatch latch = barriers.computeIfAbsent(key, (k) -> new CountDownLatch(n));
        latch.countDown();
        latch.await();
    }

    public int reached(T key) {
        CountDownLatch latch = barriers.get(key);
        if (latch == null)
            return 0;

        return (int) (n - latch.getCount());
    }
}
