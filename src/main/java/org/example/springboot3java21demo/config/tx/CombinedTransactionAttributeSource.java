package org.example.springboot3java21demo.config.tx;

import org.springframework.transaction.interceptor.TransactionAttribute;
import org.springframework.transaction.interceptor.TransactionAttributeSource;

import java.lang.reflect.Method;
import java.util.List;

/**
 * 注解优先，方法前缀兜底。
 */
public class CombinedTransactionAttributeSource implements TransactionAttributeSource {

    /**
     * 兜底规则只作用于 service 层，末尾带点号避免误匹配兄弟包。
     * 放到整个业务包会把 Controller / Filter / Config 等也纳入事务代理，
     * 它们继承来的 getXxx() 会命中 get* 规则。
     */
    private static final List<String> TX_PACKAGE_PREFIXES = List.of(
            "org.example.springboot3java21demo.service.",
            "org.example.springboot3java21demo.payment.service.",
            "org.example.springboot3java21demo.exercise.dispatcher.");

    private final TransactionAttributeSource annotationSource;
    private final TransactionAttributeSource prefixSource;

    public CombinedTransactionAttributeSource(
            TransactionAttributeSource annotationSource,
            TransactionAttributeSource prefixSource) {
        this.annotationSource = annotationSource;
        this.prefixSource = prefixSource;
    }

    @Override
    public TransactionAttribute getTransactionAttribute(Method method, Class<?> targetClass) {
        // 第一优先级：@Transactional
        TransactionAttribute attribute = annotationSource.getTransactionAttribute(method, targetClass);
        if (attribute != null) {
            return attribute;
        }
        // 第二优先级：方法名前缀兜底
        if (targetClass != null && inTxPackage(targetClass)) {
            return prefixSource.getTransactionAttribute(method, targetClass);
        }
        return null;
    }

    private static boolean inTxPackage(Class<?> targetClass) {
        String name = targetClass.getName();
        for (String prefix : TX_PACKAGE_PREFIXES) {
            if (name.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }
}