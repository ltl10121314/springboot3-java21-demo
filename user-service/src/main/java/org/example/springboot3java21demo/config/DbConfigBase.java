package org.example.springboot3java21demo.config;

import org.example.springboot3java21demo.interceptor.MybatisResultSetIntercept;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;
import java.io.IOException;

@Configuration
@MapperScan(
        basePackages = {"org.example.springboot3java21demo.mapper", "org.example.**.mapper"}
        , sqlSessionFactoryRef = "sqlSessionFactory"
        , sqlSessionTemplateRef = "sqlSession"
)
public class DbConfigBase {

    @Value("${mybatis.mapper-locations}")
    private String mapperLocations;

    /**
     * 配置sqlSessionFactory
     *
     * @param dataSource 数据源
     */
    @Bean("sqlSessionFactory")
    public SqlSessionFactoryBean sqlSessionFactory(
            @Qualifier("dataSource") DataSource dataSource,
            MybatisResultSetIntercept mybatisResultSetIntercept
    ) throws IOException {
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(dataSource);
        // 配置mybatis的mapper
        sqlSessionFactoryBean.setMapperLocations(
                new PathMatchingResourcePatternResolver().getResources(mapperLocations)
        );
        // 配置mybatis的config
        sqlSessionFactoryBean.setConfigLocation(
                new PathMatchingResourcePatternResolver().getResource("classpath:mybatis/mybatis-config.xml")
        );
        sqlSessionFactoryBean.setPlugins(mybatisResultSetIntercept);
        return sqlSessionFactoryBean;
    }

    @Primary
    @Bean("sqlSession")
    public SqlSessionTemplate sqlSession(@Qualifier("sqlSessionFactory") SqlSessionFactoryBean sqlSessionFactoryBean) throws Exception {
        return new SqlSessionTemplate(sqlSessionFactoryBean.getObject());
    }

//    /**
//     * 事务管理
//     *
//     * @param dataSource
//     * @return
//     */
//    @Bean("transactionManager")
//    public DataSourceTransactionManager transactionManager(@Qualifier("mainDataSource") DataSource dataSource) {
//        DataSourceTransactionManager manager = new DataSourceTransactionManager();
//        manager.setDataSource(dataSource);
//        return manager;
//    }

//    @Bean("objectMapper")
//    public ObjectMapperFactory objectMapper() {
//        return new ObjectMapperFactory("yyyy-MM-dd HH:mm:ss");
//    }
}
