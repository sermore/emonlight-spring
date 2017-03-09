package net.reliqs.emonlight.xbeegw.send.jms;

import net.reliqs.emonlight.commons.xbee.Data;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

/**
 * Created by sergio on 2/28/17.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {JmsConfiguration.class})
@EnableAutoConfiguration
@EnableAsync
@ActiveProfiles("jms")
public class JmsServiceTest {

    @Component
    static class Receiver {

        @JmsListener(destination = "test")
        public void receiveMessage(Data data) {
            System.out.println("Received <" + data + ">");
        }

    }

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    MessageConverter messageConverter;

    @Test
    public void test() {
        jmsTemplate.send("test", new MessageCreator() {
            @Override
            public Message createMessage(Session session) throws JMSException {
                return messageConverter.toMessage(new Data(1, 12.5), session);
            }
        });
        jmsTemplate.convertAndSend("test", new Data(1, 12.5));
    }

}
