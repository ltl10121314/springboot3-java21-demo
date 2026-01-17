package org.example.springboot3java21demo.exercise.pointcut;

import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * 控制层请求响应，日志切面
 */
@Component
@Aspect
public class RequestLogAspect {
    private final static Logger logger = LoggerFactory.getLogger(RequestLogAspect.class);

    @Pointcut("execution(* org.example.springboot3java21demo.controller..*(..))")
    public void requestServer() {
    }

    @Before("requestServer()")
    public void doBefore(JoinPoint joinPoint) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            logger.info("===============================Start========================");
            logger.info("IP                 : {}", request.getRemoteAddr());
            logger.info("URL                : {}", request.getRequestURL().toString());
            logger.info("HTTP Method        : {}", request.getMethod());
            logger.info("Class Method       : {}.{}", joinPoint.getSignature().getDeclaringTypeName(), joinPoint.getSignature().getName());

        }
    }

    @Around("requestServer()")
    public Object doAround(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        Object result = proceedingJoinPoint.proceed();
        if (result != null) {
            logger.info("Request Params     : {}", getRequestParams(proceedingJoinPoint));
            logger.info("Result             : {}", result);
            logger.info("Time Cost          : {} ms", System.currentTimeMillis() - start);

        }
        return result;
    }

    @After("requestServer()")
    public void doAfter(JoinPoint joinPoint) {
        logger.info("===============================End========================");
    }

    /**
     * 获取入参
     *
     * @param proceedingJoinPoint
     * @return
     */
    private Map<String, Object> getRequestParams(ProceedingJoinPoint proceedingJoinPoint) {
        Map<String, Object> requestParams = new HashMap<>();
        //参数名
        String[] paramNames = ((MethodSignature) proceedingJoinPoint.getSignature()).getParameterNames();
        //参数值
        Object[] paramValues = proceedingJoinPoint.getArgs();
        for (int i = 0; i < paramNames.length; i++) {
            Object value = paramValues[i];
            //如果是文件对象
            if (value instanceof MultipartFile) {
                MultipartFile file = (MultipartFile) value;
                value = file.getOriginalFilename();  //获取文件名
            }
            requestParams.put(paramNames[i], value);
        }
        return requestParams;
    }
}
