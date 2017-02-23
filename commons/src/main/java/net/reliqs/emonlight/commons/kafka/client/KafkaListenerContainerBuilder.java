package net.reliqs.emonlight.commons.kafka.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.config.ContainerProperties;
import org.springframework.kafka.support.TopicPartitionInitialOffset;

public class KafkaListenerContainerBuilder {

	TopicPartitionInitialOffset[] topics;
	
	private ConsumerFactory<Long, Double> consumerFactory;

	@Autowired
	public KafkaListenerContainerBuilder(ConsumerFactory<Long, Double> consumerFactory) {
		this.consumerFactory = consumerFactory;
	}

	@Bean
	@Scope("prototype")
	private KafkaMessageListenerContainer<Long, Double> kafkaListenerContainer() {
		ContainerProperties containerProperties = new ContainerProperties(topics);
		KafkaMessageListenerContainer<Long, Double> container = new KafkaMessageListenerContainer<>(consumerFactory,
				containerProperties);
		return container;
	}

	public KafkaMessageListenerContainer<Long, Double> getContainer(String topic, int partition, long initialOffset,
			boolean relativetoCurrent) {
		topics = new TopicPartitionInitialOffset[] { new TopicPartitionInitialOffset(topic, partition, initialOffset, relativetoCurrent) };
		return kafkaListenerContainer();
	}
	
	public KafkaMessageListenerContainer<Long, Double> getContainer(TopicPartitionInitialOffset... topics) {
		this.topics = topics;
		return kafkaListenerContainer();		
	}

}
