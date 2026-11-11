package org.example.springboot3java21demo;

import org.example.springboot3java21demo.common.runtime.LocalRuntimeSupport;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class OrderServiceApplication {

    public static void main(String[] args) {
        LocalRuntimeSupport.configure();
        SpringApplication.run(OrderServiceApplication.class, args);
    }
}
