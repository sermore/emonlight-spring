package net.reliqs.emonlight.xbeegw.monitoring;

import net.reliqs.emonlight.commons.config.Node;
import net.reliqs.emonlight.xbeegw.events.TriggerExpiredEvent;
import org.junit.Test;

import java.util.concurrent.DelayQueue;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

public class TriggerExpiredEventTest {

    @Test
    public void test() throws InterruptedException {
        DelayQueue<TriggerExpiredEvent> q = new DelayQueue<>();
        Node n = new Node();
        n.setSampleTime(200);
        TriggerExpiredEvent dp = new TriggerExpiredEvent(null, n);
        q.add(dp);
        n = new Node();
        n.setSampleTime(300);
        TriggerExpiredEvent dp1 = new TriggerExpiredEvent(null, n);
        q.add(dp1);

        assertThat(q.poll(), is(nullValue()));
        Thread.sleep(1100);
        assertThat(q.poll(), is(dp));
        Thread.sleep(500);
        assertThat(q.poll(), is(dp1));
    }

}
