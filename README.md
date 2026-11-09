## 1.接口文档地址

```
http://localhost:18081/swagger-ui/index.html#/
```

## 2.组件版本

```
springboot 3.5.9
java 21
rocketmq-client-java 5.2.2
RocketMQ Server 5.3.2
```

## 3.RocketMQ 实战

完整示例位于 [docs/rocketmq-practice.md](docs/rocketmq-practice.md)，包括：

- RocketMQ 5.x Java Producer 同步、异步发送
- PushConsumer 消费、失败重试与优雅关闭
- Spring Boot 管理 Producer/Consumer 生命周期
- Topic、Tag 过滤和 ConsumerGroup 隔离
- 真实 `%DLQ%` 死信消费、查询与一次重放
- 消费幂等、失败注入、延迟消息和运行指标
- 本地 RocketMQ Proxy 接入、Topic/ConsumerGroup 初始化

RocketMQ Proxy 使用 `8081`，应用默认使用 `18081`，避免端口冲突：

```bash
APP_ROCKETMQ_ENABLED=true \
APP_ROCKETMQ_ENDPOINT=127.0.0.1:8081 \
mvn spring-boot:run
```



