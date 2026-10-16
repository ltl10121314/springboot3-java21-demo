package org.example.springboot3java21demo.exercise.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Kafka 收发消息示例 —— 一个 main 完成「发送 + 接收」全流程
 * <p>
 * 依赖 org.apache.kafka:kafka-clients（原生客户端，无需 spring-kafka）。
 * 运行前请确保本地 Kafka 已启动（默认 bootstrap: localhost:9092），
 * 且主题 {@link #TOPIC} 已创建（或 broker 开启自动建主题）。
 * <p>
 * 运行流程：
 * 1. 启动消费者线程订阅主题（持续 poll 拉取消息）；
 * 2. main 线程发送 10 条异步消息 + 1 条同步消息；
 * 3. 等消费者处理完消息后，通过 wakeup() 优雅关闭消费者。
 */
@Slf4j
public class KafkaDemo {

    /** Kafka 集群地址 */
    private static final String BOOTSTRAP_SERVERS = "localhost:9092";

    /** 主题名称 */
    private static final String TOPIC = "test";

    /** 消费者组 id：同组内的消费者自动分摊分区 */
    private static final String GROUP_ID = "test-group";

    /** 消费者运行开关：置为 false 并 wakeup() 可优雅停止消费者线程 */
    private static final AtomicBoolean RUNNING = new AtomicBoolean(true);

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
        // 等待攒批的时间（毫秒），即使批次未满也发送，兼顾吞吐与延迟
        props.put(ProducerConfig.LINGER_MS_CONFIG, 1);
        return props;
    }

    /**
     * 构建消费者配置
     */
    private static Properties buildConsumerProps() {
        Properties props = new Properties();
        // 集群地址
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        // 消费者组 id
        props.put(ConsumerConfig.GROUP_ID_CONFIG, GROUP_ID);
        // key 反序列化器
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        // value 反序列化器
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        // 当没有已提交的 offset 时（首次消费或 offset 失效）：earliest 从头消费，latest 只消费新消息
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        // 关闭自动提交，改为处理完成后手动提交，保证不丢消息
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        // 一次 poll 返回的最大记录数
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "500");
        return props;
    }

    public static void main(String[] args) throws Exception {
        // 消费者在独立线程中持续 poll，main 线程负责发送
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(buildConsumerProps());
        Thread consumerThread = new Thread(() -> receiveMessages(consumer), "kafka-consumer");
        consumerThread.start();

        // 等待消费者完成订阅，避免消息发到分区后消费者还没加入组
        Thread.sleep(2000);

        // 发送消息
        sendMessages();

        // 留出时间让消费者处理完消息，然后优雅关闭
        Thread.sleep(5000);
        RUNNING.set(false);
        // wakeup() 让阻塞中的 poll() 抛出 WakeupException，跳出循环
        consumer.wakeup();
        consumerThread.join();
    }

    /**
     * 发送消息 —— 异步 + 同步两种方式
     */
    private static void sendMessages() {
        try (KafkaProducer<String, String> producer = new KafkaProducer<>(buildProducerProps())) {
            // 异步发送：注册回调，broker 应答后触发，不阻塞主线程（推荐用法）
            for (int i = 1; i <= 10; i++) {
                ProducerRecord<String, String> record =
                        new ProducerRecord<>(TOPIC, "key-" + i, "value-" + i);
                producer.send(record, (metadata, e) -> {
                    if (e == null) {
                        log.info("异步发送成功 => topic={}, partition={}, offset={}",
                                metadata.topic(), metadata.partition(), metadata.offset());
                    } else {
                        log.error("异步发送失败", e);
                    }
                });
            }

            // 同步发送：get() 阻塞等待 broker 应答，可靠性最高但吞吐最低
            ProducerRecord<String, String> syncRecord =
                    new ProducerRecord<>(TOPIC, "sync-key", "sync-value");
            Future<RecordMetadata> future = producer.send(syncRecord);
            try {
                RecordMetadata metadata = future.get();
                log.info("同步发送成功 => topic={}, partition={}, offset={}",
                        metadata.topic(), metadata.partition(), metadata.offset());
            } catch (InterruptedException | ExecutionException e) {
                log.error("同步发送失败", e);
                Thread.currentThread().interrupt();
            }

            // close() 前 flush，确保缓冲区的消息全部发出
            producer.flush();
        }
    }

    /**
     * 接收消息 —— 消费者主动拉取（poll 模型），处理完成后手动提交 offset（推荐）
     * <p>
     * 注意：Kafka 消费者是拉模式，不是 broker 推送。消费者必须持续 poll 才能维持组内成员身份。
     */
    private static void receiveMessages(KafkaConsumer<String, String> consumer) {
        // 订阅主题
        consumer.subscribe(Collections.singletonList(TOPIC));
        try {
            while (RUNNING.get()) {
                // poll 会阻塞，最多等待 1 秒；期间还会完成组协调（join group 等）
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(1));
                for (ConsumerRecord<String, String> record : records) {
                    log.info("收到消息 => topic={}, partition={}, offset={}, key={}, value={}",
                            record.topic(), record.partition(), record.offset(),
                            record.key(), record.value());
                }
                if (!records.isEmpty()) {
                    // 业务处理完成后手动提交 offset：同步提交，失败会重试，保证不丢消息
                    consumer.commitSync();
                }
            }
        } catch (WakeupException e) {
            // 收到 wakeup 信号，正常退出循环
            log.info("消费者收到退出信号");
        } finally {
            // close() 会提交剩余 offset 并离开消费者组，触发组内 rebalance
            consumer.close();
            log.info("消费者已关闭");
        }
    }
}
