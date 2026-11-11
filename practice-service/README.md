# practice-service

由 `archive/examples` 恢复的独立练习微服务，用于运行和学习原项目示例。

- HTTP/Actuator：`18084`
- MySQL：`test`
- Redis：用于 RedisTemplate 示例
- Kafka：Client API 练习
- RocketMQ 5.x：Producer、PushConsumer、Tag、延迟消息和死信练习
- TraceId：来自 `platform-common`，Controller 请求自动串联日志

启动：

```bash
cd ~/IdeaProjects/springboot3-java21-demo
JAVA_HOME="$HOME/Library/Java/JavaVirtualMachines/graalvm-jdk-21.0.7/Contents/Home" \
PATH=$JAVA_HOME/bin:$PATH \
java -jar practice-service/target/practice-service-0.0.1-SNAPSHOT.jar
```

启用 RocketMQ 练习客户端：

```bash
APP_ROCKETMQ_ENABLED=true \
APP_ROCKETMQ_ENDPOINT=127.0.0.1:8081 \
java -jar practice-service/target/practice-service-0.0.1-SNAPSHOT.jar
```

该服务定位为开发和练习环境，不承载订单、支付、用户等生产业务数据。

## 源码目录

源码根包为 `org.example.practice`，按照练习主题而不是原来的历史文件来源划分：

```text
org.example.practice
├── bootstrap/                 启动与基础装配
├── common/                    通用工具
├── examples/                  示例 DTO、辅助工具和杂项实验
├── extensions/formula/        公式与 Scheme 扩展
├── integration/
│   ├── database/              JDBC、SQL 示例
│   ├── http/                  HTTP 客户端示例
│   ├── kafka/                 Kafka Client 示例
│   ├── redis/                 Redis 配置、工具、接口和示例
│   └── rocketmq/              RocketMQ 完整示例
├── jdk/
│   ├── collections/           集合与排序
│   ├── concurrency/           线程、锁、线程池、ForkJoin
│   ├── core/                  构造、日期、异常、函数式、引用
│   ├── io/                    BIO/NIO 与 Socket
│   ├── management/            JMX
│   ├── network/               IP 与网络编程
│   ├── reflection/            反射与类加载
│   └── serialization/         JSON 与 XStream
├── libraries/                 Excel、FTP、图片、加密、JWT
├── spring/                    Spring 上下文、调度、Quartz、Dispatcher
└── web/                       Servlet Filter 与 SSO
```

测试源码使用同一套包结构，RocketMQ 测试位于
`src/test/java/org/example/practice/integration/rocketmq`。
