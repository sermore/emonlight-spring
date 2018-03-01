package net.reliqs.emonlight.xbeegw;

import net.reliqs.emonlight.xbeegw.config.Probe;
import net.reliqs.emonlight.xbeegw.config.Settings;
import net.reliqs.emonlight.xbeegw.publish.Data;
import net.reliqs.emonlight.xbeegw.publish.Publisher;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Instant;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {TestApp.class})
@ActiveProfiles({"integration", "jpa"})
//@ComponentScan(basePackages = {"net.reliqs.emonlight.xbeegw.config", "net.reliqs.emonlight.xbeegw.send",
//        "net.reliqs.emonlight.xbeegw.publish"})
//@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class, JmsAutoConfiguration.class})
//@EnableConfigurationProperties
//@EnableAsync
@EnableCaching
public class JpaConfigurationTest {

    @Autowired
    ApplicationContext ctx;

    @Autowired
    Publisher publisher;
    @Autowired
    Settings settings;

    @Test
    public void test() {
        assertThat(ctx.containsBean("jmsConfiguration"), is(false));
        assertThat(ctx.containsBean("jmsService"), is(false));
        assertThat(ctx.containsBean("influxdbService"), is(false));
        assertThat(ctx.containsBean("jpaService"), is(true));
        assertThat(publisher.getServices(), hasSize(1));
    }

    @Test
    @DirtiesContext
    public void testPreDestroy() {
        Probe p = settings.getProbes().findFirst().get();
        publisher.publish(p, p.getType(), new Data(Instant.now().toEpochMilli(), 145.43));
        publisher.publish(p, p.getType(), new Data(Instant.now().toEpochMilli(), 185.43));
    }

}
