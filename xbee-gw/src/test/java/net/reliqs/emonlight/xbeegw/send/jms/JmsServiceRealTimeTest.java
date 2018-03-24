package net.reliqs.emonlight.xbeegw.send.jms;

import net.reliqs.emonlight.commons.config.Probe;
import net.reliqs.emonlight.commons.config.Settings;
import net.reliqs.emonlight.xbeegw.TestApp;
import net.reliqs.emonlight.xbeegw.publish.Data;
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
@ActiveProfiles("integration,jmsrt")
@EnableJms
public class JmsServiceRealTimeTest {

    @Autowired
    Settings settings;
    @Autowired
    Receiver receiver;
    @Autowired
    JmsService service;
    @Autowired
    private JmsTemplate jmsTemplate;

    @Test
    public void serviceTest() throws InterruptedException {
        Probe p = settings.getProbes().findFirst().get();
        assertThat(service.isReady(), is(false));
        service.receive(p, p.getType(), new Data(100, 10.0));
        service.receive(p, p.getType(), new Data(110, 14.0));
        service.receive(p, p.getType(), new Data(120, 15.0));
        assertThat(service.isReady(), is(false));
        assertThat(service.isRunning(), is(true));
        Thread.sleep(500);
        assertThat(service.isRunning(), is(false));
        assertThat(service.isReady(), is(true));
        assertThat(service.isQueueEmpty(), is(false));
        assertThat(Receiver.count, is(1));
        service.post();
        Thread.sleep(500);
        assertThat(service.isRunning(), is(false));
        assertThat(service.isReady(), is(false));
        assertThat(service.isQueueEmpty(), is(true));
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
