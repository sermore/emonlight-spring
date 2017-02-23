package net.reliqs.emonlight.xbeegw.xbee;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.nio.ByteBuffer;

import org.junit.Test;

import com.digi.xbee.api.utils.HexUtils;

import net.reliqs.emonlight.xbeegw.xbee.DHT22Data;

public class DHT22DataTest {

	@Test
	public void testData() {
		DHT22Data d = new DHT22Data(ByteBuffer.wrap(HexUtils.hexStringToByteArray("0A016500C72D")));
		assertThat(d.check(), is(true));
		assertThat(d.humidity(), is(35.7));
		assertThat(d.temperature(), is(19.9));

		d = new DHT22Data(ByteBuffer.wrap(HexUtils.hexStringToByteArray("0A01748002F7")));
		assertThat(d.check(), is(true));
		assertThat(d.humidity(), is(37.2));
		assertThat(d.temperature(), is(-0.2));
	}

}
