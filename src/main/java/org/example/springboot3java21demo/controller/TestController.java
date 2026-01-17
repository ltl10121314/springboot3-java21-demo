package org.example.springboot3java21demo.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.springboot3java21demo.service.impl.ScheduleTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "测试控制类", description = "调用测试类相关接口")
@Slf4j
@RestController
@RequestMapping("/test")
public class TestController {

    private final ScheduleTask scheduleTask;

    @Autowired
    public TestController(ScheduleTask scheduleTask) {
        this.scheduleTask = scheduleTask;
    }

    @GetMapping("/updateCron")
    public String updateCron(String cron) {
        log.info("new cron :{}", cron);
        scheduleTask.setCron(cron);
        return "ok";
    }

    @GetMapping("/start")
    public String start() {
        log.info("start");
        scheduleTask.configureTasks(new ScheduledTaskRegistrar());
        return "ok";
    }

}
