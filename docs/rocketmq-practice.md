# RocketMQ 5.x + Spring Boot 实战

本项目使用 RocketMQ 官方 5.x gRPC Java 客户端 `rocketmq-client-java:5.2.2`，由 Spring Boot 管理 Producer 和 PushConsumer 的生命周期。示例覆盖同步发送、异步发送、消费、Topic、Tag、ConsumerGroup、延迟消息、幂等、失败重试、死信队列和死信重放。

## 1. 整体模型

所有订单事件发送到同一个 Topic：`practice-order-topic`。

| 配置项 | 值 | 作用 |
| --- | --- | --- |
| Topic | `practice-order-topic` | 一级消息分类，隔离订单消息 |
| 延迟 Topic | `practice-order-delay-topic` | Topic 类型为 `DELAY`，延迟消息必须使用独立 Topic |
| Tag | `ORDER_CREATED`、`ORDER_PAID` | Topic 内的二级过滤条件 |
| 业务 ConsumerGroup | `practice-order-business-group` | 订阅 `ORDER_CREATED \|\| ORDER_PAID` |
| 审计 ConsumerGroup | `practice-order-audit-group` | 只订阅 `ORDER_CREATED` |
| 死信 ConsumerGroup | `practice-order-dlq-group` | 同时消费两个业务组的 `%DLQ%` Topic |
| 业务死信 Topic | `%DLQ%practice-order-business-group` | 业务组重试耗尽后的消息 |
| 审计死信 Topic | `%DLQ%practice-order-audit-group` | 审计组重试耗尽后的消息 |
| Client Endpoint | `127.0.0.1:8081` | RocketMQ 5.x Proxy gRPC 入口 |

发送链路：

```text
HTTP 请求
  -> OrderMessageProducer
  -> Message(topic, tag, JSON body), keys=orderId
  -> delaySeconds > 0 时改投 practice-order-delay-topic
  -> RocketMQ Proxy :8081
  -> RocketMQ Broker
       -> practice-order-business-group: ORDER_CREATED + ORDER_PAID
       -> practice-order-audit-group:    ORDER_CREATED
       -> 消费失败按级别重试 2 次
       -> 重试耗尽后进入对应 %DLQ% Topic
       -> practice-order-dlq-group 保存原始死信供查询/重放
```

两个 ConsumerGroup 各自收到完整消息，互不抢消息。相同 ConsumerGroup 启动多个实例时，实例之间分摊队列。业务组和审计组在这个示例中各运行一个 PushConsumer。

## 2. 关键概念

Topic 区分业务大类。订单事件统一进入订单 Topic，不应为每次请求动态创建 Topic。

Tag 区分同一个 Topic 内的子类型。消费端使用 `FilterExpression` 和 `ORDER_CREATED || ORDER_PAID` 表达式，未被订阅的 Tag 不会投递给该消费者。

ConsumerGroup 隔离消费进度和消费语义。不同 Group 会各自消费同一条消息；相同 Group 的多个实例属于集群消费，分摊 Queue。同一个 Group 在不同实例上的订阅关系必须一致。

RocketMQ 5.x 新客户端访问 Proxy 的 gRPC Endpoint，例如 `8081`；传统 Remoting 客户端才直接访问 NameServer `9876`。本项目采用新客户端，不能把 Endpoint 配成 NameServer。

RocketMQ 5.x 的延迟消息要求 Topic 元数据声明 `message.type=DELAY`，普通 `NORMAL` Topic 不能发送延迟消息。因此本项目使用独立 `practice-order-delay-topic`，两个业务 ConsumerGroup 同时订阅普通 Topic 和延迟 Topic。

死信队列由 RocketMQ 按 ConsumerGroup 自动生成，Topic 名固定为 `%DLQ%<ConsumerGroup>`。死信消费成功后不能简单回投原 Topic，否则会形成无限循环；本项目保存原始消息，并提供人工查询与一次性重放接口。

生产环境必须保证消费者幂等。RocketMQ 是至少一次投递，网络超时、客户端重启和消费失败重试都可能造成重复消费。本项目用 `ConsumerGroup + eventId` 做进程内有界幂等；生产环境应换成 Redis 或数据库唯一约束。

## 3. 启动现有 RocketMQ 5.3.2

当前较完整的一套 Compose 位于用户目录，包含 NameServer、Broker、Proxy 和 Dashboard。先确认四个容器均正常：

```bash
docker compose -f "$HOME/docker/rocketmq/docker-compose.yml" up -d
docker ps --format 'table {{.Names}}\t{{.Status}}\t{{.Ports}}'
```

预期关键端口：

| 容器 | 端口 | 用途 |
| --- | --- | --- |
| `rmqnamesrv` | `9876` | NameServer |
| `rmqbroker` | `10909`、`10911`、`10912` | Broker |
| `rmqproxy` | `8080`、`8081` | 5.x gRPC Proxy，本项目连接 `8081` |
| `rmqdashboard` | `8082` | Dashboard |

项目也提供了可直接运行的 Compose：

```bash
docker compose -f docker-compose.rocketmq.yml up -d
docker compose -f docker-compose.rocketmq.yml ps
```

项目 Compose 中的 `init` 容器会自动创建练习 Topic 和两个 ConsumerGroup。使用已有部署时手动执行：

```bash
docker exec rmqbroker sh -lc \
  'sh mqadmin updateTopic -n rmqnamesrv:9876 -c DefaultCluster -t practice-order-topic -r 8 -w 8'

docker exec rmqbroker sh -lc \
  'sh mqadmin updateTopic -n rmqnamesrv:9876 -c DefaultCluster -t practice-order-delay-topic -r 8 -w 8 -a +message.type=DELAY'

docker exec rmqbroker sh -lc \
  "sh mqadmin updateTopic -n rmqnamesrv:9876 -c DefaultCluster -t '%DLQ%practice-order-business-group' -r 1 -w 1"

docker exec rmqbroker sh -lc \
  "sh mqadmin updateTopic -n rmqnamesrv:9876 -c DefaultCluster -t '%DLQ%practice-order-audit-group' -r 1 -w 1"

docker exec rmqbroker sh -lc \
  'sh mqadmin updateSubGroup -n rmqnamesrv:9876 -c DefaultCluster -g practice-order-business-group -r 2'

docker exec rmqbroker sh -lc \
  'sh mqadmin updateSubGroup -n rmqnamesrv:9876 -c DefaultCluster -g practice-order-audit-group -r 2'

docker exec rmqbroker sh -lc \
  'sh mqadmin updateSubGroup -n rmqnamesrv:9876 -c DefaultCluster -g practice-order-dlq-group -r 2'
```

通过 `updateTopic` 和 `updateSubGroup` 管理资源比依赖自动创建更适合生产环境。练习 Topic 使用 8 个读写队列，因此一个 ConsumerGroup 最多可以并行使用 8 个队列。`-r 2` 表示最多重试 2 次，便于快速观察死信产生过程。

## 4. 启动 Spring Boot

RocketMQ 默认关闭，避免未安装中间件时影响整个项目启动。Proxy 占用了 `8081`，所以应用默认使用 `18081`：

```bash
APP_ROCKETMQ_ENABLED=true \
APP_ROCKETMQ_ENDPOINT=127.0.0.1:8081 \
mvn spring-boot:run
```

IDEA 启动参数：

```text
--app.rocketmq.enabled=true
--app.rocketmq.endpoint=127.0.0.1:8081
```

该教程代码已组装为独立的 `practice-service`。配置位于
`practice-service/src/main/resources/application.yaml` 的 `app.rocketmq` 节点：

```yaml
app:
  rocketmq:
    enabled: false
    endpoint: 127.0.0.1:8081
    topic: practice-order-topic
    delay-topic: practice-order-delay-topic
    business-consumer-group: practice-order-business-group
    audit-consumer-group: practice-order-audit-group
    dead-letter-consumer-group: practice-order-dlq-group
    business-tags: "ORDER_CREATED || ORDER_PAID"
    audit-tags: "ORDER_CREATED"
```

Producer 在启动时会校验 Topic 路由，因此必须先创建 Topic。Topic 不存在时应用会快速启动失败，而不是带病运行。

## 5. 发送与消费

查看实际配置：

```bash
curl http://127.0.0.1:18081/api/rocketmq/info
```

同步发送 `ORDER_CREATED`：

```bash
curl -X POST http://127.0.0.1:18081/api/rocketmq/orders/sync \
  -H 'Content-Type: application/json' \
  -d '{
    "orderId": "ORDER-1001",
    "amount": 99.90,
    "eventType": "ORDER_CREATED",
    "remark": "演示创建订单"
  }'
```

该消息会被业务组和审计组共同消费。

发送 `ORDER_PAID`：

```bash
curl -X POST http://127.0.0.1:18081/api/rocketmq/orders/sync \
  -H 'Content-Type: application/json' \
  -d '{
    "orderId": "ORDER-1001",
    "amount": 99.90,
    "eventType": "ORDER_PAID",
    "remark": "演示支付完成"
  }'
```

该消息只会被业务组消费，因为审计组只订阅 `ORDER_CREATED`。

异步发送：

```bash
curl -X POST http://127.0.0.1:18081/api/rocketmq/orders/async \
  -H 'Content-Type: application/json' \
  -d '{
    "orderId": "ORDER-1002",
    "amount": 18.80,
    "eventType": "ORDER_CREATED"
  }'
```

异步接口先返回 `202 SENDING`，真正的 `SendReceipt` 在客户端回调中记录到应用日志。

发送 5 秒延迟消息：

```bash
curl -X POST http://127.0.0.1:18081/api/rocketmq/orders/sync \
  -H 'Content-Type: application/json' \
  -d '{
    "orderId": "ORDER-DELAY-1001",
    "amount": 21.00,
    "eventType": "ORDER_CREATED",
    "remark": "5 秒后消费",
    "delaySeconds": 5
  }'
```

响应中的 `topic` 应为 `practice-order-delay-topic`。延迟 Topic 必须使用前面给出的 `message.type=DELAY` 参数创建。

注入消费失败并进入死信队列：

```bash
curl -X POST http://127.0.0.1:18081/api/rocketmq/orders/sync \
  -H 'Content-Type: application/json' \
  -d '{
    "orderId": "ORDER-DLQ-1001",
    "amount": 23.00,
    "eventType": "ORDER_CREATED",
    "remark": "验证死信队列",
    "failConsumerGroup": "BOTH"
  }'
```

`failConsumerGroup` 可用值为业务组名称、审计组名称或 `BOTH`。消费者收到该属性后返回 `ConsumeResult.FAILURE`。应用不直接写死信 Topic，而是让 RocketMQ 按 ConsumerGroup 的重试级别继续投递，达到 `retryMaxTimes` 后由 Broker 放入 `%DLQ%`。

查询最近消费记录：

```bash
curl 'http://127.0.0.1:18081/api/rocketmq/messages?limit=50'

curl 'http://127.0.0.1:18081/api/rocketmq/messages?consumerGroup=practice-order-business-group'

curl 'http://127.0.0.1:18081/api/rocketmq/messages?consumerGroup=practice-order-audit-group'
```

消费记录包含 `deliveryAttempt` 和 `status`：

- `CONSUMED`：首次消费成功。
- `RETRY`：业务判定失败或抛异常，返回 `FAILURE`。
- `DUPLICATE`：幂等表发现相同事件已处理，直接返回 `SUCCESS`。

查询死信、指标并重放：

```bash
curl 'http://127.0.0.1:18081/api/rocketmq/dead-letters?limit=20'

curl http://127.0.0.1:18081/api/rocketmq/metrics

curl -X POST \
  http://127.0.0.1:18081/api/rocketmq/dead-letters/<recordId>/replay
```

重放必须使用接口返回的 `recordId`，不能只用 RocketMQ `messageId`。不同 ConsumerGroup 的死信 Topic 可能生成相同的原始 `messageId`，`recordId` 是应用侧为每条死信生成的唯一索引。重放成功后会移除失败注入属性，并通过 `practice.replayed-from-dlq` 记录来源。同一 `recordId` 只允许重放一次。

消费记录、死信快照、幂等表和指标都保存在当前 JVM 内存，重启后清空，仅用于练习观察。

## 6. 代码入口

| 能力 | 代码 |
| --- | --- |
| Spring Bean、Endpoint、ConsumerGroup、Tag 订阅 | `rocketmq/config/RocketMqConfiguration.java` |
| 同步/异步 Producer | `rocketmq/service/OrderMessageProducer.java` |
| Topic、Tag、Key、延迟时间、失败注入构建 | `OrderMessageProducer#buildMessage` |
| 业务 Consumer | `rocketmq/consumer/BusinessOrderMessageListener.java` |
| Tag 过滤 Consumer | `rocketmq/consumer/AuditOrderMessageListener.java` |
| 死信 Consumer | `rocketmq/consumer/DeadLetterMessageListener.java` |
| 死信查询与重放 | `rocketmq/service/DeadLetterStore.java`、`DeadLetterReplayService.java` |
| 幂等与失败判定 | `RocketMqIdempotencyStore.java`、`RocketMqConstants.java` |
| 指标统计 | `rocketmq/service/RocketMqMetrics.java` |
| 反序列化与消费记录 | `rocketmq/service/OrderMessageConsumptionService.java` |
| HTTP 调试接口 | `rocketmq/controller/RocketMqDemoController.java` |

## 7. 日志与可观测性

`org.example.springboot3java21demo.rocketmq` 下的配置、Producer、Consumer、死信、幂等和指标组件都补充了日志：

- 启动日志：Endpoint、普通/延迟 Topic、ConsumerGroup、订阅集合和消费线程数。
- Producer 日志：发送前参数、消息 ID、Topic、Tag、Key、发送耗时和异常。
- Consumer 日志：消费开始、成功、模拟失败、实际重试、重复消息和消费耗时。
- 死信日志：来源 Group、`%DLQ%` Topic、原始消息 ID、应用 `recordId` 和尝试次数。
- 重放日志：被重放死信、新消息 ID、原 Topic、幂等和重复重放冲突。
- HTTP 请求日志沿用项目 `TraceIdFilter`，响应中的 `X-Trace-Id` 可以与 Controller/Service 日志关联。

日志不会打印完整业务正文，死信日志只打印 `payloadBytes`，避免在日志中泄露完整订单数据。

## 8. 验证结果

本项目已在 RocketMQ `5.3.2` 上完成真实收发验证：

- `ORDER_CREATED` 同步消息被 `practice-order-business-group` 和 `practice-order-audit-group` 各消费一次。
- `ORDER_PAID` 消息只被 `practice-order-business-group` 消费。
- `ORDER_CREATED` 异步消息发送成功，并进入两个订阅了该 Tag 的 ConsumerGroup。
- Topic 路由显示 8 个读队列和 8 个写队列。
- 普通 Topic 的延迟消息被客户端拒绝后，改为 DELAY Topic，消息按 5 秒延迟实际消费。
- 失败消息在 `deliveryAttempt=1/2/3` 三次投递后进入两个 `%DLQ%` Topic。
- 死信 ConsumerGroup 成功保存业务组和审计组的两条独立死信，并支持一次重放。

## 9. 生产化注意事项

1. 消费记录内存对象不是业务存储，生产环境应写数据库或事件日志。
2. 业务处理成功返回 `ConsumeResult.SUCCESS`，可重试失败返回 `ConsumeResult.FAILURE`；不可恢复的解析错误应先记录再按业务策略处理，避免无意义重试。
3. 业务处理必须幂等，不能假设 RocketMQ 只投递一次。
4. Topic 的 Queue 数量决定单 ConsumerGroup 的最大并行消费能力，需要根据流量提前规划。
5. 同一个 ConsumerGroup 下的订阅关系必须一致，不要让不同实例订阅不同 Tag。
6. Producer 发送超时不等于消息未发送成功。重试前应使用业务唯一键或事件 `eventId` 查询、去重。
7. 内存中的死信、幂等和指标不具备跨实例与重启能力，生产环境应落库并接入监控告警。
8. 死信重放应经过权限控制、人工审批或运营后台，本示例的 HTTP 重放接口仅用于本地练习。
9. 生产环境应启用鉴权、TLS、监控和告警；本示例为便于本地练习关闭了 SSL。
