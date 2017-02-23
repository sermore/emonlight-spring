package net.reliqs.emonlight.streams.streams;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.streams.processor.TimestampExtractor;

public class EventTimeExtractor implements TimestampExtractor {

	@Override
	public long extract(ConsumerRecord<Object, Object> record) {
		return (long) record.key();
	}

}
