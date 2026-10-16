package org.example.springboot3java21demo.service;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * 测试探针：故意放在事务切点覆盖的 service 包内（{@code org.example.springboot3java21demo.service..}）。
 *
 * <p>用来在运行时观察"当前到底在不在事务里、是不是只读事务"：
 * <ul>
 *   <li>{@link #getState()}：方法名命中 {@code get*} 只读前缀，但注解要求 REQUIRES_NEW + 非只读，
 *       两者冲突时看谁生效；</li>
 *   <li>{@link #queryState()}：只命中前缀兜底（没有注解）；</li>
 *   <li>{@link #selfInvokeState()} / {@link #proxyInvokeState()}：对比 this 自调用与走代理调用。</li>
 * </ul>
 */
public class TxProbeInside {

    /** 自己注入自己，拿到的是代理对象，用于演示"走代理"与"不走代理"的差别。 */
    @Autowired
    private ObjectProvider<TxProbeInside> self;

    /** 名字匹配 get*（前缀规则给 REQUIRED + 只读），注解却要求 REQUIRES_NEW + 非只读。 */
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false)
    public String getState() {
        return state("getState");
    }

    /** 没有任何注解，只靠 query* 前缀兜底。 */
    public String queryState() {
        return state("queryState");
    }

    /** this 自调用：不经过代理，queryState 上的事务属性不会生效。 */
    public String selfInvokeState() {
        return "self->" + this.queryState();
    }

    /** 通过代理调用：queryState 会拿到 query* 前缀给的只读事务。 */
    public String proxyInvokeState() {
        return "proxy->" + self.getObject().queryState();
    }

    static String state(String tag) {
        return tag
                + " active=" + TransactionSynchronizationManager.isActualTransactionActive()
                + " readOnly=" + TransactionSynchronizationManager.isCurrentTransactionReadOnly();
    }
}
