package org.example.springboot3java21demo.exercise.dispatcher;

// 文件: Scheduler.java
// 说明：调度器核心类，封装 ScheduledExecutorService，实现任务的动态注册、取消与关闭

import org.example.springboot3java21demo.exercise.dispatcher.service.Task;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Scheduler {
    // ScheduledExecutorService 用于定时调度任务
    private final ScheduledExecutorService scheduledExecutor;
    // 保存任务名称与对应 ScheduledFuture 的映射，以便后续取消或查询
    private final ConcurrentMap<String, ScheduledFuture<?>> taskFutures;

    /**
     * 构造方法：创建固定大小的线程池，线程名称格式自定义
     *
     * @param poolSize 线程池大小
     */
    public Scheduler(int poolSize) {
        // 自定义线程工厂，为线程命名，以便调试与日志分析
        ThreadFactory threadFactory = new ThreadFactory() {
            private final AtomicInteger counter = new AtomicInteger(1);

            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                t.setName("Scheduler-Worker-" + counter.getAndIncrement());
                t.setDaemon(false); // 非守护线程，确保 JVM 不会因线程结束而退出
                return t;
            }
        };
        // 创建 ScheduledThreadPoolExecutor，并启用去除已取消任务的策略，避免内存泄露
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(poolSize, threadFactory);
        executor.setRemoveOnCancelPolicy(true);
        this.scheduledExecutor = executor;
        this.taskFutures = new ConcurrentHashMap<>();
    }

    /**
     * 以固定频率调度任务：任务首次延迟 initialDelay 执行，之后每隔 period 时间执行一次
     *
     * @param task         实现 Task 接口的任务
     * @param initialDelay 首次延迟时间，单位为 TimeUnit
     * @param period       执行间隔时间，单位为 TimeUnit
     * @param unit         时间单位
     * @throws IllegalArgumentException 若任务名称已存在，则抛出异常，避免重复调度
     */
    public void scheduleAtFixedRate(Task task, long initialDelay, long period, TimeUnit unit) {
        String name = task.getName();
        if (taskFutures.containsKey(name)) {
            throw new IllegalArgumentException("任务名称 '" + name + "' 已存在，无法重复调度");
        }
        // 将 Task 包装为 Runnable，以便执行并捕获异常
        Runnable wrapper = () -> {
            try {
                task.run();
            } catch (Exception e) {
                System.err.println("任务 '" + name + "' 执行异常：" + e.getMessage());
                e.printStackTrace();
            }
        };
        ScheduledFuture<?> future = scheduledExecutor.scheduleAtFixedRate(wrapper, initialDelay, period, unit);
        taskFutures.put(name, future);
        System.out.println("已注册固定频率任务：[" + name + "]，首次延迟：" + initialDelay + unit +
                "，执行间隔：" + period + unit);
    }

    /**
     * 以固定延迟调度任务：任务首次延迟 initialDelay 执行，之后每次执行完成后再延迟 delay 时间执行下一次
     *
     * @param task         实现 Task 接口的任务
     * @param initialDelay 首次延迟时间，单位为 TimeUnit
     * @param delay        执行完成后延迟时间，单位为 TimeUnit
     * @param unit         时间单位
     * @throws IllegalArgumentException 若任务名称已存在，则抛出异常，避免重复调度
     */
    public void scheduleWithFixedDelay(Task task, long initialDelay, long delay, TimeUnit unit) {
        String name = task.getName();
        if (taskFutures.containsKey(name)) {
            throw new IllegalArgumentException("任务名称 '" + name + "' 已存在，无法重复调度");
        }
        Runnable wrapper = () -> {
            try {
                task.run();
            } catch (Exception e) {
                System.err.println("任务 '" + name + "' 执行异常：" + e.getMessage());
                e.printStackTrace();
            }
        };
        ScheduledFuture<?> future = scheduledExecutor.scheduleWithFixedDelay(wrapper, initialDelay, delay, unit);
        taskFutures.put(name, future);
        System.out.println("已注册固定延迟任务：[" + name + "]，首次延迟：" + initialDelay + unit +
                "，执行延迟：" + delay + unit);
    }

    /**
     * 取消指定任务
     *
     * @param taskName              任务名称
     * @param mayInterruptIfRunning 是否中断正在运行的任务线程
     * @return 若任务存在且成功调用取消，则返回 true；否则返回 false
     */
    public boolean cancelTask(String taskName, boolean mayInterruptIfRunning) {
        ScheduledFuture<?> future = taskFutures.get(taskName);
        if (future == null) {
            System.err.println("尝试取消任务失败：未找到名称为 '" + taskName + "' 的任务");
            return false;
        }
        boolean cancelled = future.cancel(mayInterruptIfRunning);
        if (cancelled) {
            taskFutures.remove(taskName);
            System.out.println("任务 [" + taskName + "] 已取消");
        } else {
            System.err.println("任务 [" + taskName + "] 取消失败");
        }
        return cancelled;
    }

    /**
     * 查询当前调度中的所有任务名称
     *
     * @return 包含所有正在调度任务名称的 Set
     */
    public java.util.Set<String> getScheduledTaskNames() {
        return taskFutures.keySet();
    }

    /**
     * 关闭调度器，停止接受新任务，当前已提交的任务将根据参数决定是否立即中断或等待完成
     *
     * @param waitForTasksToComplete 若为 true，则等待当前正在运行的任务执行完成后再关闭；否则立即中断
     */
    public void shutdown(boolean waitForTasksToComplete) {
        System.out.println("调度器即将关闭，waitForTasksToComplete=" + waitForTasksToComplete);
        if (waitForTasksToComplete) {
            // 禁止提交新任务，等待已提交任务完成后关闭
            scheduledExecutor.shutdown();
            try {
                if (!scheduledExecutor.awaitTermination(60, TimeUnit.SECONDS)) {
                    System.err.println("等待超时，强制关闭调度器");
                    scheduledExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                System.err.println("等待过程中被中断，立即强制关闭");
                scheduledExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        } else {
            // 立即中断所有正在执行的任务
            scheduledExecutor.shutdownNow();
        }
        taskFutures.clear();
        System.out.println("调度器已关闭");
    }
}
