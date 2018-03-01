package net.reliqs.emonlight.xbeegw.send.jms;

import net.reliqs.emonlight.xbeegw.publish.Publisher;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jms.JmsProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;

import javax.jms.ConnectionFactory;

/**
 * Created by sergio on 02/03/17.
 */
@ConditionalOnProperty(name = "jms.enabled")
@Configuration
//@EnableConfigurationProperties
//@EnableJms
public class JmsConfiguration {

    private Publisher publisher;

    @Bean
    @ConfigurationProperties(prefix = "spring.jms")
    JmsProperties jmsProperties() {
        return new JmsProperties();
    }

    @Value("${spring.activemq.broker-url:vm://localhost?broker.persistent=false}")
    private String brokerUrl;

    public JmsConfiguration(Publisher publisher) {
        this.publisher = publisher;
    }

    @Bean
    ConnectionFactory connectionFactory() {
        return new CachingConnectionFactory(new ActiveMQConnectionFactory(brokerUrl));
    }

    @Bean
    JmsTemplate jmsTemplate(ConnectionFactory connectionFactory) {
        JmsTemplate jmsTemplate = new JmsTemplate(connectionFactory);
        JmsProperties properties = jmsProperties();
        jmsTemplate.setPubSubDomain(properties.isPubSubDomain());
//        DestinationResolver destinationResolver = this.destinationResolver
//                .getIfUnique();
//        if (destinationResolver != null) {
//            jmsTemplate.setDestinationResolver(destinationResolver);
//        }
        jmsTemplate.setMessageConverter(jacksonJmsMessageConverter());
//        MessageConverter messageConverter = this.messageConverter.getIfUnique();
//        if (messageConverter != null) {
//            jmsTemplate.setMessageConverter(messageConverter);
//        }
        JmsProperties.Template template = properties.getTemplate();
        if (template.getDefaultDestination() != null) {
            jmsTemplate.setDefaultDestinationName(template.getDefaultDestination());
        }
        if (template.getDeliveryDelay() != null) {
            jmsTemplate.setDeliveryDelay(template.getDeliveryDelay());
        }
        jmsTemplate.setExplicitQosEnabled(template.determineQosEnabled());
        if (template.getDeliveryMode() != null) {
            jmsTemplate.setDeliveryMode(template.getDeliveryMode().getValue());
        }
        if (template.getPriority() != null) {
            jmsTemplate.setPriority(template.getPriority());
        }
        if (template.getTimeToLive() != null) {
            jmsTemplate.setTimeToLive(template.getTimeToLive());
        }
        if (template.getReceiveTimeout() != null) {
            jmsTemplate.setReceiveTimeout(template.getReceiveTimeout());
        }
        return jmsTemplate;
    }

    @Bean
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

    @Bean(initMethod = "onInit", destroyMethod = "onClose")
    JmsService jmsService(JmsAsyncService jmsAsyncService) {
        JmsService s = new JmsService(jmsAsyncService);
        publisher.addService(s);
        return s;
    }

}
