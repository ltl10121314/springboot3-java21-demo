package org.example.springboot3java21demo.exercise.txprobe;

import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * 测试探针：事务切点**覆盖不到**的包。
 *
 * <p>方法名同样命中 {@code get*} 前缀，但类不在前缀兜底的候选包名单里，
 * 所以既不该被代理，也不该有事务。
 */
public class TxProbeOutside {

    public String getState() {
        return "getState"
                + " active=" + TransactionSynchronizationManager.isActualTransactionActive()
                + " readOnly=" + TransactionSynchronizationManager.isCurrentTransactionReadOnly();
    }
}
