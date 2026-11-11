package org.example.springboot3java21demo.orderpay.messaging;

import org.apache.kafka.clients.admin.NewTopic;
import org.example.springboot3java21demo.orderpay.config.OrderPayProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration(proxyBeanMethods = false)
@EnableKafka
public class KafkaTopicConfiguration {

    @Bean
    public NewTopic orderPayEventsTopic(OrderPayProperties properties) {
        return TopicBuilder.name(properties.getKafka().getTopic())
                .partitions(3)
                .replicas(1)
                .build();
    }

    /**
     * Elasticsearch 短暂不可用时持续重试当前记录。索引以 orderNo 作为文档 ID，
     * 重复消费会覆盖为 MySQL 最新状态，因此天然幂等。
     */
    @Bean
    public DefaultErrorHandler orderPayKafkaErrorHandler() {
        return new DefaultErrorHandler(new FixedBackOff(1000L, Long.MAX_VALUE));
    }
}
