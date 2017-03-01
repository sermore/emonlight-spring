package net.reliqs.emonlight.xbeegw.send.activemq;

import net.reliqs.emonlight.commons.xbee.Data;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

/**
 * Created by sergio on 2/28/17.
 */
@RunWith(SpringRunner.class)
@SpringBootApplication
@ActiveProfiles("jms")
public class TestActiveMQ {

    @Configuration
    @EnableJms
    static class MyConfig {

        @Bean
        public DefaultJmsListenerContainerFactory jmsListenerContainerFactory(ConnectionFactory connectionFactory) {
            DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
            factory.setConnectionFactory(connectionFactory);
            factory.setConcurrency("1-1");
            return factory;
        }

        @Bean // Serialize message content to json using TextMessage
        public MessageConverter messageConverter() {
            MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
            converter.setTargetType(MessageType.TEXT);
            converter.setTypeIdPropertyName("_type");
            return converter;
        }
    }

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
