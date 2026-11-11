# 订单支付微服务架构

## 1. 服务边界

```mermaid
flowchart LR
    CLIENT[Client] --> ORDER[order-service :18081]
    ORDER --> ORDERDB[(test)]
    ORDER -->|Kafka Outbox| KAFKA[[Kafka]]
    ORDER -->|delay command| ROCKET[[RocketMQ]]
    KAFKA --> INDEXER[order-service indexer]
    INDEXER --> ES[(Elasticsearch)]
    ROCKET --> ORDER
    ORDER -->|Dubbo PaymentRpcFacade| PAY[payment-service :20882]
    PAY --> PAYDB[(order_payment)]
    PAY --> REDIS[(Redis)]
    PAY --> CHANNEL[Mock Channel]
    NACOS[(Nacos)] --- ORDER
    NACOS --- PAY
```

`order-service` 保存订单、库存和订单 Outbox。`payment-service` 保存支付流水，
通过 Redis 控制同一 `requestId` 的并发。两个服务使用独立数据库，不跨库写表。

## 2. 支付状态流

```mermaid
sequenceDiagram
    participant C as Client
    participant O as order-service
    participant P as payment-service
    participant DB as order_payment
    participant R as Redis

    C->>O: POST /api/order-pay/orders/{orderNo}/pay
    O->>O: CREATED -> PAYING
    O->>P: Dubbo pay(request, traceId)
    P->>R: SET NX EX requestId
    P->>DB: insert PROCESSING
    P->>P: call channel
    P->>DB: SUCCESS / FAILED / UNKNOWN
    P-->>O: PaymentRpcResponse
    alt SUCCESS
        O->>O: PAYING -> PAID, reserved -> sold
        O->>O: Outbox PAYMENT_SUCCESS
    else FAILED
        O->>O: PAYING -> CREATED
    else UNKNOWN
        O->>O: keep PAYING
    end
    O-->>C: result
```

支付成功和订单状态回写不能放在同一个数据库事务中。当前实现采用同步 RPC 返回结果
推进订单状态，并以支付表唯一键、订单乐观锁和 Outbox 保证可重试与可追踪。生产环境
还应增加支付结果查询补偿任务，处理 RPC 响应丢失后的最终一致。

## 3. 数据隔离

| 服务 | 数据库 | 表 |
| --- | --- | --- |
| `order-service` | `test` | `op_order`、`op_inventory`、`op_outbox_event` |
| `payment-service` | `order_payment` | `op_payment` |

初始化脚本：

- `order-service/src/main/resources/db/order-pay-schema.sql`
- `order-service/src/main/resources/db/order-pay-data.sql`
- `payment-service/src/main/resources/db/payment-schema.sql`

## 4. Dubbo 契约

`payment-api` 是唯一契约模块：

- `PaymentRpcFacade`
- `PaymentRpcRequest`
- `PaymentRpcResponse`

两个服务通过同一 jar 加载同一份 DTO 字节码，避免同名类各自编译导致序列化不一致。
协议使用 Dubbo + `fastjson2`，注册中心为 Nacos。

macOS 上若存在 VPN，Dubbo 可能误选 `utun` 的 fake-ip 或走系统代理。
`platform-common` 会清除 JVM 系统代理并选择真实局域网 IPv4。Docker 中可自动选择
`eth0` 地址，生产部署可通过 `DUBBO_IP_TO_REGISTRY` 指定。

## 5. TraceId 与日志

- `RequestTraceIdFilter` 只处理业务 Controller 请求，跳过 Actuator、Swagger 和静态资源。
- `TraceIdDubboFilter` 将 MDC traceId 放入 Dubbo attachment。
- 支付请求 DTO 同时携带 traceId，作为跨版本兼容的备用通道。
- Outbox payload 保存 traceId，Kafka Header 和 RocketMQ Property 继续传播。
- MQ 消费者、线程池任务只调用 `openIfPresent`，不会给无关任务生成 traceId。
- `ApiErrorLoggingFilter` 对任何返回 4xx/5xx 的 API 请求兜底记录日志。
- Controller Advice 对业务、校验、Dubbo 和未知异常记录完整上下文。

验证：

```bash
curl -i -X POST \
  http://127.0.0.1:18081/api/order-pay/orders/ORD-REPLACE-ME/pay \
  -H 'Content-Type: application/json' \
  -H 'X-Trace-Id: trace-e2e-001' \
  -d '{"requestId":"PAY-REQ-001","channel":"ALIPAY"}'

rg 'trace-e2e-001' order-service/logs payment-service/logs
```

订单、支付、Outbox、Kafka、Elasticsearch 和 RocketMQ 的关联日志应使用同一个
traceId；应用启动日志应为 `traceId=-`。

## 6. 启动顺序

1. 启动 `~/docker/backend` 中已有中间件。
2. 执行 `scripts/init-order-pay-middleware.sh` 初始化 RocketMQ Topic/Group。
3. 启动 `payment-service`。
4. 启动 `order-service`。

订单服务配置了 `dubbo.consumer.check=false`，支付服务短暂不可用时仍可启动，但支付
请求会返回 503 并记录完整错误日志。
