package org.example.springboot3java21demo.config.tx;

import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class TransactionConfig {

//    @Autowired
//    private PlatformTransactionManager transactionManager;

    /**
     * 组合事务属性源：
     * 1. 注解优先
     * 2. 方法名前缀兜底
     */
    @Primary
    @Bean
    public TransactionAttributeSource combinedTransactionAttributeSource() {
        // 解析 @Transactional
        TransactionAttributeSource annotationSource = new AnnotationTransactionAttributeSource();
        // 解析方法名前缀
        NameMatchTransactionAttributeSource prefixSource = new NameMatchTransactionAttributeSource();
        Map<String, TransactionAttribute> rules = new HashMap<>();
        // 写事务
        RuleBasedTransactionAttribute writeTx = new RuleBasedTransactionAttribute();
        writeTx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        writeTx.setRollbackRules(List.of(new RollbackRuleAttribute(Exception.class)));
        rules.put("save*", writeTx);
        rules.put("create*", writeTx);
        rules.put("update*", writeTx);
        rules.put("delete*", writeTx);

        // 只读事务
        RuleBasedTransactionAttribute readTx = new RuleBasedTransactionAttribute();
        readTx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        readTx.setReadOnly(true);
        rules.put("get*", readTx);
        rules.put("find*", readTx);
        rules.put("query*", readTx);
        rules.put("list*", readTx);
        prefixSource.setNameMap(rules);
        return new CombinedTransactionAttributeSource(annotationSource, prefixSource);
    }

//    /**
//     * 2. 事务拦截器。
//     */
//    @Bean
//    public TransactionInterceptor businessTransactionInterceptor() {
//        TransactionInterceptor interceptor = new TransactionInterceptor();
//        interceptor.setTransactionManager(transactionManager);
//        interceptor.setTransactionAttributeSource(combinedTransactionAttributeSource());
//        return interceptor;
//    }
//
//    /**
//     * 3. 将 Service 包切点与事务拦截器关联。
//     */
//    @Bean
//    public DefaultPointcutAdvisor hiddenTransactionAdvisor() {
//        AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
//        pointcut.setExpression("execution(public * org.example.springboot3java21demo.service..*.*(..))");
//        return new DefaultPointcutAdvisor(pointcut, businessTransactionInterceptor());
//    }
}
