# 订单支付微服务平台

项目已从单体应用拆分为独立构建、独立部署的微服务。根目录只负责 Maven 聚合，
业务代码位于各服务目录。

## 模块

| 模块 | 职责 | HTTP | Dubbo |
| --- | --- | ---: | ---: |
| `order-service` | 订单、库存、Kafka/Outbox、RocketMQ 延迟关单、Elasticsearch 查询 | 18081 | 20880 |
| `payment-service` | 支付流水、Redis 幂等、支付渠道、Nacos 动态规则 | 18083 | 20882 |
| `user-service` | 用户与成员档案管理 | 18082 | - |
| `practice-service` | 独立练习服务：JDK、Spring、Redis、线程池、RocketMQ 示例 | 18084 | - |
| `payment-api` | 订单与支付共享的唯一 Dubbo 契约 | - | - |
| `platform-common` | Controller TraceId、Dubbo TraceId、统一 API 错误日志、本地运行适配 | - | - |

依赖方向固定为：

```text
order-service ──> payment-api <── payment-service
       │                               │
       └──────── platform-common ─────┘
```

订单服务不访问 `order_payment`，支付服务不访问 `op_order`。库存确认和订单状态推进
由订单服务完成，支付服务只返回支付结果。

## 基础设施

原 `~/docker/backend/docker-compose.yml` 的中间件定义已经整合到项目根目录
`docker-compose.middleware.yml`。数据目录默认继续使用
`${HOME}/docker/backend`，不会因为 Compose 文件迁移而创建空数据库：

| 组件 | 地址 | 用途 |
| --- | --- | --- |
| MySQL | `127.0.0.1:3306` | `test` 订单库、`order_payment` 支付库 |
| Redis | `127.0.0.1:6379` | 支付分布式幂等锁和结果缓存 |
| Nacos | `127.0.0.1:8848` | 配置中心、Dubbo 注册中心 |
| Kafka | `127.0.0.1:9092` | 订单事件与 ES 索引 |
| RocketMQ Proxy | `127.0.0.1:8081` | 延迟关单 |
| Elasticsearch | `http://127.0.0.1:9200` | 订单检索 |

不要重启已有中间件容器。只需保证它们健康运行。

## 本地启动

项目要求 Java 21。macOS 的系统代理或 VPN 可能把 Dubbo 内网请求转发到代理端口，
`platform-common` 会在 JVM 启动时清除系统代理，并自动选择真实局域网 IPv4 注册到
Nacos；也可用 `DUBBO_IP_TO_REGISTRY` 显式覆盖。

```bash
cd ~/IdeaProjects/springboot3-java21-demo
export JAVA_HOME="$HOME/Library/Java/JavaVirtualMachines/graalvm-jdk-21.0.7/Contents/Home"
export PATH="$JAVA_HOME/bin:$PATH"

mvn clean package

java -jar payment-service/target/payment-service-0.0.1-SNAPSHOT.jar
java -jar order-service/target/order-service-0.0.1-SNAPSHOT.jar
```

建议先启动 `payment-service`，再启动 `order-service`。用户服务可独立启动：

```bash
java -jar user-service/target/user-service-0.0.1-SNAPSHOT.jar
```

练习代码已组装为独立的 `practice-service`：

```bash
java -jar practice-service/target/practice-service-0.0.1-SNAPSHOT.jar
```

## Docker Compose

项目将中间件和应用服务拆为两个编排文件：

- `docker-compose.middleware.yml`：MySQL、Redis、Nacos、Kafka、RocketMQ、Elasticsearch
- `docker-compose.services.yml`：订单、支付、用户、练习服务

中间件 Compose 项目名和网络名均为 `backend`，可以识别当前已运行的中间件容器。
应用 Compose 项目名为 `backend-services`，通过外部 `backend` 网络连接中间件。

只启动或补齐中间件：

```bash
docker compose -f docker-compose.middleware.yml up -d mysql redis nacos kafka \
  rocketmq-namesrv rocketmq-broker rocketmq-proxy elasticsearch
docker compose -f docker-compose.middleware.yml ps
```

构建并启动全部应用：

```bash
docker compose -f docker-compose.services.yml up -d --build \
  payment-service order-service user-service practice-service
```

检查两个编排文件：

```bash
docker compose -f docker-compose.middleware.yml config --quiet
docker compose -f docker-compose.services.yml config --quiet
```

如果本机已经通过 `java -jar` 启动了这四个应用，请先停止这些 Java 进程，否则
`18081-18084`、`20880` 和 `20882` 会端口冲突。现有中间件健康检查、容器名和
数据卷路径均保持不变。

默认数据目录可以通过环境变量覆盖：

```bash
BACKEND_DATA_ROOT="$HOME/docker/backend" \
  docker compose -f docker-compose.middleware.yml up -d
```

已有 MySQL 表补齐中文字段注释：

```bash
docker exec -i dev-mysql sh -lc \
  'MYSQL_PWD=123456 mysql -h127.0.0.1 -udev --default-character-set=utf8mb4' \
  < scripts/apply-table-column-comments.sql
```

## 支付接口

创建订单：

```bash
curl -X POST http://127.0.0.1:18081/api/order-pay/orders \
  -H 'Content-Type: application/json' \
  -H 'X-Trace-Id: order-create-demo' \
  -d '{"requestId":"ORDER-REQ-001","userId":"user-001","skuId":"SKU-1001","quantity":2}'
```

支付订单：

```bash
curl -X POST http://127.0.0.1:18081/api/order-pay/orders/ORD-REPLACE-ME/pay \
  -H 'Content-Type: application/json' \
  -H 'X-Trace-Id: order-pay-demo' \
  -d '{"requestId":"PAY-REQ-001","channel":"ALIPAY"}'
```

## 库存管理接口

订单服务提供独立库存管理接口。`SKU` 会自动去除首尾空格并转为大写，
`availableStock` 始终按 `totalStock - reservedStock - soldStock` 计算。

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| `POST` | `/api/order-pay/inventory` | 创建库存 |
| `GET` | `/api/order-pay/inventory` | 分页查询，支持 `keyword`、`page`、`size` |
| `GET` | `/api/order-pay/inventory/{skuId}` | 查询库存详情 |
| `PUT` | `/api/order-pay/inventory/{skuId}` | 更新名称、价格和库存总量 |
| `PUT` | `/api/order-pay/inventory/{skuId}/stock` | 单独调整库存总量 |
| `DELETE` | `/api/order-pay/inventory/{skuId}` | 删除无预占、无已售库存的 SKU |

创建库存示例：

```bash
curl -X POST http://127.0.0.1:18081/api/order-pay/inventory \
  -H 'Content-Type: application/json' \
  -H 'X-Trace-Id: inventory-create-demo' \
  -d '{"skuId":"SKU-2001","skuName":"演示商品","price":99.00,"totalStock":100}'
```

库存总量不能小于 `reservedStock + soldStock`。订单预占、确认和释放仍由订单生命周期
内部完成，不通过管理接口直接操作，避免破坏订单与库存的一致性。

完整架构、数据表、异常处理和验证方法见
[订单支付微服务架构](docs/order-payment-architecture.md)。

## TraceId 规则

- 只有业务 Controller 请求入口生成或接收 `X-Trace-Id`，Actuator 和静态资源除外。
- 同一请求经过订单服务、Dubbo、支付服务时使用同一个 traceId。
- Outbox、Kafka、RocketMQ、线程池任务只继承已有 traceId，不凭空生成。
- 启动、健康检查、定时监控等无关联任务保持 `traceId=-`。
- 所有 API 4xx/5xx 和 RPC 异常都记录方法、路径、状态、异常类型及堆栈。
