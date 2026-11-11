package org.example.practice;

import org.example.springboot3java21demo.common.runtime.LocalRuntimeSupport;
import org.example.practice.integration.rocketmq.config.RocketMqProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication(scanBasePackages = {
        "org.example.practice",
        "org.example.springboot3java21demo.common"
})
@EnableConfigurationProperties(RocketMqProperties.class)
@EnableAspectJAutoProxy(exposeProxy = true)
public class PracticeServiceApplication {

    public static void main(String[] args) {
        LocalRuntimeSupport.configure();
        SpringApplication.run(PracticeServiceApplication.class, args);
    }
}
