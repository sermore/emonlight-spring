package net.reliqs.emonlight.xbeegw.events;

import com.digi.xbee.api.utils.HexUtils;
import net.reliqs.emonlight.xbeegw.xbee.DataMessage;
import net.reliqs.emonlight.xbeegw.xbee.XbeeProcessor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jms.JmsAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

//import net.reliqs.emonlight.commons.kafka.utils.KafkaUtils;

@ActiveProfiles("test-queue")
@RunWith(SpringRunner.class)
@SpringBootTest
@ComponentScan(basePackages = {"net.reliqs.emonlight.xbeegw"})
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class, JmsAutoConfiguration.class})
@EnableConfigurationProperties
@EnableAsync
@EnableCaching
public class EventQueueTest {

    @Autowired
    EventQueue queue;

    @MockBean
    XbeeProcessor xbeeGateway;

    final String[][] q = {
            {"0013A20041468922", "44E79F6EDA5003E79F6ED9"},
            {"0013A20041479F96", "530765560CAE"},
            {"0013A20041468922", "44E79F8BB45003E79F8BB4"},
            {"0013A20041468938", "4A0A020000BFC1"},
            {"0013A20041468922", "44E79FA8835003E79FA883"},
            {"0013A20041468922", "44E79FC5555003E79FC555"}
    };

    private void populate() {

        for (int i = 0; i < q.length; i++) {
            DataMessage msg = new DataMessage(Instant.now().plus(i * 500L, ChronoUnit.MILLIS), q[i][0], HexUtils.hexStringToByteArray(q[i][1]));
            queue.offerDataMessage(msg, i * 500L);
        }
    }

    @Before
    @After
    public void clearQueue() {
        queue.clear();
    }

    @Test(timeout = 600L)
    public void testStopEvent() {
        queue.offerStopEvent(500L);
        assertThat(queue.run(1000L), is(0));
    }

    @Test
    public void testEventQueue() {
        populate();
        queue.run(5_000L);
    }

    @Test
    public void testBackup() {
        populate();
        assertThat(queue.size(), is(Integer.toUnsignedLong(q.length)));
        queue.close();
        queue.clear();
        queue.init();
        assertThat(queue.size(), is(Integer.toUnsignedLong(q.length)));
    }

}
