package net.reliqs.emonlight.xbeegw.xbee;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class PulseProcessorTest {

    @Test
    public void testCalcPower() {
        PulseProcessor pp = new PulseProcessor(null, null);
        assertThat(pp.calcPower(1000, 1000), is(3600.0));
    }

}
