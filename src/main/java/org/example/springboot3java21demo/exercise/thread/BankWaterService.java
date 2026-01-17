package org.example.springboot3java21demo.exercise.thread;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * 银行流水处理服务类
 */
public class BankWaterService implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(BankWaterService.class);

    /**
     * 创建4个屏障，处理完之后执行当前类的run方法
     */
    private CyclicBarrier cyclicBarrier = new CyclicBarrier(4, this);

    /**
     * 创建4个线程处理，分别执行自己的任务
     */
    private Executor executor = Executors.newFixedThreadPool(4);

    /**
     * 汇总计算
     */
    private ConcurrentHashMap<String, Integer> sheetBankWaterCount = new ConcurrentHashMap<>();

    public void count() {
        for (int i = 0; i < 4; i++) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    //处理自己的逻辑
                    sheetBankWaterCount.put(Thread.currentThread().getName(), 1);
                    try {
                        //处理完自己的逻辑，执行await方法
                        cyclicBarrier.await();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    @Override
    public void run() {
        int result = 0;
        for (Integer count : sheetBankWaterCount.values()) {
            result += count;
        }
        sheetBankWaterCount.put("result", result);
        LOGGER.info("银行流水总数为：{}", sheetBankWaterCount.get("result"));

    }

    public static void main(String[] args) {
        BankWaterService bankWaterService = new BankWaterService();
        bankWaterService.count();
    }
}
