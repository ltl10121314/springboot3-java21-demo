package org.example.springboot3java21demo.exercise.thread.pool;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 自定义线程工厂
 * 为线程池中的线程设置统一的命名前缀，便于日志追踪
 */
public class CustomThreadFactory implements ThreadFactory {
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private final String namePrefix;

    public CustomThreadFactory(String namePrefix) {
        this.namePrefix = namePrefix;
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread t = new Thread(r, namePrefix + "-" + threadNumber.getAndIncrement());
        // 设置为非守护线程，确保任务能执行完毕
        t.setDaemon(false);
        // 设置合理的优先级
        t.setPriority(Thread.NORM_PRIORITY);
        return t;
    }
}