package net.reliqs.emonlight.xbeegw.monitoring;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.*;

import java.util.concurrent.DelayQueue;

import org.junit.Test;

import net.reliqs.emonlight.xbeegw.config.Probe;

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
