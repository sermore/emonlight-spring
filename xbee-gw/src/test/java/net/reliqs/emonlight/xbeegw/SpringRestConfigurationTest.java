package net.reliqs.emonlight.xbeegw;

import net.reliqs.emonlight.xbeegw.publish.Publisher;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jms.JmsAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@ActiveProfiles("rest")
@RunWith(SpringRunner.class)
@SpringBootTest
@ComponentScan(basePackages = {"net.reliqs.emonlight.xbeegw.config", "net.reliqs.emonlight.xbeegw.send",
        "net.reliqs.emonlight.xbeegw.publish"})
//@EnableAutoConfiguration
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class, JmsAutoConfiguration.class})
@EnableConfigurationProperties
@EnableAsync
public class SpringRestConfigurationTest {

    @Autowired
    ApplicationContext ctx;

    @Autowired
    Publisher publisher;

    @Test
    public void test() {
        assertThat(ctx.containsBean("jmsConfiguration"), is(false));
        assertThat(ctx.containsBean("jmsService"), is(false));
        assertThat(ctx.containsBean("influxdbService"), is(false));
        assertThat(ctx.containsBean("jpaService"), is(false));
        assertThat(publisher.getServices(), hasSize(1));
    }

}
