package org.example.springboot3java21demo.exercise.thread.forkjoin;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.TimeUnit;

/**
 * Fork/Join框架
 */
@Slf4j
public class Task extends RecursiveAction {

    Integer inIndex;

    List<String> inList = null;
    CountDownLatch inCountDownLatch = null;

    public Task() {
    }

    public Task(CountDownLatch countDownLatch, List<String> list, Integer index) {
        inCountDownLatch = countDownLatch;
        inList = list;
        inIndex = index;
    }

    @Override
    protected void compute() {
        try {
            if (inIndex == 2) {
//                int i = 1 / 0;
                System.out.println("hello");
            }
            Thread.sleep(2 * 1000);
            inList.add(String.valueOf(inIndex));
            log.info("输出:{}", inIndex);
        } catch (Exception e) {
            log.info("计算报错:{}", e.getMessage());
            throw new RuntimeException(e.getMessage());
        } finally {
            this.inCountDownLatch.countDown();
        }
    }

    public static void main(String[] args) {
        List<String> list = new ArrayList<>();
        int parallelThreadCount = 3 * Runtime.getRuntime().availableProcessors() + 2;
        int taskCount = 6;
        ForkJoinPool aloneForkJoinPool = new ForkJoinPool(parallelThreadCount);
        CountDownLatch countDownLatch = new CountDownLatch(taskCount);
        List<RecursiveAction> taskList = new ArrayList<>(taskCount);
        for (int m = 0; m < taskCount; m++) {
            Task recursiveAction = new Task(countDownLatch, list, m);
            taskList.add(recursiveAction);
            aloneForkJoinPool.submit(recursiveAction);
        }
        // 并行计算时，最多等待30min，避免线程无限等待出现故障
        try {
            boolean await = countDownLatch.await(20, TimeUnit.SECONDS);
            if (!await) {
                String format = String.format("并行计算已经计算了%d分钟,非正常计算结束", 30);
                throw new RuntimeException(format);
            }
            String format = String.format("单线程已经计算了%d分钟,非正常计算结束", 30);
            log.info("等待:{},{}", await, format);
        } catch (InterruptedException | RuntimeException ie) {
            log.error("线程异常：", ie);
            // 销毁动态线程池
            if (aloneForkJoinPool != null && !aloneForkJoinPool.isShutdown()) {
                aloneForkJoinPool.shutdown();
            }
            throw new RuntimeException(ie.getMessage());
        }
        for (RecursiveAction recursiveAction : taskList) {
            if (recursiveAction.isCompletedAbnormally()) {
                Throwable exception = recursiveAction.getException();
                log.info("外部拋错:{}", exception.getCause().getCause().getMessage());
                throw new RuntimeException(exception.getMessage());
            }
        }
        log.info("列表大小:{}", list);
    }
}
