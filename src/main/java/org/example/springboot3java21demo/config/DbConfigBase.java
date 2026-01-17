package org.example.springboot3java21demo.config;

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

//    //替换为自己服务的配置
//    @Value("${db.main.ref:yonbip-hr-paybiz_mddDataSource}")
//    private String dbMainRef;

//    //替换为自己服务的配置
//    @Value("${db.uimeta.ref:yonbip-hr-paybiz_uimeta}")
//    private String dbUimetaRef;

//    @Autowired(required = false)
//    @Qualifier("ymsProvider")
//    private YMSDataSourceProviderItf ymsDataSourceProvider;

//    /**
//     * 主数据
//     *
//     * @return
//     */
//    @Primary
//    @Bean("mainDataSource")
//    public DataSource mainDataBase() {
//        YMSDataSource dataSource = new YMSDataSource();
//        dataSource.setDsProvider(ymsDataSourceProvider);
//        dataSource.setLogicDataSource(dbMainRef);
//        dataSource.init();
//        return dataSource;

//    }

    /**
     * 配置sqlSessionFactory,mybatis-spring.jar
     *
     * @param dataSource
     * @return
     * @throws IOException
     */
    @Bean("sqlSessionFactory")
    public SqlSessionFactoryBean sqlSessionFactory(
            @Qualifier("dataSource") DataSource dataSource
    ) throws IOException {
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(dataSource);
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        //配置mybatis的mapper路径
        Resource[] resources = resolver.getResources(mapperLocations);
        sqlSessionFactoryBean.setMapperLocations(resources);
        //309特性修改
        sqlSessionFactoryBean.setConfigLocation(new PathMatchingResourcePatternResolver()
                .getResource("classpath:mybatis/mybatis-config.xml"));
        return sqlSessionFactoryBean;
    }

    @Bean("sqlSession")
    @Primary
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

//    @Bean("uimeta")
//    public DataSource uimeta() {
//        YMSDataSource dataSource = new YMSDataSource();
//        dataSource.setDsProvider(ymsDataSourceProvider);
//        dataSource.setLogicDataSource(dbUimetaRef);
//        dataSource.init();
//        return dataSource;
//    }

//    /**
//     * uimeta数据源session
//     *
//     * @param dataSource
//     * @return
//     * @throws IOException
//     */
//    @Bean("sqlSessionFactory4uimeta")
//    public SqlSessionFactoryBean sqlSessionFactory4uimeta(@Qualifier("uimeta") DataSource dataSource) throws IOException {
//        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
//        //数据库连接池
//        sqlSessionFactoryBean.setDataSource(dataSource);
//        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
//        Resource[] resources = resolver.getResources(mapperLocations);
//        sqlSessionFactoryBean.setMapperLocations(resources);
//        return sqlSessionFactoryBean;
//    }

//    @Bean("sqlSession4uimeta")
//    public SqlSessionTemplate sqlSession4uimeta(@Qualifier("sqlSessionFactory4uimeta") SqlSessionFactoryBean sqlSessionFactoryBean)
//            throws Exception {
//        return new SqlSessionTemplate(sqlSessionFactoryBean.getObject());
//    }


//    @Bean("objectMapper")
//    public ObjectMapperFactory objectMapper() {
//        return new ObjectMapperFactory("yyyy-MM-dd HH:mm:ss");
//    }
//
//    @Bean
//    public DefaultJdbcTemplate defaultJdbcTemplate() {
//        DefaultJdbcTemplate template = new DefaultJdbcTemplate();
//        template.setDataSource(mainDataBase());
//        return template;
//    }

//    @Bean
//    public IdManager idManager() {
//        return new IdManager((long) 1, (long) 1);
//    }
}
