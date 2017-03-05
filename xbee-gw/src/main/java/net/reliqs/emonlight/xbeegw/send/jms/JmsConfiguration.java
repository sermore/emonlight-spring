package net.reliqs.emonlight.xbeegw.send.jms;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
//@ConditionalOnProperty(name = "jms.enabled", matchIfMissing = true, havingValue = "")
@EnableJms
public class JmsConfiguration {

    @Bean
    ConnectionFactory connectionFactory(@Value("${spring.activemq.broker-url:vm://localhost?broker.persistent=false}") String brokerUrl) {
        return new CachingConnectionFactory(new ActiveMQConnectionFactory(brokerUrl));
    }

    @Bean // Serialize message content to json using TextMessage
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
    JmsService jmsService(JmsAsyncService jmsAsyncService) {
        return new JmsService(jmsAsyncService);
    }

}
