package org.example.springboot3java21demo;

import org.example.springboot3java21demo.common.runtime.LocalRuntimeSupport;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy(exposeProxy = true)
public class UserServiceApplication {

    public static void main(String[] args) {
        LocalRuntimeSupport.configure();
        SpringApplication.run(UserServiceApplication.class, args);
    }

}
