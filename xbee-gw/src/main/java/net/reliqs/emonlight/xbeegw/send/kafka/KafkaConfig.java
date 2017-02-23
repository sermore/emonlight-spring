package net.reliqs.emonlight.xbeegw.send.kafka;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.DoubleSerializer;
import org.apache.kafka.common.serialization.LongSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

@Configuration
public class KafkaConfig {

	@Value("${kafka.bootstrap.servers}")
	private String bootstrapServers;

	@Bean
	public Map<String, Object> producerConfigs() {
		Map<String, Object> props = new HashMap<>();
		// list of host:port pairs used for establishing the initial connections
		// to the Kakfa cluster
		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
		props.put(ProducerConfig.RETRIES_CONFIG, 0);
		props.put(ProducerConfig.BATCH_SIZE_CONFIG, 1024);
		props.put(ProducerConfig.LINGER_MS_CONFIG, 1);
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class);
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, DoubleSerializer.class);
		// value to block, after which it will throw a TimeoutException
		props.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, 5000);

		return props;
	}

	@Bean
	public ProducerFactory<Long, Double> producerFactory() {
		return new DefaultKafkaProducerFactory<>(producerConfigs());
	}

	@Bean
	public KafkaTemplate<Long, Double> kafkaTemplate() {
		return new KafkaTemplate<>(producerFactory());
	}
	
}
