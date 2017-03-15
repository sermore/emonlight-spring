package net.reliqs.emonlight.xbeegw.monitoring;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import net.reliqs.emonlight.xbeegw.config.Probe.Type;
import net.reliqs.emonlight.xbeegw.config.Settings;
import net.reliqs.emonlight.xbeegw.publish.Publisher;
import net.reliqs.emonlight.xbeegw.state.GlobalState;
import net.reliqs.emonlight.xbeegw.xbee.Processor;
import net.reliqs.emonlight.xbeegw.xbee.ProcessorTestConfig;
import net.reliqs.emonlight.xbeegw.xbee.TestSubscriber;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { Settings.class, Processor.class, GlobalState.class, Publisher.class,
        TriggerManager.class, TriggerDataAbsent.class, ProcessorTestConfig.class })
@EnableConfigurationProperties
@EnableScheduling
@EnableAsync
@ActiveProfiles("test-settings")
public class TriggerDataAbsentTest {

    @Autowired
    TriggerDataAbsent trigger;

    @Autowired
    Publisher publisher;

    @Test
    public void test() throws InterruptedException {
        TestSubscriber t = new TestSubscriber();
        publisher.addSubscriber(t);
        Thread.sleep(3100);
        assertThat(t.data, hasSize(4));
        assertThat(t.types, is(Arrays.asList(Type.DATA_MISSING_ALARM, Type.DATA_MISSING_ALARM, Type.DATA_MISSING_ALARM, Type.DATA_MISSING_ALARM)));
        assertThat(t.data.get(0).v, is(1.0));
        assertThat(t.data.get(1).v, is(1.0));
        t.clear();
        Thread.sleep(3100);
        assertThat(t.data, hasSize(4));
        assertThat(t.types, is(Arrays.asList(Type.DATA_MISSING_ALARM, Type.DATA_MISSING_ALARM, Type.DATA_MISSING_ALARM, Type.DATA_MISSING_ALARM)));
        assertThat(t.data.get(0).v, is(2.0));
        assertThat(t.data.get(1).v, is(2.0));
    }

}
