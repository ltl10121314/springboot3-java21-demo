package org.example.springboot3java21demo.payment.service;

import org.example.springboot3java21demo.payment.model.ReconcileReport;

/**
 * 对账服务：用渠道侧的事实校验本地流水，把不一致变成可追踪的工单。
 */
public interface ReconcileService {

    /**
     * 对指定账期做一次全量对账
     *
     * @param bizDate 账期 yyyy-MM-dd
     */
    ReconcileReport reconcile(String bizDate);
}
