package org.example.springboot3java21demo.exercise.kafka;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

/**
 * KAFKA 消费者测试类 —— 接收消息
 * <p>
 * 依赖 org.apache.kafka:kafka-clients（原生客户端，无需 spring-kafka）。
 * 测试前请确保本地 Kafka 已启动（默认 bootstrap: localhost:9092）。
 * <p>
 * 运行方式：
 * 1. 先启动本类 main 方法（消费者会持续 poll 消息）；
 * 2. 再运行 {@link KafkaProducerTest} 发送消息，观察本控制台输出。
 */
public class KafkaConsumerTest {

    /** Kafka 集群地址 */
    private static final String BOOTSTRAP_SERVERS = "localhost:9092";

    /** 主题名称 */
    private static final String TOPIC = "test";

    /** 消费者组 id：同组内的消费者自动分摊分区 */
    private static final String GROUP_ID = "demo-group";

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
        // 是否自动提交 offset
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        // 自动提交的时间间隔（毫秒），仅在开启自动提交时生效
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");
        // 一次 poll 返回的最大记录数
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "500");
        return props;
    }

    /**
     * 接收消息 —— 消费者主动拉取（poll 模型），处理完成后手动提交 offset（推荐）
     * <p>
     * 注意：Kafka 消费者是拉模式，不是 broker 推送。消费者必须持续 poll 才能维持组内成员身份。
     */
    public static void main(String[] args) {
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(buildConsumerProps());
        // 订阅主题
        consumer.subscribe(Collections.singletonList(TOPIC));

        // 优雅退出：JVM 关闭钩子中关闭 consumer，触发组内 rebalance
        final Thread mainThread = Thread.currentThread();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            // wakeup() 让阻塞中的 poll() 抛出 WakeupException，跳出循环
            consumer.wakeup();
            try {
                mainThread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }));

        try {
            // 持续轮询消息
            while (true) {
                // poll 会阻塞，最多等待 1 秒；期间还会完成组协调（join group 等）
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(1));
                for (ConsumerRecord<String, String> record : records) {
                    System.out.println("收到消息 => topic=" + record.topic()
                            + ", partition=" + record.partition()
                            + ", offset=" + record.offset()
                            + ", key=" + record.key()
                            + ", value=" + record.value());
                }
                if (!records.isEmpty()) {
                    // 业务处理完成后手动提交 offset：同步提交，失败会重试，保证不丢消息
                    consumer.commitSync();
                }
            }
        } catch (org.apache.kafka.common.errors.WakeupException e) {
            // 收到 wakeup 信号，正常退出循环
            System.out.println("消费者已关闭");
        } finally {
            // close() 会提交剩余 offset 并离开消费者组
            consumer.close();
        }
    }
}
