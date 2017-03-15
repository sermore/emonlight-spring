package net.reliqs.emonlight.xbeegw.send.jms;

import net.reliqs.emonlight.xbeegw.publish.Publisher;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;

import javax.jms.ConnectionFactory;

/**
 * Created by sergio on 02/03/17.
 */
@Configuration
@ConditionalOnProperty(name = "jms.enabled", matchIfMissing = true, havingValue = "")
@EnableJms
public class JmsConfiguration {

    private Publisher publisher;

    @Autowired
    public JmsConfiguration(Publisher publisher) {
        this.publisher = publisher;
    }

    @Bean
    ConnectionFactory connectionFactory(@Value("${spring.activemq.broker-url:vm://localhost?broker.persistent=false}") String brokerUrl) {
        return new CachingConnectionFactory(new ActiveMQConnectionFactory(brokerUrl));
    }

    @Bean
        // Serialize message content to json using TextMessage
    MessageConverter jacksonJmsMessageConverter() {
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName("_type");
        return converter;
    }

    @Bean
    JmsAsyncService jmsAsyncService(JmsTemplate jmsTemplate) {
        return new JmsAsyncService(jmsTemplate);
    }

    @Bean
    @Order(10)
    JmsService jmsService(JmsAsyncService jmsAsyncService) {
        JmsService s = new JmsService(jmsAsyncService);
        publisher.addService(s);
        return s;
    }

}
