package org.example.springboot3java21demo.aop.log;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.example.springboot3java21demo.common.utils.JacksonUtil;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Date;

/**
 * <ol>
 *  <li>普通业务增强：优先考虑 @Aspect + @Around，代码直观，便于维护。</li>
 * <li>基础设施、通用框架或动态组装增强规则：可以直接实现 MethodInterceptor，并通过 Advisor 和 Pointcut 灵活组合。</li>
 * </ol>
 * <p>拦截器实现日志举例：</p>
 * <pre>{@code
 * @Slf4j
 * @Component
 * public class LogInterceptor implements MethodInterceptor {
 *
 *     @Override
 *     public Object invoke(MethodInvocation invocation) throws Throwable {
 *         log.info("方法执行前");
 *         Object result = invocation.proceed();
 *         log.info("方法执行后");
 *         return result;
 *     }
 * }
 * @Configuration
 * public class LogAopConfig {
 *
 *     @Bean
 *     public Advisor logAdvisor(LogInterceptor logInterceptor) {
 *         AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
 *         pointcut.setExpression("execution(* org.example.springboot3java21demo.service..*(..))");
 *         return new DefaultPointcutAdvisor(pointcut, logInterceptor);
 *     }
 * }
 * }</pre>
 */
@Aspect
@Component
@Slf4j
public class LogAspect {

    /**
     * <p>切面示例1:
     * <pre>{@code
     * @Pointcut(
     * value = "@annotation(org.example.springboot3java21demo.aop.log.MyLog)"
     * )
     * public void myLockPointcut() {
     * }
     *
     * @Around(
     * value = "myLockPointcut()"
     * )
     * public Object aroundAdvice(ProceedingJoinPoint joinPoint) throws Throwable {
     * // 前置逻辑
     * log.error("Before method execution with myLog annotation");
     * // 执行目标方法
     * Object result = joinPoint.proceed();
     * // 后置逻辑
     * log.error("After method execution with myLog annotation");
     * return result;
     * }
     * }</pre>
     */
    @Around(
            value = "@annotation(org.example.springboot3java21demo.aop.log.MyLog)"
    )
    public Object recordLog(ProceedingJoinPoint joinPoint) throws Throwable {
        //操作时间
        Date operateTime = new Date();
        //操作类名
        String className = joinPoint.getTarget().getClass().getName();
        //操作方法名
        String methodName = joinPoint.getSignature().getName();
        //操作方法参数
        Object[] args = joinPoint.getArgs();
        String methodParams = Arrays.toString(args);
        long begin = System.currentTimeMillis();
        log.info("Before-操作时间:{},操作类名:{},操作方法名:{},操作方法参数:{}",
                operateTime, className, methodName, methodParams);
        //调用原始目标方法运行
        Object result = joinPoint.proceed();
        long end = System.currentTimeMillis();
        //方法返回值
        String returnValue = JacksonUtil.getMapper().writeValueAsString(result);
        //操作耗时
        long costTime = end - begin;
        //记录操作日志
        log.debug("After-方法返回值:{},操作耗时{}", returnValue, costTime);
        return result;
    }

    @AfterReturning(
            value = "@annotation(org.example.springboot3java21demo.aop.log.MyLog)",
            returning = "returnValue"
    )
    public void returnValuePointcut(JoinPoint joinPoint, final Object returnValue) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        log.debug("returnValuePointcut-{}-{}", method.getName(), returnValue);
    }

    /**
     * <p>示例：指定方法的切面，包括：传递指定参数
     * <pre>{@code
     * @Pointcut( value = "execution(* org.example.springboot3java21demo.service.UserService.insert(..)) && args(user)",
     * argNames = "user"
     * )
     * public void insertPointcut(User user) {
     * }
     * @Before(value = "insertPointcut(user)", argNames = "user")
     * public void beforeUpdate(User user) {
     * log.info(user.toString());
     * }}</pre>
     */
    @Pointcut(value = "execution(* org.example.springboot3java21demo.service..*(..)) ")
    public void serviceMethods() {

    }

    @Before(value = "serviceMethods()")
    public void beforeServiceMethods(JoinPoint joinPoint) {
        log.info("Before service method execution");
    }

    @AfterReturning(pointcut = "serviceMethods()", returning = "result")
    public void afterServiceMethods(JoinPoint joinPoint, Object result) {
        log.debug("After service method execution, result: {}", result);
    }

    @AfterThrowing(value = "serviceMethods()", throwing = "throwing")
    public void throwsAdvice(JoinPoint joinPoint, Throwable throwing) {
        log.error("method error.", throwing);
    }
}
