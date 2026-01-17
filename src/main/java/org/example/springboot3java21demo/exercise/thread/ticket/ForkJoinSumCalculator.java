package org.example.springboot3java21demo.exercise.thread.ticket;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

/**
 * 方法5：使用ForkJoin框架
 */
public class ForkJoinSumCalculator extends RecursiveTask<Integer> {
    private static final int THRESHOLD = 1000;
    private final int start;
    private final int end;

    public ForkJoinSumCalculator(int start, int end) {
        this.start = start;
        this.end = end;
    }

    @Override
    protected Integer compute() {
        int length = end - start;

        if (length <= THRESHOLD) {
            return computeDirectly();
        }

        int mid = start + length / 2;
        ForkJoinSumCalculator leftTask = new ForkJoinSumCalculator(start, mid);
        ForkJoinSumCalculator rightTask = new ForkJoinSumCalculator(mid + 1, end);

        leftTask.fork();
        int rightResult = rightTask.compute();
        int leftResult = leftTask.join();

        return leftResult + rightResult;
    }

    private int computeDirectly() {
        int sum = 0;
        for (int i = start; i <= end; i++) {
            sum += i;
        }
        return sum;
    }

    public static void main(String[] args) {
        ForkJoinPool pool = new ForkJoinPool();
        ForkJoinSumCalculator task = new ForkJoinSumCalculator(1, 10000);

        long startTime = System.currentTimeMillis();
        int result = pool.invoke(task);
        long endTime = System.currentTimeMillis();

        System.out.println("ForkJoin计算结果: " + result);
        System.out.println("公式验证: " + (10000 * 10001 / 2));
        System.out.println("耗时: " + (endTime - startTime) + "ms");

        pool.shutdown();
    }
}
