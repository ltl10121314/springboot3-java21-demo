package org.example.springboot3java21demo.orderpay.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 订单服务的组件配置。
 */
@Data
@ConfigurationProperties(prefix = "order-pay")
public class OrderPayProperties {

    private int timeoutSeconds = 120;
    private String defaultChannel = "ALIPAY";
    private Nacos nacos = new Nacos();
    private Kafka kafka = new Kafka();
    private RocketMq rocketmq = new RocketMq();
    private Search search = new Search();
    private Outbox outbox = new Outbox();

    @Data
    public static class Nacos {
        private boolean enabled = true;
        private String serverAddress = "127.0.0.1:8848";
        private String namespace = "";
        private String dataId = "order-service.properties";
        private String group = "DEFAULT_GROUP";
        private long timeoutMillis = 3000;
    }

    @Data
    public static class Kafka {
        private String topic = "order-pay-events";
        private String groupId = "order-pay-indexer";
    }

    @Data
    public static class RocketMq {
        private boolean enabled = true;
        private String endpoint = "127.0.0.1:8081";
        private String topic = "order-pay-timeout-topic";
        private String consumerGroup = "order-pay-timeout-consumer";
        private long requestTimeoutMillis = 3000;
    }

    @Data
    public static class Search {
        private String index = "order-pay-orders";
    }

    @Data
    public static class Outbox {
        private int batchSize = 50;
        private long retryDelaySeconds = 5;
    }
}
