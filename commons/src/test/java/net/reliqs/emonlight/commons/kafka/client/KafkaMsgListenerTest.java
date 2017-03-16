package net.reliqs.emonlight.commons.kafka.client;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {KafkaConfig.class})
public class KafkaMsgListenerTest {

    @Autowired
    KafkaListenerContainerBuilder builder;

    @Test
    public void test() throws InterruptedException {
        KafkaMessageListenerContainer<Long, Double> c1 = builder
                .getContainer("mean_10_kafka-pino_a7LiZVht-FNo3i8bUf61", 0, -50, false);
        KafkaMessageListenerContainer<Long, Double> c2 = builder
                .getContainer("mean_10_kafka-pino_a7LiZVht-FNo3i8bUf61", 0, -50, false);
        assertNotSame(c1, c2);
        assertThat(c1, is(notNullValue()));
        KafkaMsgListener m = new KafkaMsgListener();
        c1.setupMessageListener(m);
        c1.start();
        Thread.sleep(20000);
        c1.stop();
    }

}
