package org.example.springboot3java21demo.exercise.thread.ticket;

import java.util.Random;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

// 继承RecursiveAction
public class ArrayProcessor extends RecursiveAction {
    private static final int THRESHOLD = 1000; // 任务拆分阈值
    private int[] array;
    private int start;
    private int end;

    public ArrayProcessor(int[] array, int start, int end) {
        this.array = array;
        this.start = start;
        this.end = end;
    }

    @Override
    protected void compute() {
        // 如果任务足够小，直接执行
        if (end - start <= THRESHOLD) {
            processDirectly();
        } else {
            // 拆分任务
            int mid = (start + end) / 2;
            ArrayProcessor leftTask = new ArrayProcessor(array, start, mid);
            ArrayProcessor rightTask = new ArrayProcessor(array, mid, end);

            // 并行执行子任务
            invokeAll(leftTask, rightTask);
        }
    }

    private void processDirectly() {
        // 实际的处理逻辑：这里将数组元素乘以2
        for (int i = start; i < end; i++) {
            array[i] = array[i] * 2;
        }
    }

    // 测试
    public static void main(String[] args) {
        // 创建一个大数组
        int size = 10000;
        int[] array = new int[size];
        Random random = new Random();

        // 初始化数组
        for (int i = 0; i < size; i++) {
            array[i] = random.nextInt(100);
        }

        System.out.println("原始数组前10个元素:");
        for (int i = 0; i < 10; i++) {
            System.out.print(array[i] + " ");
        }
        System.out.println();

        // 创建ForkJoinPool
        ForkJoinPool pool = new ForkJoinPool();

        // 创建任务
        ArrayProcessor task = new ArrayProcessor(array, 0, array.length);

        // 执行任务
        pool.invoke(task);

        System.out.println("处理后的数组前10个元素:");
        for (int i = 0; i < 10; i++) {
            System.out.print(array[i] + " ");
        }
        System.out.println();

        pool.shutdown();
    }
}
