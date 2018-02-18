package net.reliqs.emonlight.xbeegw.send.jpa;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.AbstractJpaVendorAdapter;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.LinkedHashMap;
import java.util.Map;

@ConditionalOnProperty(name = "jpa.enabled")
@Configuration
//@EnableTransactionManagement
//@EntityScan("net.reliqs.emonlight.xbeegw.send.jpa")
public class JpaConfiguration {
//    private static final Logger log = LoggerFactory.getLogger(JpaConfiguration.class);

    @Bean
//    @ConfigurationProperties(prefix = "spring.jpa")
    JpaProperties jpaProperties() {
        return new JpaProperties();
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource")
    DataSource dataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    PlatformTransactionManager transactionManager() {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
//        if (this.transactionManagerCustomizers != null) {
//            this.transactionManagerCustomizers.customize(transactionManager);
//        }
        return transactionManager;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        DataSource dataSource = dataSource();
        JpaProperties properties = jpaProperties();
//        log.debug("PROPS {}", properties.getProperties());
        EntityManagerFactoryBuilder builder = entityManagerFactoryBuilder(dataSource, properties);
        Map<String, Object> vendorProperties = getVendorProperties(dataSource, properties);
        return builder.dataSource(dataSource).packages("net.reliqs.emonlight.xbeegw.send.jpa")
                .properties(vendorProperties).build();
    }

    private EntityManagerFactoryBuilder entityManagerFactoryBuilder(DataSource dataSource, JpaProperties properties) {
        EntityManagerFactoryBuilder builder = new EntityManagerFactoryBuilder(
                jpaVendorAdapter(dataSource, properties), properties.getProperties(),
                null);
//        builder.setCallback(getVendorCallback());
        return builder;
    }

    private Map<String, Object> getVendorProperties(DataSource dataSource, JpaProperties properties) {
        Map<String, Object> vendorProperties = new LinkedHashMap<String, Object>();
        vendorProperties.putAll(properties.getHibernateProperties(dataSource));
        return vendorProperties;
    }


    private JpaVendorAdapter jpaVendorAdapter(DataSource dataSource, JpaProperties properties) {
        AbstractJpaVendorAdapter adapter = new HibernateJpaVendorAdapter();
        adapter.setShowSql(properties.isShowSql());
        adapter.setDatabase(properties.determineDatabase(dataSource));
        adapter.setDatabasePlatform(properties.getDatabasePlatform());
        adapter.setGenerateDdl(properties.isGenerateDdl());
        return adapter;
    }

}
