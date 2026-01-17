package org.example.springboot3java21demo.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.resultset.ResultSetHandler;
import org.apache.ibatis.executor.resultset.ResultSetWrapper;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.springframework.stereotype.Component;

import java.beans.Statement;
import java.util.Properties;

/**
 * mybatis 拦截器
 */
@Intercepts(
        {
                @Signature(type = ResultSetHandler.class, method = "handleResultSets", args = Statement.class)
        }
)
@Component
@Slf4j
public class MybatisResultSetIntercept implements Interceptor {
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        ResultSetWrapper rsw = (ResultSetWrapper) invocation.getArgs()[0];
        // 在这里可以访问或修改typeHandlerMap等属性
        System.out.println("---------intercept----------");
        return invocation.proceed();
    }

    @Override
    public Object plugin(Object o) {
        System.out.println("---------plugin----------");
        return o;
    }

    @Override
    public void setProperties(Properties properties) {
        System.out.println("---------setProperties----------");
    }
}
