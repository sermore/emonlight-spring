package net.reliqs.emonlight.xbeegw.xbee;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import org.junit.Test;

import net.reliqs.emonlight.xbeegw.config.Probe.Type;
import net.reliqs.emonlight.xbeegw.xbee.PulseProcessor;

public class PulseProcessorTest {

	@Test
	public void testCalcPower() {
		PulseProcessor pp = new PulseProcessor(null, null);
		assertThat(pp.calcPower(1000, 1000), is(3600.0));
	}

}
