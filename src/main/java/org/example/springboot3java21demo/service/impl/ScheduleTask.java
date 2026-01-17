package org.example.springboot3java21demo.service.impl;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.TriggerContext;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;

@Data
@Slf4j
@Component
//@PropertySource("classpath:/task-config.ini")
public class ScheduleTask implements SchedulingConfigurer {

    @Value("${printTime.cron}")
    private String cron;

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        // 动态使用cron表达式设置循环间隔
        taskRegistrar.addTriggerTask(new Runnable() {
            @Override
            public void run() {
                log.info("Current time： {}", LocalDateTime.now());
            }
        }, new Trigger() {
            @Override
            public Date nextExecutionTime(TriggerContext triggerContext) {
                // 使用CronTrigger触发器，可动态修改cron表达式来操作循环规则
                CronTrigger cronTrigger = new CronTrigger(cron);
                return cronTrigger.nextExecutionTime(triggerContext);
            }

            @Override
            public Instant nextExecution(TriggerContext triggerContext) {
                return null;
            }
        });
    }
}
