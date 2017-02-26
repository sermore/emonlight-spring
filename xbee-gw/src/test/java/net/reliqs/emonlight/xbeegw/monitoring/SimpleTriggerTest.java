package net.reliqs.emonlight.xbeegw.monitoring;

import net.reliqs.emonlight.xbeegw.xbee.Data;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

/**
 * Created by sergio on 26/02/17.
 */
public class SimpleTriggerTest {

    @Test
    public void test() {
        SimpleTrigger s = new SimpleTrigger(null, 1000);
        assertThat(s.process(new Data(10L, 100.4)), is(false));
        assertThat(s.process(new Data(10L, 1000.0)), is(true));
        assertThat(s.process(new Data(10L, 999.4)), is(false));
    }

}