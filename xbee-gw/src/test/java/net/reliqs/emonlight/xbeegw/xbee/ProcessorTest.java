package net.reliqs.emonlight.xbeegw.xbee;

import com.digi.xbee.api.utils.HexUtils;
import net.reliqs.emonlight.xbeegw.config.Node;
import net.reliqs.emonlight.xbeegw.config.Probe;
import net.reliqs.emonlight.xbeegw.config.Probe.Type;
import net.reliqs.emonlight.xbeegw.config.Settings;
import net.reliqs.emonlight.xbeegw.monitoring.TriggerDataAbsent;
import net.reliqs.emonlight.xbeegw.monitoring.TriggerManager;
import net.reliqs.emonlight.xbeegw.publish.Data;
import net.reliqs.emonlight.xbeegw.publish.Publisher;
import net.reliqs.emonlight.xbeegw.state.GlobalState;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

/**
 * Created by sergio on 12/03/17.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {Settings.class, Processor.class, GlobalState.class, Publisher.class, TriggerManager.class,
        ProcessorTestConfig.class, TriggerDataAbsent.class})
@EnableConfigurationProperties
@ActiveProfiles("test-router")
public class ProcessorTest {

    @Autowired
    Publisher publisher;
    @Autowired
    GlobalState globalState;
    @Value("${processor.maxProcessTime:1000}")
    private long maxProcessTime;
    @Autowired
    private Processor processor;
    @Autowired
    private Settings settings;

    @Test
    public void testMaxProcessTime() throws Exception {

        Thread t = new Thread() {
            @Override
            public void run() {
                for (int i = 0; i < 10; i++) {
                    processor.queue(new DataMessage(Instant.now(), "", null));
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
        DataMessage msg = new DataMessage(Instant.now(), "0013A20041468937",
                HexUtils.hexStringToByteArray("4A0A0378007FFA"));
        Probe ph = settings.getProbes()
                .filter(p -> p.getNode().getAddress().equals("0013A20041468937") && p.getType() == Type.DHT22_H)
                .findFirst().get();
        Probe pt = settings.getProbes()
                .filter(p -> p.getNode().getAddress().equals("0013A20041468937") && p.getType() == Type.DHT22_T)
                .findFirst().get();
        processor.queue(msg);
        processor.process();
        assertThat(testSubscriber.data, is(Arrays.asList(new Data(msg.getTime().toEpochMilli(), 88.8),
                new Data(msg.getTime().toEpochMilli(), 12.7))));
        assertThat(testSubscriber.types, is(Arrays.asList(Type.DHT22_H, Type.DHT22_T)));
        assertThat(testSubscriber.probes, is(Arrays.asList(ph, pt)));
    }

    @Test
    public void testVccMessageProcessing() throws InterruptedException {
        TestSubscriber testSubscriber = new TestSubscriber();
        publisher.addSubscriber(testSubscriber);
        DataMessage msg = new DataMessage(Instant.now(), "0013A20041468937", HexUtils.hexStringToByteArray("5605BB"));
        Probe pv = settings.getProbes()
                .filter(p -> p.getNode().getAddress().equals("0013A20041468937") && p.getType() == Type.VCC).findFirst()
                .get();
        processor.queue(msg);
        processor.process();
        assertThat(testSubscriber.data, is(Arrays.asList(new Data(msg.getTime().toEpochMilli(), 4.043116483516483))));
        assertThat(testSubscriber.types, is(Arrays.asList(Type.VCC)));
        assertThat(testSubscriber.probes, is(Arrays.asList(pv)));
    }

    /**
     * Data received from 0013A20041468922 >> 44 0A 0A 71 38 50 03 0A 0A 5A 9D.
     * Data T=168456504, DT=55461, D=5031, @2017-03-14T10:05:41.584Z
     * skipNext=true N [MAIN, 0013A20041468922]: Pulse(3) Pow=0.0, T=168450717,
     * DT=5787 @2017-03-14T10:05:35.797Z, skipped=true
     * <p>
     * Data received from 0013A20041468922 >> 44 0A 0A E6 6E 48 0A 00 DC 00 C9
     * A5 0A 0A E6 6D. Data T=168486510, DT=35793,
     * D=23, @2017-03-14T10:06:11.567Z skipNext=false DHT22 P=10, T=20.1,
     * H=22.0 @2017-03-14T10:06:11.566Z
     * <p>
     * Data received from 0013A20041468922 >> 44 0A 0B 0D 78 57 0A 0A E6 7B 0C
     * DF. Data T=168496504, DT=45787, D=24, @2017-03-14T10:06:21.560Z
     * skipNext=false Vcc = 3.295 @2017-03-14T10:06:11.579Z
     * <p>
     * Data received from 0013A20041468922 >> 44 0A 0B 34 88 50 03 0A 0B 1D 2F.
     * Data T=168506504, DT=55787, D=40, @2017-03-14T10:06:31.544Z
     * skipNext=false Pulse(3) Pow=72.27464364585424, T=168500527,
     * DT=49810 @2017-03-14T10:06:25.605Z, skipped=false
     *
     * @throws InterruptedException
     */
    @Test
    public void testMultiMessageProcessing() throws InterruptedException {
        TestSubscriber testSubscriber = new TestSubscriber();
        publisher.addSubscriber(testSubscriber);
        DataMessage msg0 = new DataMessage(Instant.parse("2017-03-14T10:05:41.584Z"), "0013A20041468937",
                HexUtils.hexStringToByteArray("440A0A713850030A0A5A9D"));
        DataMessage msg1 = new DataMessage(Instant.parse("2017-03-14T10:06:11.567Z"), "0013A20041468937",
                HexUtils.hexStringToByteArray("440A0AE66E480A00DC00C9A50A0AE66D"));
        DataMessage msg2 = new DataMessage(Instant.parse("2017-03-14T10:06:21.560Z"), "0013A20041468937",
                HexUtils.hexStringToByteArray("440A0B0D78570A0AE67B0CDF"));
        DataMessage msg3 = new DataMessage(Instant.parse("2017-03-14T10:06:31.544Z"), "0013A20041468937",
                HexUtils.hexStringToByteArray("440A0B348850030A0B1D2F"));
        Node n = settings.getNodes().stream().filter(nn -> nn.getAddress().equals("0013A20041468937")).findFirst()
                .get();
        NodeState ns = globalState.getNodeState(n.getAddress());
        ns.lastTimeMSec = 168456504 - 55461;
        ns.lastTime = msg0.getTime().minus(55461 - 5031, ChronoUnit.MILLIS);
        processor.queue(msg0);
        processor.queue(msg1);
        processor.queue(msg2);
        processor.queue(msg3);
        processor.process();
        assertThat(testSubscriber.data, hasSize(4));
        assertThat(testSubscriber.data,
                is(Arrays.asList(new Data(msg1.getTime().toEpochMilli() - 1, 22.0),
                        new Data(msg1.getTime().toEpochMilli() - 1, 20.1),
                        new Data(Instant.parse("2017-03-14T10:06:11.579Z").toEpochMilli(), 9.081164835164834), // calculation
                        // wrong
                        // due
                        // to
                        // different
                        // node
                        // setup
                        new Data(Instant.parse("2017-03-14T10:06:25.605Z").toEpochMilli(), 72.27464364585424))));
        assertThat(testSubscriber.types, is(Arrays.asList(Type.DHT22_H, Type.DHT22_T, Type.VCC, Type.PULSE)));
        assertThat(testSubscriber.probes, is(Arrays.asList(n.getProbe(Type.DHT22_H), n.getProbe(Type.DHT22_T),
                n.getProbe(Type.VCC), n.getProbe(Type.PULSE))));
    }

    @Test
    public void testIncompleteMessage() throws InterruptedException {
        TestSubscriber testSubscriber = new TestSubscriber();
        publisher.addSubscriber(testSubscriber);
        DataMessage msg = new DataMessage(Instant.now(), "0013A20041468937",
                HexUtils.hexStringToByteArray("4A0A0378007F"));
        processor.queue(msg);
        processor.process();
        assertThat(testSubscriber.data.isEmpty(), is(true));
    }

}