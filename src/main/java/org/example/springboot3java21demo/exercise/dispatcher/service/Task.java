package org.example.springboot3java21demo.exercise.dispatcher.service;

// 文件: Task.java
// 说明：定义调度器任务接口，所有可调度的任务应实现此接口
public interface Task extends Runnable {
    /**
     * 获取任务名称，用于标识和取消任务
     *
     * @return 任务名称
     */
    String getName();
}
