package org.example.springboot3java21demo.orderpay.config;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(OrderPayProperties.class)
@EnableDubbo
@EnableScheduling
public class OrderPayConfiguration {
}
