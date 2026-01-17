package org.example.springboot3java21demo.exercise.dispatcher.service.impl;

import org.example.springboot3java21demo.exercise.dispatcher.service.Task;

// 文件: SimpleTask.java
// 说明：示例任务实现类，可根据具体业务逻辑修改此处代码
public class SimpleTask implements Task {
    private final String name;
    private final String message;

    /**
     * 构造方法
     *
     * @param name    任务名称
     * @param message 任务执行时打印的消息
     */
    public SimpleTask(String name, String message) {
        this.name = name;
        this.message = message;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void run() {
        // 任务执行逻辑：在控制台输出当前线程名称与消息
        System.out.println("[" + Thread.currentThread().getName() + "] "
                + "执行任务：" + name + "，消息：" + message + "，时间：" + java.time.LocalDateTime.now());
    }
}
