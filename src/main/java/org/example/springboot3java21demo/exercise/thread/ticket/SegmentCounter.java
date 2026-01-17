package org.example.springboot3java21demo.exercise.thread.ticket;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 3.使用分段计数（最高性能）
 */
public class SegmentCounter {
    private final int target = 10000;
    private AtomicInteger globalCount = new AtomicInteger(0);

    public void countWithSegments(int threadCount) throws InterruptedException {
        Thread[] threads = new Thread[threadCount];
        int segmentSize = target / threadCount;

        for (int i = 0; i < threadCount; i++) {
            final int threadIndex = i;
            threads[i] = new Thread(() -> {
                int start = threadIndex * segmentSize + 1;
                int end = (threadIndex == threadCount - 1) ? target : (threadIndex + 1) * segmentSize;

                int localSum = 0;
                for (int j = start; j <= end; j++) {
                    localSum += j;
                }

                // 累加到全局计数器
                globalCount.addAndGet(localSum);
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

        System.out.println("最终结果: " + globalCount.get());
        System.out.println("耗时: " + (endTime - startTime) + "ms");
    }

    public static void main(String[] args) throws InterruptedException {
        SegmentCounter counter = new SegmentCounter();
        counter.countWithSegments(4); // 使用4个线程
    }
}
