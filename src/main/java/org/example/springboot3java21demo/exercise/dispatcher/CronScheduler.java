package org.example.springboot3java21demo.exercise.dispatcher;
// 文件: CronScheduler.java
// 说明：示例基于 CronExpressionParser 的简易 Cron 调度器，定期扫描注册的任务并判断是否执行


import org.example.springboot3java21demo.exercise.dispatcher.service.Task;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.*;

public class CronScheduler {
    // 存储任务名称与对应 CronExpressionParser 的映射
    private final ConcurrentMap<String, CronExpressionParser> cronExpressions;
    // 存储任务名称与 Task 对象的映射
    private final ConcurrentMap<String, Task> cronTasks;
    // 调度线程池，仅维护单线程，用于定时扫描判断是否触发 Cron 任务
    private final ScheduledExecutorService cronExecutor;
    private final ScheduledFuture<?> cronScannerFuture;

    /**
     * 构造方法：启动 Cron 调度扫描线程，每秒执行一次扫描
     */
    public CronScheduler() {
        this.cronExpressions = new ConcurrentHashMap<>();
        this.cronTasks = new ConcurrentHashMap<>();
        this.cronExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r);
            t.setName("Cron-Scheduler-Scanner");
            t.setDaemon(false);
            return t;
        });
        // 每秒扫描一次注册的所有 Cron 任务
        this.cronScannerFuture = cronExecutor.scheduleAtFixedRate(this::scanAndRun,
                1, 1, TimeUnit.SECONDS);
        System.out.println("CronScheduler 已启动，每秒扫描一次任务");
    }

    /**
     * 注册 Cron 任务
     *
     * @param expression 三段式 Cron 表达式："秒 分 时"
     * @param task       实现 Task 接口的任务
     * @throws IllegalArgumentException 若任务名称已存在，则抛出异常
     */
    public void scheduleWithCron(String expression, Task task) {
        String name = task.getName();
        if (cronTasks.containsKey(name)) {
            throw new IllegalArgumentException("Cron 任务 '" + name + "' 已存在，无法重复注册");
        }
        CronExpressionParser parser = new CronExpressionParser(expression);
        cronExpressions.put(name, parser);
        cronTasks.put(name, task);
        System.out.println("已注册 Cron 任务：[" + name + "]，表达式：" + expression);
    }

    /**
     * 扫描所有注册的 Cron 任务，判断是否在当前时间触发
     */
    private void scanAndRun() {
        LocalDateTime now = LocalDateTime.now();
        for (Map.Entry<String, CronExpressionParser> entry : cronExpressions.entrySet()) {
            String name = entry.getKey();
            CronExpressionParser parser = entry.getValue();
            if (parser.matches(now)) {
                Task task = cronTasks.get(name);
                if (task != null) {
                    // 异步执行匹配的任务
                    CompletableFuture.runAsync(() -> {
                        try {
                            task.run();
                        } catch (Exception e) {
                            System.err.println("Cron 任务 '" + name + "' 执行异常：" + e.getMessage());
                            e.printStackTrace();
                        }
                    });
                }
            }
        }
    }

    /**
     * 取消指定 Cron 任务
     *
     * @param taskName 任务名称
     * @return 若任务存在并移除成功，则返回 true；否则返回 false
     */
    public boolean cancelCronTask(String taskName) {
        CronExpressionParser parser = cronExpressions.remove(taskName);
        Task task = cronTasks.remove(taskName);
        if (parser != null && task != null) {
            System.out.println("Cron 任务 [" + taskName + "] 已取消");
            return true;
        } else {
            System.err.println("未找到 Cron 任务 [" + taskName + "]，取消失败");
            return false;
        }
    }

    /**
     * 关闭 Cron 调度器，停止扫描线程
     */
    public void shutdown() {
        System.out.println("CronScheduler 即将关闭");
        cronScannerFuture.cancel(true);
        cronExecutor.shutdownNow();
        cronExpressions.clear();
        cronTasks.clear();
        System.out.println("CronScheduler 已关闭");
    }
}
