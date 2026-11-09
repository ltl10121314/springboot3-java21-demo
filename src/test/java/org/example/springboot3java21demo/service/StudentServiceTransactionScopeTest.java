package org.example.springboot3java21demo.service;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.example.springboot3java21demo.config.tx.CombinedTransactionAttributeSource;
import org.example.springboot3java21demo.domain.Student;
import org.example.springboot3java21demo.exercise.txprobe.TxProbeOutside;
import org.example.springboot3java21demo.aop.log.LogAspect;
import org.example.springboot3java21demo.mapper.StudentDao;
import org.example.springboot3java21demo.mapper.UserDao;
import org.example.springboot3java21demo.service.impl.StudentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.aop.Advisor;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionAttribute;
import org.springframework.transaction.interceptor.TransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
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
 * <p>被测对象是运行期真正生效的那条代理链。项目里 {@code TransactionConfig} 有两种装配方式
 * （给组合属性源加 {@code @Primary} 交给框架的全局事务 advisor，或自定义
 * {@code hiddenTransactionAdvisor} 切 service 包），本测试不写死某一种：用例 0 断言
 * <b>代理链里存在事务拦截器、且它拿的就是容器里的 {@code combinedTransactionAttributeSource}</b>
 * （用 anyMatch，两种装配同时开也成立）。
 * 反过来，如果这个 bean 不再被任何事务拦截器使用（比如去掉 {@code @Primary} 又没启用自定义
 * advisor——框架会退回它自己的 {@code AnnotationTransactionAttributeSource}），则用例
 * 0/1/2/2b/2c/6 立刻失败，前缀兜底静默失效这件事跑不掉。
 *
 * <p>结论（每条都有对应断言）：
 * <ol>
 *   <li>同时命中 {@code @Transactional} 和方法名前缀时，<b>注解优先</b>，前缀规则完全不参与；</li>
 *   <li>没有注解、只命中前缀时，前缀兜底生效（{@code get/find/query/list} → 只读，
 *       {@code save/create/update/delete} → 写事务且异常回滚），8 个前缀家族逐个校验；</li>
 *   <li>两者都不命中 → 无事务；</li>
 *   <li>候选包之外的类拿不到前缀兜底（{@code @Transactional} 注解本身不受包限制，仍然照常生效）；</li>
 *   <li>类内 {@code this.xxx()} 自调用不走代理，事务属性不生效。</li>
 * </ol>
 *
 * <p>DAO 用 Mockito 顶掉，所以不会真的执行 SQL、也不会改库；但事务是真的事务，
 * 上下文启动时仍会连本地 MySQL（Hikari 启动即建连），所以本测试需要本地数据库可达。
 * DAO 同时兼当"事务状态探针"：在它被调用的瞬间记录当前是否有事务、是否只读。
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

    /** 故意用一个专用异常，避免和"根本没有事务"时抛出的其他异常混在一起。 */
    static class DaoBoomException extends RuntimeException {
        DaoBoomException(String message) {
            super(message);
        }
    }

    @Autowired
    private ApplicationContext ctx;

    @Autowired
    private StudentService studentService;

    @Autowired
    private UserService userService;

    @Autowired
    private ObjectProvider<TxProbeInside> probeInside;

    @Autowired
    private ObjectProvider<TxProbeOutside> probeOutside;

    @MockitoBean
    private StudentDao studentDao;

    @MockitoBean
    private UserDao userDao;

    /** 每次 DAO 调用时记录的事务状态。 */
    private final List<String> daoStates = new CopyOnWriteArrayList<>();

    /** 容器里的组合属性源——运行时拦截器实际使用的那一个，见用例 0。 */
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
    @DisplayName("0. 前置校验：事务拦截器用的就是容器的组合属性源；包外探针没有事务增强")
    void transactionAdvisorIsWiredToContainerAttributeSource() {
        // 只断言"被代理"没有意义：LogAspect 也会代理 service 包，所以直接看代理链里的事务拦截器。
        // 用 anyMatch 而不是"第一个"：自定义 advisor 与框架 advisor 同时存在时，两条链都可能有事务拦截器。
        assertThat(transactionInterceptorsOf(studentService))
                .as("StudentService 的代理链里要有事务拦截器，且用的是容器的组合属性源")
                .isNotEmpty()
                .anyMatch(interceptor -> interceptor.getTransactionAttributeSource() == combined);
        assertThat(transactionInterceptorsOf(probeInside.getObject()))
                .isNotEmpty()
                .anyMatch(interceptor -> interceptor.getTransactionAttributeSource() == combined);

        // 包外探针：没有代理，更没有事务增强
        assertThat(AopUtils.isAopProxy(probeOutside.getObject())).isFalse();
        assertThat(transactionInterceptorsOf(probeOutside.getObject())).isEmpty();
    }

    @Test
    @DisplayName("1. 注解与前缀冲突时：注解胜（REQUIRES_NEW + 非只读 覆盖 get* 的 REQUIRED + 只读）")
    void annotationWinsOverPrefixRule() throws Exception {
        // 基线（来自真实配置）：同一个类里名字命中 query*、且没有注解的方法 -> REQUIRED + 只读
        TransactionAttribute byPrefix = combined.getTransactionAttribute(
                TxProbeInside.class.getMethod("queryState"), TxProbeInside.class);
        assertThat(byPrefix).isNotNull();
        assertThat(byPrefix.getPropagationBehavior()).isEqualTo(TransactionDefinition.PROPAGATION_REQUIRED);
        assertThat(byPrefix.isReadOnly()).isTrue();

        // getState 的名字同样命中 get*，但多了注解 -> 生效的是注解
        TransactionAttribute actual = combined.getTransactionAttribute(
                TxProbeInside.class.getMethod("getState"), TxProbeInside.class);
        assertThat(actual).isNotNull();
        assertThat(actual.getPropagationBehavior()).isEqualTo(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        assertThat(actual.isReadOnly()).isFalse();

        // 运行时对照：同一套切点、同一个类，只差一个注解，事务只读性就反过来了
        assertThat(probeInside.getObject().queryState()).isEqualTo("queryState active=true readOnly=true");
        assertThat(probeInside.getObject().getState()).isEqualTo("getState active=true readOnly=false");
    }

    @Test
    @DisplayName("1b. 真实 StudentService.updateStudent：属性确实来自注解（与前缀源无关）")
    void updateStudentAttributeComesFromAnnotation() throws Exception {
        Method method = StudentServiceImpl.class.getMethod("updateStudent", Student.class);

        // 把前缀源换成"永远返回 null"的，属性依然解析得出来 -> 它来自注解，跟前缀无关。
        // 注意：updateStudent 的注解（REQUIRED + rollbackFor Exception）与 update* 写前缀语义相同，
        // 所以"谁优先"在这个方法上观测不到，优先级由用例 1 覆盖。
        TransactionAttributeSource annotationOnly =
                new CombinedTransactionAttributeSource(new AnnotationTransactionAttributeSource(), (m, c) -> null);
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
                new CombinedTransactionAttributeSource(new AnnotationTransactionAttributeSource(), (m, c) -> null);
        assertThat(annotationOnly.getTransactionAttribute(method, StudentServiceImpl.class)).isNull();

        TransactionAttribute actual = combined.getTransactionAttribute(method, StudentServiceImpl.class);
        assertThat(actual).isNotNull();
        assertThat(actual.isReadOnly()).isTrue();
        assertThat(actual.getPropagationBehavior()).isEqualTo(TransactionDefinition.PROPAGATION_REQUIRED);

        studentService.queryById("1");
        assertThat(daoStates).containsExactly("queryById active=true readOnly=true");
    }

    @Test
    @DisplayName("2b. 只命中前缀 delete*：写事务，DAO 抛异常时真的回滚（方法上同样没有注解）")
    void prefixOnlyRuleGivesWriteTransaction() throws Exception {
        Method method = StudentServiceImpl.class.getMethod("deleteById", String.class);

        TransactionAttribute actual = combined.getTransactionAttribute(method, StudentServiceImpl.class);
        assertThat(actual).isNotNull();
        assertThat(actual.isReadOnly()).isFalse();
        // 写兜底规则同样声明了 rollbackOn Exception
        assertThat(actual.rollbackOn(new IllegalStateException("boom"))).isTrue();
        assertThat(actual.rollbackOn(new java.io.IOException("boom"))).isTrue();

        // 运行时①：确实开了写事务
        studentService.deleteById("1");
        assertThat(daoStates).containsExactly("deleteById active=true readOnly=false");

        // 运行时②：这条"只靠前缀兜底"的事务也会回滚
        AtomicInteger completionStatus = new AtomicInteger(-1);
        when(studentDao.deleteById(anyString())).thenAnswer(inv -> {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCompletion(int status) {
                    completionStatus.set(status);
                }
            });
            throw new DaoBoomException("boom");
        });
        assertThatThrownBy(() -> studentService.deleteById("1")).isInstanceOf(DaoBoomException.class);
        assertThat(completionStatus.get()).isEqualTo(TransactionSynchronization.STATUS_ROLLED_BACK);
    }

    @Test
    @DisplayName("2d. 前缀规则表本身：8 个前缀家族都能从生产配置解析出正确属性")
    void allPrefixFamiliesResolve() throws Exception {
        // 逐个前缀家族验证 TransactionConfig 里的规则表，删掉/改错任何一条这里都会失败
        Map<String, Boolean> expectedReadOnly = Map.of(
                "saveProbe", false,
                "createProbe", false,
                "updateProbe", false,
                "deleteProbe", false,
                "getProbe", true,
                "findProbe", true,
                "queryState", true,
                "listProbe", true);

        for (Map.Entry<String, Boolean> entry : expectedReadOnly.entrySet()) {
            Method method = TxProbeInside.class.getMethod(entry.getKey());
            TransactionAttribute attribute = combined.getTransactionAttribute(method, TxProbeInside.class);
            assertThat(attribute)
                    .as("前缀 %s 没有解析出事务属性（规则表被改动？）", entry.getKey())
                    .isNotNull();
            assertThat(attribute.isReadOnly())
                    .as("前缀 %s 的只读属性", entry.getKey())
                    .isEqualTo(entry.getValue());
        }
    }

    @Test
    @DisplayName("2c. 只命中前缀 find*：UserService.findAll 同样是只读事务")
    void findAllGetsReadOnlyTransaction() {
        when(userDao.findAll()).thenAnswer(inv -> {
            recordDao("userDao.findAll");
            return List.of();
        });

        userService.findAll();
        assertThat(daoStates).containsExactly("userDao.findAll active=true readOnly=true");
    }

    @Test
    @DisplayName("3. 有注解但名字不匹配任何前缀（insert）：注解独立生效")
    void annotationWorksWithoutAnyPrefixRule() throws Exception {
        Method method = StudentServiceImpl.class.getMethod("insert", Student.class);

        // 前缀表里只有 save*/create*/update*/delete* 和 get*/find*/query*/list*，没有 insert*；
        // 前缀源置空后属性依然解析得出来，说明这条事务只可能来自注解。
        TransactionAttributeSource annotationOnly =
                new CombinedTransactionAttributeSource(new AnnotationTransactionAttributeSource(), (m, c) -> null);
        assertThat(annotationOnly.getTransactionAttribute(method, StudentServiceImpl.class)).isNotNull();
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
        AtomicBoolean synchronizationRegistered = new AtomicBoolean(false);

        when(studentDao.update(any(Student.class))).thenAnswer(inv -> {
            // 能注册成功本身就说明"此刻确实在事务里"（没有事务时这里会抛 IllegalStateException）
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCompletion(int status) {
                    completionStatus.set(status);
                }
            });
            synchronizationRegistered.set(true);
            throw new DaoBoomException("boom");
        });

        assertThatThrownBy(() -> studentService.update(new Student()))
                .isInstanceOf(DaoBoomException.class);   // 专用异常：不会把"根本没开事务"的失败混进来
        assertThat(synchronizationRegistered).isTrue();
        assertThat(completionStatus.get()).isEqualTo(TransactionSynchronization.STATUS_ROLLED_BACK);
    }

    @Test
    @DisplayName("5. 注解的传播行为生效：外层有事务时，task2 的 NOT_SUPPORTED 把事务挂起")
    void notSupportedPropagationSuspendsTransaction() {
        TransactionTemplate outerTx = new TransactionTemplate(ctx.getBean(PlatformTransactionManager.class));

        outerTx.executeWithoutResult(status -> {
            // 外层事务确实在
            assertThat(TransactionSynchronizationManager.isActualTransactionActive()).isTrue();

            // task2 内部会 insert -> task3() -> task4() 抛异常
            assertThatThrownBy(() -> studentService.task2()).isInstanceOf(RuntimeException.class);

            // 同一个外层事务里，普通查询方法仍然看得见事务（对照，证明是被挂起而不是"压根没事务"）
            studentService.queryById("1");
        });

        assertThat(daoStates)
                .contains("insert active=false readOnly=false")     // task2 里：事务被挂起
                .contains("queryById active=true readOnly=false");  // 回到外层事务后：事务还在
    }

    @Test
    @DisplayName("6. 类内 this 自调用不走代理：queryState 在自调用时没有事务，走代理时才有")
    void selfInvocationBypassesProxy() {
        // 负例：this.queryState()
        assertThat(probeInside.getObject().selfInvokeState())
                .isEqualTo("self->queryState active=false readOnly=false");
        // 正例：同一个方法走代理就有只读事务（配合用例 0：该 bean 的事务拦截器确实挂上了）
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

    @Test
    @DisplayName("8. @AfterThrowing：DAO 抛异常时 throwsAdvice 执行，@AfterReturning 不执行")
    void throwsAdviceFiresWhenDaoThrows() {
        // 让 DAO 抛异常：异常从 StudentServiceImpl.update 逃出代理，而不是被方法体 catch 掉
        when(studentDao.update(any(Student.class))).thenThrow(new DaoBoomException("boom"));

        // 给 LogAspect 的 logger 挂一个 ListAppender，直接断言切面方法真的执行了
        Logger aspectLogger = (Logger) LoggerFactory.getLogger(LogAspect.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        aspectLogger.addAppender(appender);
        try {
            assertThatThrownBy(() -> studentService.update(new Student()))
                    .isInstanceOf(DaoBoomException.class);
        } finally {
            aspectLogger.detachAppender(appender);
        }

        // 只看当前测试线程的日志：上下文里的 Quartz/定时任务在后台线程跑，不能算进来
        List<String> messages = appender.list.stream()
                .filter(event -> Thread.currentThread().getName().equals(event.getThreadName()))
                .map(ILoggingEvent::getFormattedMessage)
                .toList();

        assertThat(messages)
                .as("前置通知先执行，异常逃逸后 @AfterThrowing 执行")
                .contains("Before service method execution", "method error.");

        assertThat(messages)
                .as("异常路径下 @AfterReturning 不应执行")
                .filteredOn(message -> message.startsWith("After service method execution"))
                .isEmpty();
    }

    /** 取代理链里所有的事务拦截器；空列表说明这个 bean 没有被事务增强。 */
    private static List<TransactionInterceptor> transactionInterceptorsOf(Object bean) {
        if (!(bean instanceof Advised advised)) {
            return List.of();
        }
        return Arrays.stream(advised.getAdvisors())
                .map(Advisor::getAdvice)
                .filter(TransactionInterceptor.class::isInstance)
                .map(TransactionInterceptor.class::cast)
                .toList();
    }

    private void recordDao(String method) {
        daoStates.add(method
                + " active=" + TransactionSynchronizationManager.isActualTransactionActive()
                + " readOnly=" + TransactionSynchronizationManager.isCurrentTransactionReadOnly());
    }
}
