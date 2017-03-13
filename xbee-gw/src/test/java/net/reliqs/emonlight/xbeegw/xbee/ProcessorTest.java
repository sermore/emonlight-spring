package net.reliqs.emonlight.xbeegw.xbee;

import net.reliqs.emonlight.xbeegw.config.Settings;
import net.reliqs.emonlight.xbeegw.monitoring.TriggerManager;
import net.reliqs.emonlight.xbeegw.publish.Publisher;
import net.reliqs.emonlight.xbeegw.state.GlobalState;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * Created by sergio on 12/03/17.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = { Settings.class, Processor.class, GlobalState.class, Publisher.class, TriggerManager.class })
@EnableConfigurationProperties
@ActiveProfiles("test-router")
public class ProcessorTest {

    @Configuration
    static class MyConfig {

        @Bean
        @Primary
        XbeeGateway xbeeGateway() {
            return Mockito.mock(XbeeGateway.class);
        }

    }

    @Autowired
    Processor processor;

    @Value("${processor.maxProcessTime:5000}")
    private long maxProcessTime;

    @Test
    public void testMaxProcessTime() throws Exception {

        Thread t = new Thread() {
            @Override
            public void run() {
                for (int i = 0; i < 10; i++) {
                    processor.queue(new DataMessage(null));
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        };
        t.start();
        Instant start = Instant.now();
        processor.process();
        Instant end = Instant.now();
        assertThat(end.isAfter(start.plus(maxProcessTime, ChronoUnit.MILLIS))
                && end.isBefore(start.plus(maxProcessTime + 1000, ChronoUnit.MILLIS)), is(true));
    }

}