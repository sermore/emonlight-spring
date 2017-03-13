package net.reliqs.emonlight.xbeegw.xbee;

import net.reliqs.emonlight.xbeegw.config.Settings;
import net.reliqs.emonlight.xbeegw.config.Probe;
import net.reliqs.emonlight.xbeegw.config.Probe.Type;
import net.reliqs.emonlight.xbeegw.monitoring.TriggerManager;
import net.reliqs.emonlight.xbeegw.publish.Data;
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

import com.digi.xbee.api.utils.HexUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;

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

    @Value("${processor.maxProcessTime:1000}")
    private long maxProcessTime;
    @Autowired
    private Processor processor;
    @Autowired
    Publisher publisher;
    @Autowired
    private Settings settings;
    
    
    @Test
    public void testMaxProcessTime() throws Exception {

        Thread t = new Thread() {
            @Override
            public void run() {
                for (int i = 0; i < 10; i++) {
                    processor.queue(new DataMessage("", null));
                    try {
                        Thread.sleep(maxProcessTime / 5);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        };
        t.start();
        Thread.sleep(maxProcessTime / 2);
        Instant start = Instant.now();
        processor.process();
        Instant end = Instant.now();
        assertThat(end.isAfter(start.plus(maxProcessTime, ChronoUnit.MILLIS)), is(true));
        assertThat(end.isBefore(start.plus((long) (maxProcessTime * 1.2), ChronoUnit.MILLIS)), is(true));
    }

    @Test
    public void testDHT22MessageProcessing() throws InterruptedException {
        TestSubscriber testSubscriber = new TestSubscriber();
        publisher.addSubscriber(testSubscriber);        
        DataMessage msg = new DataMessage("0013A20041468937", HexUtils.hexStringToByteArray("4A0A0378007FFA"));
        Probe ph = settings.getProbes().filter(p -> p.getNode().getAddress().equals("0013A20041468937") && p.getType() == Type.DHT22_H).findFirst().get();
        Probe pt = settings.getProbes().filter(p -> p.getNode().getAddress().equals("0013A20041468937") && p.getType() == Type.DHT22_T).findFirst().get();
        processor.queue(msg);
        processor.process();
        assertThat(testSubscriber.data, is(Arrays.asList(new Data(msg.getTime().toEpochMilli(), 88.8), new Data(msg.getTime().toEpochMilli(), 12.7))));
        assertThat(testSubscriber.types, is(Arrays.asList(Type.DHT22_H, Type.DHT22_T)));
        assertThat(testSubscriber.probes, is(Arrays.asList(ph, pt)));
    }

    @Test
    public void testVccMessageProcessing() throws InterruptedException {
        TestSubscriber testSubscriber = new TestSubscriber();
        publisher.addSubscriber(testSubscriber);        
        DataMessage msg = new DataMessage("0013A20041468937", HexUtils.hexStringToByteArray("5605BB"));
        Probe pv = settings.getProbes().filter(p -> p.getNode().getAddress().equals("0013A20041468937") && p.getType() == Type.VCC).findFirst().get();
        processor.queue(msg);
        processor.process();
        assertThat(testSubscriber.data, is(Arrays.asList(new Data(msg.getTime().toEpochMilli(), 4.043116483516483))));
        assertThat(testSubscriber.types, is(Arrays.asList(Type.VCC)));
        assertThat(testSubscriber.probes, is(Arrays.asList(pv)));
    }
    
    @Test
    public void testIncompleteMessage() throws InterruptedException {
        TestSubscriber testSubscriber = new TestSubscriber();
        publisher.addSubscriber(testSubscriber);        
        DataMessage msg = new DataMessage("0013A20041468937", HexUtils.hexStringToByteArray("4A0A0378007F"));
        processor.queue(msg);
        processor.process();
        assertThat(testSubscriber.data.isEmpty(), is(true));
    }

}