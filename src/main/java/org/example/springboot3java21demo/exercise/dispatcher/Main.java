package org.example.springboot3java21demo.exercise.dispatcher;

// 文件: Main.java
// 说明：演示如何使用 Scheduler 与 CronScheduler 进行任务调度

import org.example.springboot3java21demo.exercise.dispatcher.service.Task;
import org.example.springboot3java21demo.exercise.dispatcher.service.impl.SimpleTask;

import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        // =========================
        // 示例一：使用 Scheduler 调度固定频率与固定延迟任务
        // =========================
        Scheduler scheduler = new Scheduler(4); // 创建线程池大小为 4 的调度器

        // 创建两个示例任务
        Task taskA = new SimpleTask("TaskA", "任务 A 每 5 秒执行一次");
        Task taskB = new SimpleTask("TaskB", "任务 B 延迟 3 秒执行，执行完成后再延迟 7 秒执行下次");

        // 注册固定频率任务：TaskA 首次延迟 1 秒，之后每 5 秒执行一次
        scheduler.scheduleAtFixedRate(taskA, 1, 5, TimeUnit.SECONDS);

        // 注册固定延迟任务：TaskB 首次延迟 3 秒，之后执行完成再延迟 7 秒执行
        scheduler.scheduleWithFixedDelay(taskB, 3, 7, TimeUnit.SECONDS);

        // 让主线程睡眠 20 秒，以便观察前两个任务的输出
        Thread.sleep(20000);

        // 取消 TaskA
        scheduler.cancelTask("TaskA", false);

        // 让主线程继续睡眠 15 秒，以便观察 TaskB 继续执行
        Thread.sleep(15000);

        // 关闭调度器，等待 TaskB 当前执行完成后关闭
        scheduler.shutdown(true);

        // =========================
        // 示例二：使用 CronScheduler 注册 Cron 表达式任务（每分钟示例）
        // =========================
        CronScheduler cronScheduler = new CronScheduler();

        // 创建一个每隔 10 秒执行一次的 Cron 任务（注意：示例中秒域用 "*/10"，也可扩展到分钟、小时）
        Task cronTask = new SimpleTask("CronTask", "每 10 秒执行一次的 Cron 任务");
        cronScheduler.scheduleWithCron("*/10 * *", cronTask);

        // 让主线程睡眠 35 秒，以便观察 Cron 任务的执行（大约会执行 3 次）
        Thread.sleep(35000);

        // 取消 Cron 任务
        cronScheduler.cancelCronTask("CronTask");

        // 关闭 Cron 调度器
        cronScheduler.shutdown();
    }
}
