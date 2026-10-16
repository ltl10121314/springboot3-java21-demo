package org.example.springboot3java21demo.payment.repository;

import org.example.springboot3java21demo.payment.domain.ReconcileTask;

import java.util.List;

/**
 * 对账差异工单仓储端口：复盘闭环的载体。
 */
public interface ReconcileTaskRepository {

    void save(ReconcileTask task);

    List<ReconcileTask> findByBizDate(String bizDate);

    List<ReconcileTask> findOpen();
}
