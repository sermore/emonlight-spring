package net.reliqs.emonlight.xbeegw.monitoring;

import net.reliqs.emonlight.xbeegw.config.Probe;
import org.junit.Test;

import java.util.concurrent.DelayQueue;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

public class DelayProbeTest {

    @Test
    public void test() throws InterruptedException {
        DelayQueue<DelayProbe> q = new DelayQueue<>();
        Probe p = new Probe();
        p.setSampleTime(200);
        DelayProbe dp = new DelayProbe(p);
        q.add(dp);
        p = new Probe();
        p.setSampleTime(300);
        DelayProbe dp1 = new DelayProbe(p);
        q.add(dp1);

        assertThat(q.poll(), is(nullValue()));
        Thread.sleep(1100);
        assertThat(q.poll(), is(dp));
        Thread.sleep(500);
        assertThat(q.poll(), is(dp1));
    }

}
