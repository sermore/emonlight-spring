package net.reliqs.emonlight.xbeegw.events;

import com.digi.xbee.api.utils.HexUtils;
import net.reliqs.emonlight.xbeegw.TestApp;
import net.reliqs.emonlight.xbeegw.xbee.DataMessage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

//import net.reliqs.emonlight.commons.kafka.utils.KafkaUtils;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {TestApp.class})
@ActiveProfiles({"integration", "test-queue"})
public class EventQueueTest {

    final String[][] q = {
            {"0013A20041468922", "44E79F6EDA5003E79F6ED9"},
            {"0013A20041479F96", "530765560CAE"},
            {"0013A20041468922", "44E79F8BB45003E79F8BB4"},
            {"0013A20041468938", "4A0A020000BFC1"},
            {"0013A20041468922", "44E79FA8835003E79FA883"},
            {"0013A20041468922", "44E79FC5555003E79FC555"}
    };

    @Autowired
    private EventQueue queue;
    @Autowired
    private EventProcessorFacade eventProcessorFacade;

    private void populate() {

        for (int i = 0; i < q.length; i++) {
            DataMessage msg = new DataMessage(Instant.now().plus(i * 500L, ChronoUnit.MILLIS), q[i][0], HexUtils.hexStringToByteArray(q[i][1]));
            eventProcessorFacade.queueMessage(msg, i * 500L);
        }
    }

    @Before
    @After
    public void clearQueue() {
        queue.clear();
    }

    @Test(timeout = 600L)
    public void testStopEvent() {
        eventProcessorFacade.queueStopEvent(500L);
        assertThat(queue.run(), is(0));
    }

    @Test
    public void testEventQueue() {
        populate();
        eventProcessorFacade.run(5_000L);
    }

    @Test
    public void testBackup() {
        eventProcessorFacade.setBackupEnabled(true);
        populate();
        assertThat(queue.size(), is(q.length));
        eventProcessorFacade.close();
        queue.clear();
        eventProcessorFacade.init();
        assertThat(queue.size(), is(q.length + 1));
    }

}
