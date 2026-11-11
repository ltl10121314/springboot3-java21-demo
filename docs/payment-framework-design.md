# 支付服务设计说明

支付域已从原单体示例中拆出，生产实现位于 `payment-service`，RPC 契约位于
`payment-api`。旧的内存版支付框架不再参与任何服务启动。

## 核心约束

1. `requestId` 是支付幂等键，Redis `SET NX EX` 只负责并发互斥，MySQL 唯一索引是最终事实约束。
2. `paymentNo` 是平台支付单号，由订单服务在进入 `PAYING` 时生成。
3. 渠道成功、明确失败和结果未知必须分开处理；异常网络结果不能直接视为失败。
4. 支付服务只修改 `order_payment`，订单状态与库存由订单服务闭环。
5. 状态和异常日志必须携带请求 traceId。

## 处理流程

```text
参数校验
  -> Nacos 渠道白名单
  -> requestId 幂等查询
  -> Redis 分布式锁
  -> 创建 PROCESSING 流水
  -> 调用渠道
  -> SUCCESS / FAILED / UNKNOWN
  -> 缓存结果
```

渠道调用抛出运行时异常时，已创建流水会更新为 `UNKNOWN` 并返回给订单服务。订单服务
保持 `PAYING`，等待查询或后续补偿，不会错误释放库存。

## 扩展点

- 将 `PaymentGateway` 的 Mock 实现替换为支付宝、微信或银联客户端。
- 增加渠道查询补偿任务，处理长时间停留在 `UNKNOWN` 的支付单。
- 将支付结果缓存接入查询优先路径，减少数据库读取。
- 接入 Micrometer、Prometheus 和分布式链路追踪系统。
