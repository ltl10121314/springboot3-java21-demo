package org.example.springboot3java21demo.payment.repository;

import org.example.springboot3java21demo.payment.domain.ChannelTradeBill;

import java.util.List;

/**
 * 渠道对账单仓储端口：渠道侧事实的落库入口（T+1 下载文件解析后入库）。
 */
public interface ChannelBillRepository {

    void saveAll(List<ChannelTradeBill> bills);

    List<ChannelTradeBill> findByBizDate(String bizDate);
}
