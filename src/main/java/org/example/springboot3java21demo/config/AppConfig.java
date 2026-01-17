package org.example.springboot3java21demo.config;

import org.example.springboot3java21demo.utils.SpringContextUtil;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {
    @Bean
    public SpringContextUtil springContextUtil() {
        return new SpringContextUtil();
    }
    @Bean
    public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
        return args -> SpringContextUtil.setApplicationContext(ctx);
    }
}
