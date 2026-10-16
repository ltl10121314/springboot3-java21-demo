package org.example.springboot3java21demo.exercise.kafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * KAFKA 生产者测试类 —— 发送消息
 * <p>
 * 依赖 org.apache.kafka:kafka-clients（原生客户端，无需 spring-kafka）。
 * 测试前请确保本地 Kafka 已启动（默认 bootstrap: localhost:9092）。
 */
public class KafkaProducerTest {

    /** Kafka 集群地址 */
    private static final String BOOTSTRAP_SERVERS = "localhost:9092";

    /** 主题名称 */
    private static final String TOPIC = "test";

    /**
     * 构建生产者配置
     */
    private static Properties buildProducerProps() {
        Properties props = new Properties();
        // 集群地址
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        // key 序列化器
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        // value 序列化器
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        // 应答级别：all 表示所有 ISR 副本都确认才算发送成功（最可靠）
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        // 发送失败重试次数
        props.put(ProducerConfig.RETRIES_CONFIG, 3);
        // 批量大小（字节），达到该大小触发一次发送
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
        // 等待攒批的时间（毫秒），即使批次未满也发送，兼顾吞吐与延迟
        props.put(ProducerConfig.LINGER_MS_CONFIG, 1);
        // 生产者可用缓冲区总大小
        props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);
        return props;
    }

    /**
     * 发送消息 —— 异步方式：注册回调，broker 应答后触发，不阻塞主线程（推荐用法）
     */
    public static void main(String[] args) {
        try (KafkaProducer<String, String> producer = new KafkaProducer<>(buildProducerProps())) {
            for (int i = 0; i < 100; i++) {
                ProducerRecord<String, String> record =
                        new ProducerRecord<>(TOPIC, Integer.toString(i), Integer.toString(i));
                producer.send(record, (metadata, e) -> {
                    if (e == null) {
                        // 发送成功
                        System.out.println("异步发送成功 => topic=" + metadata.topic()
                                + ", partition=" + metadata.partition()
                                + ", offset=" + metadata.offset());
                    } else {
                        // 发送失败
                        System.err.println("异步发送失败: " + e.getMessage());
                    }
                });
            }
            // close() 会等待尚未完成的发送收尾
            producer.flush();
        }
    }

    /**
     * 发送消息 —— 同步方式：get() 阻塞等待 broker 应答，可靠性最高但吞吐最低
     */
    public void syncSend() {
        try (KafkaProducer<String, String> producer = new KafkaProducer<>(buildProducerProps())) {
            for (int i = 0; i < 5; i++) {
                ProducerRecord<String, String> record =
                        new ProducerRecord<>(TOPIC, "sync-key-" + i, "sync-value-" + i);
                Future<RecordMetadata> future = producer.send(record);
                try {
                    // 阻塞获取发送结果
                    RecordMetadata metadata = future.get();
                    System.out.println("同步发送成功 => topic=" + metadata.topic()
                            + ", partition=" + metadata.partition()
                            + ", offset=" + metadata.offset());
                } catch (InterruptedException | ExecutionException e) {
                    System.err.println("同步发送失败: " + e.getMessage());
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    /**
     * 发送消息 —— 带自定义分区：消息会进入指定的 partition
     */
    public void sendWithPartition() {
        try (KafkaProducer<String, String> producer = new KafkaProducer<>(buildProducerProps())) {
            // 构造器第三个参数指定分区：强制写入分区 0
            ProducerRecord<String, String> record =
                    new ProducerRecord<>(TOPIC, 0, "partition-key", "partition-value");
            producer.send(record, (metadata, e) -> {
                if (e == null) {
                    System.out.println("指定分区发送成功 => partition=" + metadata.partition()
                            + ", offset=" + metadata.offset());
                } else {
                    System.err.println("发送失败: " + e.getMessage());
                }
            });
            producer.flush();
        }
    }
}
