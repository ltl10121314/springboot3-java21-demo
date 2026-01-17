package org.example.springboot3java21demo.exercise.thread.ticket;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * 方法4：使用ExecutorService和Callable
 */
public class ExecutorServiceCounter {
    private final int target = 10000;
    private final int threadCount = 4;

    public void calculateSum() throws InterruptedException, ExecutionException {
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        List<Future<Integer>> futures = new ArrayList<>();

        int segmentSize = target / threadCount;

        // 提交任务
        for (int i = 0; i < threadCount; i++) {
            final int start = i * segmentSize + 1;
            final int end = (i == threadCount - 1) ? target : (i + 1) * segmentSize;

            futures.add(executor.submit(() -> {
                int sum = 0;
                for (int j = start; j <= end; j++) {
                    sum += j;
                }
                return sum;
            }));
        }

        // 收集结果
        int totalSum = 0;
        for (Future<Integer> future : futures) {
            totalSum += future.get();
        }

        executor.shutdown();

        System.out.println("1到10000的和: " + totalSum);
        System.out.println("公式验证: " + (target * (target + 1) / 2));
    }

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        ExecutorServiceCounter counter = new ExecutorServiceCounter();
        counter.calculateSum();
    }
}
