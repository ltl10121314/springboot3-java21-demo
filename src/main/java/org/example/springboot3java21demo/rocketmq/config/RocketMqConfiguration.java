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
import org.example.springboot3java21demo.rocketmq.consumer.DeadLetterMessageListener;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class RocketMqConfiguration {

    @Bean
    public ClientServiceProvider clientServiceProvider() {
        log.info("初始化 RocketMQ 5.x ClientServiceProvider");
        return ClientServiceProvider.loadService();
    }

    @Bean
    public ClientConfiguration rocketMqClientConfiguration(RocketMqProperties properties) {
        ClientConfiguration configuration = ClientConfiguration.newBuilder()
                .setEndpoints(properties.getEndpoint())
                .setRequestTimeout(Duration.ofMillis(properties.getRequestTimeoutMs()))
                .enableSsl(false)
                .build();
        log.info("RocketMQ 客户端配置完成 endpoint={}, requestTimeoutMs={}, ssl=false",
                properties.getEndpoint(), properties.getRequestTimeoutMs());
        return configuration;
    }

    @Bean(destroyMethod = "close")
    public Producer rocketMqProducerClient(
            ClientServiceProvider provider,
            ClientConfiguration clientConfiguration,
            RocketMqProperties properties) throws ClientException {
        Producer producer = provider.newProducerBuilder()
                .setClientConfiguration(clientConfiguration)
                .setTopics(properties.getTopic(), properties.getDelayTopic())
                .setMaxAttempts(properties.getSendMaxAttempts())
                .build();
        log.info("RocketMQ Producer 启动成功 topic={}, delayTopic={}, maxAttempts={}",
                properties.getTopic(), properties.getDelayTopic(), properties.getSendMaxAttempts());
        return producer;
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
                properties.getBusinessConsumerGroup(),
                Map.of(
                        properties.getTopic(), new FilterExpression(properties.getBusinessTags()),
                        properties.getDelayTopic(), new FilterExpression(properties.getBusinessTags())),
                properties.getConsumeThreadCount(),
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
                properties.getAuditConsumerGroup(),
                Map.of(
                        properties.getTopic(), new FilterExpression(properties.getAuditTags()),
                        properties.getDelayTopic(), new FilterExpression(properties.getAuditTags())),
                properties.getConsumeThreadCount(),
                listener);
    }

    /**
     * 独立消费两个业务 Group 的死信 Topic。
     *
     * <p>死信消息消费成功后返回 SUCCESS；应用会保存原始消息，供查询和重放。</p>
     */
    @Bean(destroyMethod = "close")
    public PushConsumer deadLetterConsumer(
            ClientServiceProvider provider,
            ClientConfiguration clientConfiguration,
            RocketMqProperties properties,
            DeadLetterMessageListener listener) throws ClientException {
        return createPushConsumer(
                provider,
                clientConfiguration,
                properties.getDeadLetterConsumerGroup(),
                Map.of(
                        properties.getBusinessDeadLetterTopic(), FilterExpression.SUB_ALL,
                        properties.getAuditDeadLetterTopic(), FilterExpression.SUB_ALL),
                properties.getConsumeThreadCount(),
                listener);
    }

    private PushConsumer createPushConsumer(
            ClientServiceProvider provider,
            ClientConfiguration clientConfiguration,
            String consumerGroup,
            Map<String, FilterExpression> subscriptions,
            int consumptionThreadCount,
            MessageListener listener) throws ClientException {
        PushConsumer consumer = provider.newPushConsumerBuilder()
                .setClientConfiguration(clientConfiguration)
                .setConsumerGroup(consumerGroup)
                .setSubscriptionExpressions(subscriptions)
                .setConsumptionThreadCount(consumptionThreadCount)
                .setMessageListener(listener)
                .build();
        log.info("RocketMQ PushConsumer 启动成功 group={}, subscriptions={}, threads={}",
                consumerGroup, subscriptions.keySet(), consumptionThreadCount);
        return consumer;
    }
}
