package net.reliqs.emonlight.xbeegw;

import net.reliqs.emonlight.xbeegw.publish.Publisher;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {TestApp.class})
@ActiveProfiles({"integration", "rest"})
public class RestConfigurationTest {

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
        assertThat(publisher.getServices(), hasSize(2));
    }

}
