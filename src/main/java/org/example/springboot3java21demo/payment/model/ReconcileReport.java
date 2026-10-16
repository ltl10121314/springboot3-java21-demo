package org.example.springboot3java21demo.payment.model;

import org.example.springboot3java21demo.payment.domain.ReconcileTask;

import java.util.List;
import java.util.Map;

/**
 * 对账报告。
 *
 * @param bizDate      账期
 * @param localTotal   本地流水数
 * @param channelTotal 渠道账单数
 * @param matched      完全一致的笔数
 * @param diffCount    差异笔数
 * @param diffByType   差异类型分布，用于快速判断是哪类系统性问题
 * @param tasks        差异工单明细
 */
public record ReconcileReport(
        String bizDate,
        int localTotal,
        int channelTotal,
        int matched,
        int diffCount,
        Map<String, Integer> diffByType,
        List<ReconcileTask> tasks) {

    public boolean balanced() {
        return diffCount == 0;
    }
}
