package org.example.springboot3java21demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy(exposeProxy = true)
public class Springboot3Java21DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(Springboot3Java21DemoApplication.class, args);
    }

}
