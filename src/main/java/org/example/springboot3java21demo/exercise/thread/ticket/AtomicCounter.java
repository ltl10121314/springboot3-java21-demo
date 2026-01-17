package org.example.springboot3java21demo.exercise.thread.ticket;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 2.使用AtomicInteger（高性能）
 */
public class AtomicCounter {
    private AtomicInteger count = new AtomicInteger(0);
    private final int target = 10000;

    public void increment() {
        while (true) {
            int current = count.get();
            if (current >= target) {
                break;
            }
            if (count.compareAndSet(current, current + 1)) {
                break;
            }
        }
    }

    public int getCount() {
        return count.get();
    }

    public static void main(String[] args) throws InterruptedException {
        AtomicCounter counter = new AtomicCounter();

        // 创建10个线程
        Thread[] threads = new Thread[10];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(() -> {
                while (counter.getCount() < counter.target) {
                    counter.increment();
                }
            });
        }

        long startTime = System.currentTimeMillis();
        for (Thread thread : threads) {
            thread.start();
        }

        for (Thread thread : threads) {
            thread.join();
        }
        long endTime = System.currentTimeMillis();

        System.out.println("最终结果: " + counter.getCount());
        System.out.println("耗时: " + (endTime - startTime) + "ms");
    }
}
