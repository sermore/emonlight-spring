package net.reliqs.emonlight.xbeegw.send.jms;

import net.reliqs.emonlight.xbeegw.config.Probe;
import net.reliqs.emonlight.xbeegw.config.Settings;
import net.reliqs.emonlight.xbeegw.publish.Data;
import net.reliqs.emonlight.xbeegw.publish.Publisher;
import net.reliqs.emonlight.xbeegw.send.StoreData;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Created by sergio on 2/28/17.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {Settings.class, JmsConfiguration.class, Publisher.class})
@EnableAutoConfiguration
@EnableAsync
@ActiveProfiles("jms")
public class JmsServiceTest {

    @Autowired
    Settings settings;
    @Autowired
    MessageConverter messageConverter;
    @Autowired
    Receiver receiver;
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

    @Component
    static class Receiver {
        boolean received;

        @JmsListener(destination = "test")
        public void receiveMessage(StoreData data) {
            System.out.println("Received <" + data + ">");
            received = true;
        }

    }

}
