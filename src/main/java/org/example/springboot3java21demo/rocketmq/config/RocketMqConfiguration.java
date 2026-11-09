package org.example.springboot3java21demo.rocketmq.config;

import org.apache.rocketmq.client.apis.ClientConfiguration;
import org.apache.rocketmq.client.apis.ClientException;
import org.apache.rocketmq.client.apis.ClientServiceProvider;
import org.apache.rocketmq.client.apis.consumer.FilterExpression;
import org.apache.rocketmq.client.apis.consumer.MessageListener;
import org.apache.rocketmq.client.apis.consumer.PushConsumer;
import org.apache.rocketmq.client.apis.producer.Producer;
import org.example.springboot3java21demo.rocketmq.consumer.AuditOrderMessageListener;
import org.example.springboot3java21demo.rocketmq.consumer.BusinessOrderMessageListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Map;

/**
 * 用 Spring Bean 管理 RocketMQ 5.x gRPC Java 客户端。
 *
 * <p>把客户端配置集中在这里，业务层只依赖 Producer/Listener，
 * 可以避免在每个业务类中重复创建连接、设置 Group 和启动线程。</p>
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(prefix = "app.rocketmq", name = "enabled", havingValue = "true")
public class RocketMqConfiguration {

    @Bean
    public ClientServiceProvider clientServiceProvider() {
        return ClientServiceProvider.loadService();
    }

    @Bean
    public ClientConfiguration rocketMqClientConfiguration(RocketMqProperties properties) {
        return ClientConfiguration.newBuilder()
                .setEndpoints(properties.getEndpoint())
                .setRequestTimeout(Duration.ofMillis(properties.getRequestTimeoutMs()))
                .enableSsl(false)
                .build();
    }

    @Bean(destroyMethod = "close")
    public Producer rocketMqProducerClient(
            ClientServiceProvider provider,
            ClientConfiguration clientConfiguration,
            RocketMqProperties properties) throws ClientException {
        return provider.newProducerBuilder()
                .setClientConfiguration(clientConfiguration)
                .setTopics(properties.getTopic())
                .setMaxAttempts(properties.getSendMaxAttempts())
                .build();
    }

    /**
     * 订单业务 ConsumerGroup：订阅同一 Topic 的两个 Tag。
     */
    @Bean(destroyMethod = "close")
    public PushConsumer businessOrderConsumer(
            ClientServiceProvider provider,
            ClientConfiguration clientConfiguration,
            RocketMqProperties properties,
            BusinessOrderMessageListener listener) throws ClientException {
        return createPushConsumer(
                provider,
                clientConfiguration,
                properties,
                properties.getBusinessConsumerGroup(),
                properties.getBusinessTags(),
                listener);
    }

    /**
     * 审计 ConsumerGroup：只订阅 ORDER_CREATED，演示 Tag 过滤和独立消费进度。
     */
    @Bean(destroyMethod = "close")
    public PushConsumer auditOrderConsumer(
            ClientServiceProvider provider,
            ClientConfiguration clientConfiguration,
            RocketMqProperties properties,
            AuditOrderMessageListener listener) throws ClientException {
        return createPushConsumer(
                provider,
                clientConfiguration,
                properties,
                properties.getAuditConsumerGroup(),
                properties.getAuditTags(),
                listener);
    }

    private PushConsumer createPushConsumer(
            ClientServiceProvider provider,
            ClientConfiguration clientConfiguration,
            RocketMqProperties properties,
            String consumerGroup,
            String tags,
            MessageListener listener) throws ClientException {
        return provider.newPushConsumerBuilder()
                .setClientConfiguration(clientConfiguration)
                .setConsumerGroup(consumerGroup)
                .setSubscriptionExpressions(Map.of(
                        properties.getTopic(),
                        new FilterExpression(tags)))
                .setConsumptionThreadCount(properties.getConsumeThreadCount())
                .setMessageListener(listener)
                .build();
    }
}
