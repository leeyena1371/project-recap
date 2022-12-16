package co.project.api.common.config.mybatis;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
@MapperScan(basePackages = {"co.dearu.live.api.domain.coin.repository","co.dearu.live.api.domain.item.repository" }, sqlSessionFactoryRef = "storeSqlSessionFactory")
public class MybatisStoreConfig {

    @Autowired
    ApplicationContext applicationContext;

    @Bean(name = "storeDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.store")
    public DataSource storeDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "storeSqlSessionFactory")
    public SqlSessionFactory storeSqlSessionFactory(@Qualifier("storeDataSource") DataSource storeDataSource) throws Exception {
        SqlSessionFactoryBean storeSqlSessionFactoryBean = new SqlSessionFactoryBean();
        storeSqlSessionFactoryBean.setDataSource(storeDataSource);
        storeSqlSessionFactoryBean.setMapperLocations(applicationContext.getResources("classpath:/mapper/store/*.xml"));
        return storeSqlSessionFactoryBean.getObject();
    }
    //--------------------------------- SQL TransactionManager -------------------------------------
    @Bean(name = "store")
    public PlatformTransactionManager storeTransactionManager(@Qualifier("storeDataSource") DataSource storeDataSource) {
        return new DataSourceTransactionManager(storeDataSource);
    }
    //--------------------------------- SQL SessionTemplate -------------------------------------
    @Bean(name = "storeSessionTemplate")
    public SqlSessionTemplate storeSqlSessionTemplate(@Qualifier("storeSqlSessionFactory") SqlSessionFactory storeSqlSessionFactory) {
        return new SqlSessionTemplate(storeSqlSessionFactory);
    }
}

//@Configuration
//@EnableTransactionManagement
//@MapperScan(basePackages = "co.dear.admin.common.repository.dontalkadmin", sqlSessionFactoryRef = "dontalkadminSqlSessionFactory")
//public class MybatisDontalkadminConfig {
//
//    //--------------------------------- SQL DataSource -------------------------------------
//    @Primary
//    @Bean(name="dontalkadminDataSource")
//    @ConfigurationProperties(prefix = "datasource")
//    public DataSource dontalkadminDataSource() {
//        return DataSourceBuilder.create().build();
//    }
//
//    //--------------------------------- SQL TransactionManager -------------------------------------
//    @Bean(name = "dontalkadminSqlSessionFactory")
//    public SqlSessionFactory dontalkadminSqlSesstionFactory(@Qualifier("dontalkadminDataSource") DataSource dontalkadminDataSource, ApplicationContext applicationContext) throws Exception {
//        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
//        sqlSessionFactoryBean.setDataSource(dontalkadminDataSource);
//        sqlSessionFactoryBean.setMapperLocations(applicationContext.getResource("classpath:/mapper/dontalkadmin/*Mapper.xml"));
//        return sqlSessionFactoryBean.getObject();
//    }
//
//    //--------------------------------- SQL SessionTemplate -------------------------------------
//    @Bean(name = "dontalkadminTranscationManager")
//    public SqlSessionTemplate dontalkadminSqlSessionTemplate(@Qualifier("dontalkadminSqlSessionFactory") SqlSessionFactory dontalkadminSqlSessionFactory) throws Exception {
//        return new SqlSessionTemplate(dontalkadminSqlSessionFactory);
//    }
//}
