# payment-service

独立支付微服务，只负责支付域：

- `order_payment.op_payment` 支付流水
- Redis `SET NX EX` 幂等锁和支付结果缓存
- Mock 支付渠道及失败/未知结果处理
- Nacos 动态渠道白名单
- 作为 Dubbo `PaymentRpcFacade` Provider 注册到 Nacos

默认端口：

- HTTP/Actuator：`18083`
- Dubbo：`20882`

独立启动：

```bash
cd ~/IdeaProjects/springboot3-java21-demo
JAVA_HOME="$HOME/Library/Java/JavaVirtualMachines/graalvm-jdk-21.0.7/Contents/Home" \
PATH=$JAVA_HOME/bin:$PATH \
java -jar payment-service/target/payment-service-0.0.1-SNAPSHOT.jar
```

Dubbo 契约只来自 `payment-api`。订单服务和支付服务不会各自维护同名 DTO。

## `op_payment` 字段

| 字段 | 含义 |
| --- | --- |
| `payment_no` | 平台支付单号，支付流水主键。 |
| `request_id` | 支付请求幂等键，同一支付请求只能创建一条流水。 |
| `order_no` | 关联订单号。 |
| `amount` | 本次支付金额。 |
| `channel` | 支付渠道，如 `ALIPAY`、`WECHAT`。 |
| `status` | 支付状态，见下表。 |
| `channel_trade_no` | 支付渠道返回的第三方交易号。 |
| `fail_reason` | 渠道明确失败时的原因。 |
| `version` | 乐观锁版本号，状态变更时递增。 |
| `created_at` | 支付流水创建时间。 |
| `updated_at` | 支付流水最后更新时间。 |

支付状态：

- `PROCESSING`：正在调用渠道或等待渠道结果。
- `SUCCESS`：渠道明确返回支付成功。
- `FAILED`：渠道明确返回支付失败。
- `UNKNOWN`：超时或网络异常导致结果未知，不能直接按失败处理。

主要环境变量：

| 变量 | 默认值 |
| --- | --- |
| `PAYMENT_MYSQL_URL` | `jdbc:mysql://127.0.0.1:3306/order_payment?...` |
| `PAYMENT_MYSQL_USERNAME` | `dev` |
| `PAYMENT_MYSQL_PASSWORD` | `123456` |
| `REDIS_HOST` / `REDIS_PORT` | `127.0.0.1` / `6379` |
| `NACOS_SERVER_ADDRESS` | `127.0.0.1:8848` |
| `DUBBO_IP_TO_REGISTRY` | 自动选择真实局域网 IPv4 |
