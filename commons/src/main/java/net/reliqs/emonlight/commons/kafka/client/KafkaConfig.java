package net.reliqs.emonlight.commons.kafka.client;

import net.reliqs.emonlight.commons.kafka.utils.KafkaZkClient;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.DoubleDeserializer;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

import java.util.HashMap;
import java.util.Map;

@ConditionalOnProperty(name = "kafka.enabled")
@Configuration
public class KafkaConfig {

    @Value("${kafka.bootstrap.servers}")
    private String bootstrapServers;

    @Value("${kafka.consumerGroup}")
    private String consumerGroup;

    @Value("${kafka.zookeeperHosts}")
    private String zookeeperHosts;

//	private String[] topics;

    @Bean
    Map<String, Object> consumerConfigs() {
        Map<String, Object> props = new HashMap<>();
        // list of host:port pairs used for establishing the initial connections
        // to the Kakfa cluster
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, DoubleDeserializer.class);
        // consumer groups allow a pool of processes to divide the work of
        // consuming and processing records
        props.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroup);

        return props;
    }

    @Bean
    ConsumerFactory<Long, Double> consumerFactory() {
        return new DefaultKafkaConsumerFactory<>(consumerConfigs());
    }

    @Bean
    @Scope("prototype")
    KafkaListenerContainerBuilder kafkaListenerContainerBuilder() {
        return new KafkaListenerContainerBuilder(consumerFactory());
    }

    @Bean
    KafkaZkClient kafkaZkClient() {
        return new KafkaZkClient(zookeeperHosts);
    }

}
