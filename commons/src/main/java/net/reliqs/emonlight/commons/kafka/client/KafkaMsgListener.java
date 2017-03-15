package net.reliqs.emonlight.commons.kafka.client;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.listener.BatchMessageListener;

import java.time.Instant;
import java.util.List;

public class KafkaMsgListener implements BatchMessageListener<Long, Double> {
    private static final Logger log = LoggerFactory.getLogger(KafkaMsgListener.class);

    @Override
    public void onMessage(List<ConsumerRecord<Long, Double>> data) {
        log.info("received messages {}", data.size());
        for (ConsumerRecord<Long, Double> c : data) {
            log.info("{}: message[{}]= '{}' {}", c.topic(), c.offset(),
                    Instant.ofEpochMilli(c.key()) /* c.key() */, c.value());
        }
    }

}
