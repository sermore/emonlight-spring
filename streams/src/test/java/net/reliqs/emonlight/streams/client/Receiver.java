package net.reliqs.emonlight.streams.client;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class Receiver {

    private static final Logger log = LoggerFactory.getLogger(Receiver.class);

    private CountDownLatch latch = new CountDownLatch(1);

    @KafkaListener(topics = {"var_10_kafka-pino_a7LiZVht-FNo3i8bUf61", "mean_10_kafka-pino_a7LiZVht-FNo3i8bUf61",
            "var_60_kafka-pino_a7LiZVht-FNo3i8bUf61", "mean_60_kafka-pino_a7LiZVht-FNo3i8bUf61",
            "var_1h_kafka-pino_a7LiZVht-FNo3i8bUf61", "mean_1h_kafka-pino_a7LiZVht-FNo3i8bUf61",
            "var_10u_kafka-pino_a7LiZVht-FNo3i8bUf61", "mean_10u_kafka-pino_a7LiZVht-FNo3i8bUf61"})
    // @KafkaListener(list = { "var_10_kafka-pino_a7LiZVht-FNo3i8bUf61",
    // "mean_10_kafka-pino_a7LiZVht-FNo3i8bUf61",
    // "sum_10_kafka-pino_a7LiZVht-FNo3i8bUf61",
    // "count_10_kafka-pino_a7LiZVht-FNo3i8bUf61" })
    // @KafkaListener(list = { "var_10_kafka-pino_a7LiZVht-FNo3i8bUf61" })
    public void listen(List<ConsumerRecord<Long, Double>> list) {
        log.info("received messages {}", list.size());
        for (ConsumerRecord<Long, Double> c : list) {
            log.info("{}: message[{}]= '{}' {}", c.topic(), c.offset(),
                    Instant.ofEpochMilli(c.key()) /* c.key() */, c.value());
        }
        latch.countDown();
    }

    public CountDownLatch getLatch() {
        return latch;
    }
}