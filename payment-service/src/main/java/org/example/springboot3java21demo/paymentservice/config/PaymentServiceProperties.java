package org.example.springboot3java21demo.paymentservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashSet;
import java.util.Set;

@Data
@ConfigurationProperties(prefix = "payment-service")
public class PaymentServiceProperties {

    private Nacos nacos = new Nacos();
    private Gateway gateway = new Gateway();

    @Data
    public static class Nacos {
        private boolean enabled = true;
        private String serverAddress = "127.0.0.1:8848";
        private String namespace = "";
        private String dataId = "payment-service.properties";
        private String group = "DEFAULT_GROUP";
        private long timeoutMillis = 3000;
    }

    @Data
    public static class Gateway {
        private Set<String> allowedChannels =
                new LinkedHashSet<>(Set.of("ALIPAY", "WECHAT", "MOCK"));
        private double successRate = 1.0;
        private double unknownRate = 0.0;
        private long latencyMillis = 80;
    }
}
