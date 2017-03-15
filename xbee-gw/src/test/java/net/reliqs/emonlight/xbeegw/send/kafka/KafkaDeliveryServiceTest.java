package net.reliqs.emonlight.xbeegw.send.kafka;

import net.reliqs.emonlight.commons.kafka.utils.KafkaUtils;
import net.reliqs.emonlight.xbeegw.config.Probe;
import net.reliqs.emonlight.xbeegw.config.Settings;
import net.reliqs.emonlight.xbeegw.send.services.DeliveryService;
import net.reliqs.emonlight.xbeegw.xbee.Data;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {KafkaConfig.class, Settings.class, KafkaUtils.class})
@EnableConfigurationProperties
@EnableAsync
@ActiveProfiles("test-router")
public class KafkaDeliveryServiceTest {

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
        kds.receive(p, in);

        t += 1000;
        in = new Data(t, 100.0);
        kds.receive(p, in);

        t += 1000;
        in = new Data(t, 140.0);
        kds.receive(p, in);

        assertThat(kds.isReady(), is(true));
        kds.post();
        assertThat(kds.isEmpty(), is(false));
        Thread.sleep(1000);
        assertThat(kds.isEmpty(), is(true));

    }

}
