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

    public JmsConfiguration(Publisher publisher) {
        this.publisher = publisher;
    }

    //    @Value("${spring.activemq.broker-url:vm://localhost?broker.persistent=false}")
    //    private String brokerUrl;

    @Bean
    @ConfigurationProperties(prefix = "spring.jms")
    JmsProperties jmsProperties() {
        return new JmsProperties();
    }

    @Bean
    ConnectionFactory connectionFactory(
            @Value("${spring.activemq.broker-url:vm://localhost?broker.persistent=false}") String brokerUrl) {
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
            jmsTemplate.setDeliveryDelay(template.getDeliveryDelay().toMillis());
        }
        jmsTemplate.setExplicitQosEnabled(template.determineQosEnabled());
        if (template.getDeliveryMode() != null) {
            jmsTemplate.setDeliveryMode(template.getDeliveryMode().getValue());
        }
        if (template.getPriority() != null) {
            jmsTemplate.setPriority(template.getPriority());
        }
        if (template.getTimeToLive() != null) {
            jmsTemplate.setTimeToLive(template.getTimeToLive().toMillis());
        }
        if (template.getReceiveTimeout() != null) {
            jmsTemplate.setReceiveTimeout(template.getReceiveTimeout().toMillis());
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
    JmsAsyncService jmsAsyncService(JmsTemplate jmsTemplate, @Value("${jms.maxRetries:0}") int maxRetries,
            @Value("${jms.ignoreErrors:false}") boolean ignoreErrors) {
        return new JmsAsyncService(jmsTemplate, maxRetries, ignoreErrors);
    }

    @Bean(initMethod = "onInit", destroyMethod = "onClose")
    JmsService jmsService(JmsAsyncService jmsAsyncService, @Value("${jms.enableBackup:true}") boolean enableBackup,
            @Value("${jms.backupPath:jmsServiceBackup.dat}") String backupPath,
            @Value("${jms.maxBatch:0}") int maxBatch, @Value("${jms.realTime:true}") boolean realTime,
            @Value("${jms.timeOutOnClose:2000}") long timeOutOnClose, @Value("${jms.maxQueued:0}") int maxQueued) {
        JmsService s = new JmsService(jmsAsyncService, enableBackup, backupPath, maxBatch, realTime, timeOutOnClose,
                maxQueued);
        publisher.addService(s);
        return s;
    }

}
