package org.example.springboot3java21demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.example.springboot3java21demo.rocketmq.config.RocketMqProperties;

@SpringBootApplication
@EnableConfigurationProperties(RocketMqProperties.class)
@EnableAspectJAutoProxy(exposeProxy = true)
public class Springboot3Java21DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(Springboot3Java21DemoApplication.class, args);
    }

}
