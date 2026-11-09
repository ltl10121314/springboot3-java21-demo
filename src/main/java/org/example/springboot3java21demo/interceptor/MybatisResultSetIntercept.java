package org.example.springboot3java21demo.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.resultset.ResultSetHandler;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.springframework.stereotype.Component;

import java.sql.Statement;
import java.util.Properties;

/**
 * <p>mybatis 拦截器</p>
 * <p>实现方式1：</p>
 * <pre>{@code
 * @Configuration
 * public class MyBatisSqlInterceptorConfiguration implements ApplicationContextAware {
 *
 *     @Override
 *     public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
 *         SqlSessionFactory sqlSessionFactory = applicationContext.getBean(SqlSessionFactory.class);
 *         sqlSessionFactory.getConfiguration().addInterceptor(new MybatisResultSetIntercept());
 *     }
 * }
 * }</pre>
 * <p>注册方式2：</p>
 * <pre>{@code
 * @Configuration
 * @MapperScan("org.example.springboot3java21demo.interceptor" )
 * public class MyBatisPlusConfig {
 *
 *     @Autowired
 *     private ApplicationContext applicationContext;
 *
 *     @Bean
 *     public String myInterceptor(SqlSessionFactory sqlSessionFactory){
 *         sqlSessionFactory.getConfiguration().addInterceptor(resultSetIntercept());
 *         return "myInterceptor";
 *     }
 *
 *     public MybatisResultSetIntercept resultSetIntercept(){
 *         return applicationContext.getAutowireCapableBeanFactory().createBean(MybatisResultSetIntercept.class);
 *     }
 * }
 * }</pre>
 */
@Intercepts(
        {
                @Signature(type = ResultSetHandler.class, method = "handleResultSets", args = {Statement.class})
        }
)
@Component
@Slf4j
public class MybatisResultSetIntercept implements Interceptor {
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        // 在这里可以访问或修改typeHandlerMap等属性
        log.info("---------before-mybatisIntercept----------");
        Object result = invocation.proceed();
        log.info("---------after-mybatisIntercept----------");
        return result;
    }

    @Override
    public void setProperties(Properties properties) {
        log.info("---------setProperties----------");
    }
}
