package org.example.springboot3java21demo.exercise.thread;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.*;

@Slf4j
public class ThreadTest2 {

    public static final ThreadFactory THREAD_FACTORY = new ThreadFactoryBuilder().setNameFormat("demo-pool-%d").build();
    public static final ExecutorService THREAD_POOL_EXECUTOR = new ThreadPoolExecutor(1, 20, 60L,
            TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(1000),
            THREAD_FACTORY
//            ,new ThreadPoolExecutor.CallerRunsPolicy()
    );

    @Test
    public void test3() {
        Set<String> staffIds = new HashSet<>();
        for (int i = 0; i < 10000; i++) {
            staffIds.add(i + "");
        }
        Set<String> payFileIds = new HashSet<>();
        for (int i = 0; i < 1000; i++) {
            payFileIds.add(i + "");
        }
        // 筛选符合逻辑的数据
        List<CompletableFuture> cfList = new ArrayList<>();
        for (String id : payFileIds) {
            log.error("mainThread-{}", id);
            CompletableFuture cf = CompletableFuture.runAsync(() -> {
                this.testThread1(new ArrayList<>(staffIds));
            }, THREAD_POOL_EXECUTOR);
            cfList.add(cf);
        }
        CompletableFuture.allOf(cfList.toArray(new CompletableFuture[0])).join();
    }

    public void testThread1(List<String> idList) {
        List<List<String>> partitions = Lists.partition(idList, 1000);
        List<CompletableFuture<List<Map<String, Object>>>> cfList = new ArrayList<>();
        for (List<String> staffIdList : partitions) {
            log.error("subThread-{}", staffIdList);
            CompletableFuture<List<Map<String, Object>>> cf = CompletableFuture.supplyAsync(() -> {
                try {
                    Thread.sleep(1000 * 10);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                return new ArrayList<>();
            }, THREAD_POOL_EXECUTOR);
            cfList.add(cf);
        }
        CompletableFuture.allOf(cfList.toArray(new CompletableFuture[0])).join();
    }

    @Test
    public void test2() {
        System.out.println("加了join");
//        threadTest(true);
        System.out.println("没加join");
        threadTest(false);
    }

    private void threadTest(Boolean flag) {
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            list.add(i);
        }
        System.out.println("开始==========");
        long start = System.currentTimeMillis();
        List<CompletableFuture<Integer>> ls = new ArrayList<>();
        for (Integer integer : list) {
            ls.add(CompletableFuture.supplyAsync(() -> {
                if (integer % 2 == 0) {
                    try {
                        Thread.sleep(1000 * 10);
//                        System.out.println("延迟排名：" + integer);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                } else {
//                    System.out.println("排名：" + integer);
                }
                return integer;
            }, THREAD_POOL_EXECUTOR));
        }
        if (flag) {
            CompletableFuture.allOf(ls.toArray(new CompletableFuture[0])).join();
        }
        List<Integer> integerList = new ArrayList<>();
        for (CompletableFuture<Integer> future : ls) {
            try {
                Integer resultList = future.get();
                integerList.add(resultList);
            } catch (Exception e) {
                log.info("exception:" + e.getMessage());
            }
        }
        System.out.println(integerList);
        long end = System.currentTimeMillis();
        System.out.println(end - start);
        System.out.println("结束============");
    }

    public static void main(String[] args) {
        log.error("main-start");
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                log.error("start");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                log.error("end");
            }
        };
        THREAD_POOL_EXECUTOR.execute(runnable);
        log.error("main-end");
        THREAD_POOL_EXECUTOR.shutdown();
    }

    @Test
    public void test() {
        log.info("start");
        List<CompletableFuture> cfList = new ArrayList<>();
        Vector<Integer> vector = new Vector<>();
        for (int i = 0; i < 10; i++) {
            int finalI = i;
            CompletableFuture cf = CompletableFuture.runAsync(() -> {
                int i1 = getException(finalI);
                vector.add(i1);
            });
            cfList.add(cf);
        }
        CompletableFuture.allOf(cfList.toArray(new CompletableFuture[0])).join();
        log.info(vector.toString());
        log.info("end");
    }

    private int getException(int i) {
        if (i % 2 == 0) {
            try {
                Thread.sleep(1000 * 20);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
//            throw new RuntimeException("error.");
        }
        log.info("number-{}", i);
        return i;
    }
}
