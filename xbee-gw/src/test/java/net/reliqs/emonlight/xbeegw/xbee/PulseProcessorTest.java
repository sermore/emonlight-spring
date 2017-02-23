package net.reliqs.emonlight.xbeegw.xbee;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import org.junit.Test;

import net.reliqs.emonlight.xbeegw.config.Probe.Type;
import net.reliqs.emonlight.xbeegw.xbee.PulseProcessor;

public class PulseProcessorTest {

	@Test
	public void testCalcBuzzerLevel() {
		PulseProcessor pp = new PulseProcessor(null);
		assertThat(pp.calcBuzzerLevel(Type.M_SOFT_THRESHOLD_1, true), is(1));
		assertThat(pp.calcBuzzerLevel(Type.M_SOFT_THRESHOLD_2, true), is(2));
		assertThat(pp.calcBuzzerLevel(Type.M_HARD_THRESHOLD, true), is(4));
		assertThat(pp.calcBuzzerLevel(Type.M_SOFT_THRESHOLD_1, false), is(0));
	}

	@Test
	public void testCalcPower() {
		PulseProcessor pp = new PulseProcessor(null);
		assertThat(pp.calcPower(1000, 1000), is(3600.0));
	}

}
