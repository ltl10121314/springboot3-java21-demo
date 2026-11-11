#!/usr/bin/env bash
set -euo pipefail

# 只创建 RocketMQ 需要显式初始化的 Topic/ConsumerGroup。
# Kafka Topic 由 Spring Kafka Admin 创建，MySQL 表和 Nacos 配置由应用启动时自动初始化。
broker_container="${ROCKETMQ_BROKER_CONTAINER:-dev-rmq-broker}"
namesrv_addr="${ROCKETMQ_NAMESRV_ADDR:-rocketmq-namesrv:9876}"

docker exec "${broker_container}" sh mqadmin updateTopic \
  -n "${namesrv_addr}" \
  -c DefaultCluster \
  -t order-pay-timeout-topic \
  -r 2 \
  -w 2 \
  -a +message.type=DELAY

docker exec "${broker_container}" sh mqadmin updateSubGroup \
  -n "${namesrv_addr}" \
  -c DefaultCluster \
  -g order-pay-timeout-consumer \
  -r 2

echo "RocketMQ orders timeout topic and consumer group are ready."
