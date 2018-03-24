package net.reliqs.emonlight.xbeegw.send.jms;

import net.reliqs.emonlight.commons.config.Probe;
import net.reliqs.emonlight.commons.config.Settings;
import net.reliqs.emonlight.xbeegw.TestApp;
import net.reliqs.emonlight.xbeegw.publish.Data;
import net.reliqs.emonlight.xbeegw.send.StoreData;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jms.DefaultJmsListenerContainerFactoryConfigurer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import javax.jms.ConnectionFactory;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Created by sergio on 2/28/17.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestApp.class)
@ActiveProfiles("integration,jms")
@EnableJms
public class JmsServiceTest {

    @Autowired
    Settings settings;
    @Autowired
    Receiver receiver;
    @Autowired
    JmsService service;
    @Autowired
    private JmsTemplate jmsTemplate;

    @Test
    public void test() throws InterruptedException {
        Probe probe = settings.getProbes().findFirst().get();
        StoreData s = new StoreData(probe, probe.getType(), new Data(1, 12.5));
        jmsTemplate.convertAndSend("test", s);
        Thread.sleep(1000);
        assertThat(receiver.received, is(true));

    }

    @Test
    public void serviceTest() throws InterruptedException {
        Receiver.count = 0;
        Probe p = settings.getProbes().findFirst().get();
        assertThat(service.isReady(), is(false));
        Data in = new Data(100, 10.0);
        service.receive(p, p.getType(), in);
        assertThat(service.isReady(), is(true));
        in = new Data(200, 20.0);
        service.receive(p, p.getType(), in);
        service.post();
        Thread.sleep(1000);
        assertThat(service.isReady(), is(false));
//        assertThat(service.isEmpty(), is(true));
        in = new Data(300, 30.0);
        service.receive(p, p.getType(), in);
        service.post();
        assertThat(service.isReady(), is(false));
        Thread.sleep(1000);
        assertThat(service.isReady(), is(false));
        assertThat(Receiver.count, is(3));
    }

    @TestConfiguration
    static class TestJmsListenerConf {

        @Autowired
        MessageConverter messageConverter;

        @Bean
        public DefaultJmsListenerContainerFactoryConfigurer configurer() {
            return new DefaultJmsListenerContainerFactoryConfigurer();
        }

        @Bean
        public DefaultJmsListenerContainerFactory jmsListenerContainerFactory(ConnectionFactory connectionFactory) {
            DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
            factory.setConnectionFactory(connectionFactory);
            factory.setMessageConverter(messageConverter);
            return factory;
        }

        @Bean
        Receiver receiver() {
            return new Receiver();
        }
    }
}
