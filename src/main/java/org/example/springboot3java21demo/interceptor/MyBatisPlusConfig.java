package org.example.springboot3java21demo.interceptor;//package org.example.springboot3java21demo.interceptor;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("org.example.springboot3java21demo.interceptor" )
public class MyBatisPlusConfig {

    @Autowired
    private ApplicationContext applicationContext;

    /**
     * 注册MyBatis拦截器
     * @param sqlSessionFactory
     * @return
     */
    @Bean
    public String myInterceptor(SqlSessionFactory sqlSessionFactory){
        sqlSessionFactory.getConfiguration().addInterceptor(resultSetIntercept());
        return "myInterceptor";
    }

    public MybatisResultSetIntercept resultSetIntercept(){
        return applicationContext.getAutowireCapableBeanFactory().createBean(MybatisResultSetIntercept.class);
    }
}
