package org.example.springboot3java21demo.rocketmq;

import org.apache.rocketmq.client.apis.consumer.PushConsumer;
import org.apache.rocketmq.client.apis.producer.Producer;
import org.example.springboot3java21demo.rocketmq.config.RocketMqConfiguration;
import org.example.springboot3java21demo.rocketmq.config.RocketMqProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

class RocketMqConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(PropertiesConfiguration.class, RocketMqConfiguration.class);

    @Test
    void doesNotCreateClientsWhenRocketMqIsDisabled() {
        contextRunner
                .withPropertyValues(
                        "app.rocketmq.enabled=false",
                        "app.rocketmq.topic=test-topic")
                .run(context -> {
                    assertThat(context).hasSingleBean(RocketMqProperties.class);
                    assertThat(context.getBean(RocketMqProperties.class).getTopic())
                            .isEqualTo("test-topic");
                    assertThat(context).doesNotHaveBean(Producer.class);
                    assertThat(context).doesNotHaveBean(PushConsumer.class);
                });
    }

    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties(RocketMqProperties.class)
    static class PropertiesConfiguration {
    }
}
