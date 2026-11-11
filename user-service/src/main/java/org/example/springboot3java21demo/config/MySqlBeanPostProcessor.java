package org.example.springboot3java21demo.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.Date;

@Slf4j
@Component
public class MySqlBeanPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof SqlSessionTemplate) {
            SqlSessionTemplate sqlSessionTemplate = (SqlSessionTemplate) bean;
            DataSource dataSource = sqlSessionTemplate.getConfiguration().getEnvironment().getDataSource();
            TypeHandlerRegistry typeHandlerRegistry = sqlSessionTemplate.getConfiguration().getTypeHandlerRegistry();
            typeHandlerRegistry.register(Date.class, JdbcType.BIGINT, new MddLong2DateTypeHandler());
        }
        return bean;
    }
}
