package org.example.springboot3java21demo.service;

import org.example.springboot3java21demo.config.tx.CombinedTransactionAttributeSource;
import org.example.springboot3java21demo.domain.Student;
import org.example.springboot3java21demo.exercise.txprobe.TxProbeOutside;
import org.example.springboot3java21demo.mapper.StudentDao;
import org.example.springboot3java21demo.service.impl.StudentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.NameMatchTransactionAttributeSource;
import org.springframework.transaction.interceptor.RollbackRuleAttribute;
import org.springframework.transaction.interceptor.RuleBasedTransactionAttribute;
import org.springframework.transaction.interceptor.TransactionAttribute;
import org.springframework.transaction.interceptor.TransactionAttributeSource;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

/**
 * 验证事务的"生效范围"与"注解 / 方法名前缀兜底"的优先级。
 *
 * <p>被测对象是容器里真实装配的那套东西：{@code combinedTransactionAttributeSource}
 * + {@code hiddenTransactionAdvisor}（切点 {@code execution(public * ...service..*.*(..))}）。
 *
 * <p>结论（每条都有对应断言）：
 * <ol>
 *   <li>同时命中 {@code @Transactional} 和方法名前缀时，<b>注解优先</b>，前缀规则完全不参与；</li>
 *   <li>没有注解、只命中前缀时，前缀兜底生效（{@code get/find/query/list} → 只读，
 *       {@code save/create/update/delete} → 写事务且异常回滚）；</li>
 *   <li>两者都不命中 → 无事务；</li>
 *   <li>候选包之外（本例 {@code exercise.txprobe}）一律无事务、也不会被代理；</li>
 *   <li>类内 {@code this.xxx()} 自调用不走代理，事务属性不生效。</li>
 * </ol>
 *
 * <p>DAO 用 Mockito 顶掉，一来不依赖本地 MySQL，二来可以当"事务状态探针"——
 * 在 DAO 被调用的瞬间记录当前是否有事务、是否只读。
 */
@SpringBootTest
@DisplayName("事务生效范围：@Transactional 与方法名前缀兜底的优先级")
class StudentServiceTransactionScopeTest {

    /** 只提供两个探针 Bean，避免污染主配置（探针不在生产代码里）。 */
    @TestConfiguration
    static class ProbeConfig {

        @Bean
        TxProbeInside txProbeInside() {
            return new TxProbeInside();
        }

        @Bean
        TxProbeOutside txProbeOutside() {
            return new TxProbeOutside();
        }
    }

    /** 复刻 TransactionConfig 里的注解源：只认 @Transactional。 */
    private static final TransactionAttributeSource ANNOTATION_ONLY = new AnnotationTransactionAttributeSource();

    /** 一个永远返回 null 的前缀源，用来判断"某个方法的事务到底是不是来自注解"。 */
    private static final TransactionAttributeSource NO_PREFIX = (method, targetClass) -> null;

    @Autowired
    private ApplicationContext ctx;

    @Autowired
    private StudentService studentService;

    @Autowired
    private ObjectProvider<TxProbeInside> probeInside;

    @Autowired
    private ObjectProvider<TxProbeOutside> probeOutside;

    @MockitoBean
    private StudentDao studentDao;

    /** 每次 DAO 调用时记录的事务状态。 */
    private final List<String> daoStates = new CopyOnWriteArrayList<>();

    /** 容器里真正装配给 hiddenTransactionAdvisor 的那个事务属性源。 */
    private TransactionAttributeSource combined;

    @BeforeEach
    void setUp() {
        combined = ctx.getBean("combinedTransactionAttributeSource", TransactionAttributeSource.class);
        daoStates.clear();
        when(studentDao.queryById(anyString())).thenAnswer(inv -> {
            recordDao("queryById");
            return null;
        });
        when(studentDao.deleteById(anyString())).thenAnswer(inv -> {
            recordDao("deleteById");
            return 1;
        });
        when(studentDao.insert(any(Student.class))).thenAnswer(inv -> {
            recordDao("insert");
            return 1;
        });
        doAnswer(inv -> {
            recordDao("deleteAll");
            return null;
        }).when(studentDao).deleteAll();
    }

    @Test
    @DisplayName("0. 前置校验：service 包被代理，包外探针没被代理")
    void proxiesAreInPlace() {
        assertThat(AopUtils.isAopProxy(studentService)).isTrue();
        assertThat(AopUtils.isAopProxy(probeInside.getObject())).isTrue();
        assertThat(AopUtils.isAopProxy(probeOutside.getObject())).isFalse();
    }

    @Test
    @DisplayName("1. 注解与前缀冲突时：注解胜（REQUIRES_NEW + 非只读 覆盖 get* 的 REQUIRED + 只读）")
    void annotationWinsOverPrefixRule() throws Exception {
        Method method = TxProbeInside.class.getMethod("getState");

        // 只看前缀规则的话：getState 命中 get* -> REQUIRED + 只读
        TransactionAttribute byPrefixOnly = prefixRuleSource().getTransactionAttribute(method, TxProbeInside.class);
        assertThat(byPrefixOnly).isNotNull();
        assertThat(byPrefixOnly.getPropagationBehavior()).isEqualTo(TransactionDefinition.PROPAGATION_REQUIRED);
        assertThat(byPrefixOnly.isReadOnly()).isTrue();

        // 实际生效的是注解：REQUIRES_NEW + 非只读
        TransactionAttribute actual = combined.getTransactionAttribute(method, TxProbeInside.class);
        assertThat(actual).isNotNull();
        assertThat(actual.getPropagationBehavior()).isEqualTo(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        assertThat(actual.isReadOnly()).isFalse();

        // 运行时确认：如果前缀赢了会是只读事务
        assertThat(probeInside.getObject().getState()).isEqualTo("getState active=true readOnly=false");
    }

    @Test
    @DisplayName("1b. 真实 StudentService.updateStudent：注解与前缀同时命中，属性来自注解")
    void updateStudentAttributeComesFromAnnotation() throws Exception {
        Method method = StudentServiceImpl.class.getMethod("updateStudent", Student.class);

        // 把前缀源换成"永远返回 null"的，属性依然解析得出来 -> 证明确实来自注解，跟前缀无关
        TransactionAttributeSource annotationOnly =
                new CombinedTransactionAttributeSource(ANNOTATION_ONLY, NO_PREFIX);
        assertThat(annotationOnly.getTransactionAttribute(method, StudentServiceImpl.class)).isNotNull();

        TransactionAttribute actual = combined.getTransactionAttribute(method, StudentServiceImpl.class);
        assertThat(actual).isNotNull();
        assertThat(actual.isReadOnly()).isFalse();
        assertThat(actual.getPropagationBehavior()).isEqualTo(TransactionDefinition.PROPAGATION_REQUIRED);
        // rollbackFor = Exception：运行时异常和受检异常都要回滚
        assertThat(actual.rollbackOn(new IllegalStateException("boom"))).isTrue();
        assertThat(actual.rollbackOn(new java.io.IOException("boom"))).isTrue();
    }

    @Test
    @DisplayName("2. 只命中前缀 query*：只读事务（方法上没有注解）")
    void prefixOnlyRuleGivesReadOnlyTransaction() throws Exception {
        Method method = StudentServiceImpl.class.getMethod("queryById", String.class);

        // 没有前缀源时解析不出来 -> 该方法的事务完全来自前缀兜底
        TransactionAttributeSource annotationOnly =
                new CombinedTransactionAttributeSource(ANNOTATION_ONLY, NO_PREFIX);
        assertThat(annotationOnly.getTransactionAttribute(method, StudentServiceImpl.class)).isNull();

        TransactionAttribute actual = combined.getTransactionAttribute(method, StudentServiceImpl.class);
        assertThat(actual).isNotNull();
        assertThat(actual.isReadOnly()).isTrue();
        assertThat(actual.getPropagationBehavior()).isEqualTo(TransactionDefinition.PROPAGATION_REQUIRED);

        studentService.queryById("1");
        assertThat(daoStates).containsExactly("queryById active=true readOnly=true");
    }

    @Test
    @DisplayName("2b. 只命中前缀 delete*：写事务，异常回滚（方法上同样没有注解）")
    void prefixOnlyRuleGivesWriteTransaction() throws Exception {
        Method method = StudentServiceImpl.class.getMethod("deleteById", String.class);

        TransactionAttribute actual = combined.getTransactionAttribute(method, StudentServiceImpl.class);
        assertThat(actual).isNotNull();
        assertThat(actual.isReadOnly()).isFalse();
        // 写兜底规则同样声明了 rollbackOn Exception
        assertThat(actual.rollbackOn(new IllegalStateException("boom"))).isTrue();
        assertThat(actual.rollbackOn(new java.io.IOException("boom"))).isTrue();

        studentService.deleteById("1");
        assertThat(daoStates).containsExactly("deleteById active=true readOnly=false");
    }

    @Test
    @DisplayName("3. 有注解但名字不匹配任何前缀（insert）：注解独立生效")
    void annotationWorksWithoutAnyPrefixRule() throws Exception {
        Method method = StudentServiceImpl.class.getMethod("insert", Student.class);

        // 前缀表里没有 insert*，兜底解析不出来
        assertThat(prefixRuleSource().getTransactionAttribute(method, StudentServiceImpl.class)).isNull();
        // 注解照样生效
        assertThat(combined.getTransactionAttribute(method, StudentServiceImpl.class)).isNotNull();
    }

    @Test
    @DisplayName("3b. 注解和前缀都不命中（setBeanName）：没有事务")
    void noRuleNoTransaction() throws Exception {
        Method method = StudentServiceImpl.class.getMethod("setBeanName", String.class);
        assertThat(combined.getTransactionAttribute(method, StudentServiceImpl.class)).isNull();
    }

    @Test
    @DisplayName("4. 写事务真回滚：注解上的 rollbackFor = Exception 生效")
    void writeTransactionRollsBack() {
        AtomicInteger completionStatus = new AtomicInteger(-1);
        when(studentDao.update(any(Student.class))).thenAnswer(inv -> {
            // 在事务内部挂一个同步器，事务结束时会回调
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCompletion(int status) {
                    completionStatus.set(status);
                }
            });
            throw new IllegalStateException("boom");
        });

        assertThatThrownBy(() -> studentService.update(new Student()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(completionStatus.get()).isEqualTo(TransactionSynchronization.STATUS_ROLLED_BACK);
    }

    @Test
    @DisplayName("5. 注解的传播行为生效：task2 标注 NOT_SUPPORTED，方法体内没有事务")
    void notSupportedPropagationSuspendsTransaction() {
        // task2 内部：insert -> task3() -> task4() 抛异常，所以调用会抛 RuntimeException
        assertThatThrownBy(() -> studentService.task2()).isInstanceOf(RuntimeException.class);

        // task2 的 NOT_SUPPORTED 把事务挂起了，所以里面的 DAO 调用看不到事务
        assertThat(daoStates).isNotEmpty();
        assertThat(daoStates).allMatch(state -> state.contains("active=false"));
    }

    @Test
    @DisplayName("6. 类内 this 自调用不走代理：queryState 在自调用时没有事务，走代理时才有")
    void selfInvocationBypassesProxy() {
        assertThat(probeInside.getObject().selfInvokeState())
                .isEqualTo("self->queryState active=false readOnly=false");
        assertThat(probeInside.getObject().proxyInvokeState())
                .isEqualTo("proxy->queryState active=true readOnly=true");
    }

    @Test
    @DisplayName("7. 候选包外：名字再怎么匹配前缀也没有事务")
    void outsideCandidatePackagesNothingApplies() throws Exception {
        Method method = TxProbeOutside.class.getMethod("getState");

        assertThat(combined.getTransactionAttribute(method, TxProbeOutside.class)).isNull();
        assertThat(probeOutside.getObject().getState())
                .isEqualTo("getState active=false readOnly=false");
    }

    private void recordDao(String method) {
        daoStates.add(method
                + " active=" + TransactionSynchronizationManager.isActualTransactionActive()
                + " readOnly=" + TransactionSynchronizationManager.isCurrentTransactionReadOnly());
    }

    /**
     * 复刻 {@code TransactionConfig} 里的前缀规则，用来回答"如果只看方法名前缀会得到什么"。
     * 规则表若在 TransactionConfig 里调整，这里要同步。
     */
    private static TransactionAttributeSource prefixRuleSource() {
        RuleBasedTransactionAttribute writeTx = new RuleBasedTransactionAttribute();
        writeTx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        writeTx.setRollbackRules(List.of(new RollbackRuleAttribute(Exception.class)));

        RuleBasedTransactionAttribute readTx = new RuleBasedTransactionAttribute();
        readTx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        readTx.setReadOnly(true);

        NameMatchTransactionAttributeSource source = new NameMatchTransactionAttributeSource();
        source.setNameMap(Map.of(
                "save*", writeTx,
                "create*", writeTx,
                "update*", writeTx,
                "delete*", writeTx,
                "get*", readTx,
                "find*", readTx,
                "query*", readTx,
                "list*", readTx));
        return source;
    }
}
