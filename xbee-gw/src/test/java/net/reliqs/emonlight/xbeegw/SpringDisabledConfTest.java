package net.reliqs.emonlight.xbeegw;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import net.reliqs.emonlight.xbeegw.publish.Publisher;

@ActiveProfiles("test-settings")
@RunWith(SpringRunner.class)
@SpringBootTest
@ComponentScan(basePackages = { "net.reliqs.emonlight.xbeegw.config", "net.reliqs.emonlight.xbeegw.send",
"net.reliqs.emonlight.xbeegw.publish" })
@EnableAutoConfiguration
@EnableConfigurationProperties
public class SpringDisabledConfTest {

	@Autowired
	ApplicationContext ctx;

	@Autowired
	Publisher publisher;

	@Test
	public void test() {
		assertThat(ctx.containsBean("jmsConfiguration"), is(false));
		assertThat(ctx.containsBean("jmsService"), is(false));
		assertThat(ctx.containsBean("influxdbService"), is(false));
		assertThat(publisher.getServices(), hasSize(1));
	}

}
