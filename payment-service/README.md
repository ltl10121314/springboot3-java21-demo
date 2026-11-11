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

主要环境变量：

| 变量 | 默认值 |
| --- | --- |
| `PAYMENT_MYSQL_URL` | `jdbc:mysql://127.0.0.1:3306/order_payment?...` |
| `PAYMENT_MYSQL_USERNAME` | `dev` |
| `PAYMENT_MYSQL_PASSWORD` | `123456` |
| `REDIS_HOST` / `REDIS_PORT` | `127.0.0.1` / `6379` |
| `NACOS_SERVER_ADDRESS` | `127.0.0.1:8848` |
| `DUBBO_IP_TO_REGISTRY` | 自动选择真实局域网 IPv4 |
