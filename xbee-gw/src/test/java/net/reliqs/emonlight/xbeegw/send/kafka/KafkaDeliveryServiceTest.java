package net.reliqs.emonlight.xbeegw.send.kafka;

import net.reliqs.emonlight.commons.config.Probe;
import net.reliqs.emonlight.commons.config.Settings;
import net.reliqs.emonlight.xbeegw.TestApp;
import net.reliqs.emonlight.xbeegw.publish.Data;
import net.reliqs.emonlight.xbeegw.send.services.DeliveryService;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.rule.KafkaEmbedded;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestApp.class)
@ActiveProfiles("integration,kafka")
public class KafkaDeliveryServiceTest {

    @ClassRule
    public static KafkaEmbedded embeddedKafka = new KafkaEmbedded(1, true, "topic");

    @Autowired
    @Qualifier("kafkaDeliveryService")
    DeliveryService kds;

    @Autowired
    Settings settings;

    // @Bean
    // KafkaUtils getUtils() {
    // return new KafkaUtils();
    // }

    @Test
    public void testSend() throws InterruptedException {

        Probe p = settings.getProbes().findFirst().get();

        long t = 0;
        Data in = new Data(t, 0.0);
        kds.receive(p, p.getType(), in);

        t += 1000;
        in = new Data(t, 100.0);
        kds.receive(p, p.getType(), in);

        t += 1000;
        in = new Data(t, 140.0);
        kds.receive(p, p.getType(), in);

        assertThat(kds.isReady(), is(true));
        kds.post();
        assertThat(kds.isQueueEmpty(), is(false));
        Thread.sleep(1000);
        assertThat(kds.isQueueEmpty(), is(true));

    }

}
