package net.reliqs.emonlight.streams.client;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CountDownLatch;

public class Receiver3 {

    private static final Logger log = LoggerFactory.getLogger(Receiver3.class);

    private CountDownLatch latch = new CountDownLatch(1);

    //	@KafkaListener(list = "mean_10_kafka-pino_a7LiZVht-FNo3i8bUf61")
    public void listen(List<ConsumerRecord<String, Double>> list) {
        log.info("received messages {}", list.size());
        for (ConsumerRecord<String, Double> c : list) {
//			log.info("received message[{}]= '{}' {}", c.offset(), Instant.ofEpochMilli(c.key()), c.value());
            log.info("received message[{}]= '{}' {}", c.offset(), c.key(), c.value());
        }
        latch.countDown();
    }

    public CountDownLatch getLatch() {
        return latch;
    }
}