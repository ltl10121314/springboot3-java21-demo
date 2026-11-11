package org.example.springboot3java21demo.paymentservice;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.example.springboot3java21demo.common.runtime.LocalRuntimeSupport;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication(scanBasePackages = {
        "org.example.springboot3java21demo.paymentservice",
        "org.example.springboot3java21demo.common"
})
@EnableDubbo(scanBasePackages = "org.example.springboot3java21demo.paymentservice.rpc")
@ConfigurationPropertiesScan
public class PaymentServiceApplication {

    public static void main(String[] args) {
        LocalRuntimeSupport.configure();
        SpringApplication.run(PaymentServiceApplication.class, args);
    }
}
