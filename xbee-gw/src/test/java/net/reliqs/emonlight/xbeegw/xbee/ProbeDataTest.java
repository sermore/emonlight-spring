package net.reliqs.emonlight.xbeegw.xbee;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.PriorityQueue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import net.reliqs.emonlight.xbeegw.config.Probe;
import net.reliqs.emonlight.xbeegw.config.Probe.Type;
import net.reliqs.emonlight.xbeegw.config.Settings;
import net.reliqs.emonlight.xbeegw.xbee.ProbeDataTest.MyConfig;
import net.reliqs.emonlight.xbeegw.xbee.data.ProbeData;
import net.reliqs.emonlight.xbeegw.xbee.data.ProbeDataFactory;
import net.reliqs.emonlight.xbeegw.xbee.state.GlobalState;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = MyConfig.class)
@ActiveProfiles("test-router")
public class ProbeDataTest {

	@Profile("test-router")
	@SpringBootApplication(scanBasePackageClasses = { Settings.class, GlobalState.class, ProbeDataFactory.class })
	static class MyConfig {

	}

	static class MessageProcessorTest extends MessageProcessor {
//		private static final Logger log = LoggerFactory.getLogger(MessageProcessorTest.class);

		Type trigger;
		boolean enabled;
		int cnt = 0;

		MessageProcessorTest(Processor processor) {
			super(processor);
		}

		@Override
		void process(DataMessage m, NodeState ns, byte selector, ByteBuffer in) {
		}

		@Override
		public void trigger(NodeState ns, Probe p, Type t, boolean enable) {
			trigger = t;
			enabled = enable;
			cnt++;
//			log.warn("{}: {} {} ({})", p.getName(), t, enable, cnt);
		}

	}

	@Autowired
	Settings settings;

	@Autowired
	GlobalState globalState;

	// @Autowired
	// Dispatcher dispatcher;

	@Test
	public void testPriorityQeue() {
		PriorityQueue<Type> q = new PriorityQueue<Type>();
		q.add(Type.M_SOFT_THRESHOLD_1);
		q.add(Type.M_SOFT_THRESHOLD_3);
		q.add(Type.M_SOFT_THRESHOLD_2);
		assertThat(q.peek(), is(Type.M_SOFT_THRESHOLD_3));
		q.remove(Type.M_SOFT_THRESHOLD_1);
		assertThat(q.peek(), is(Type.M_SOFT_THRESHOLD_3));
		q.add(Type.M_SOFT_THRESHOLD_1);
		q.remove(Type.M_SOFT_THRESHOLD_3);
		assertThat(q.peek(), is(Type.M_SOFT_THRESHOLD_2));
	}

	@Test
	public void testAddAndClear() throws JsonParseException, JsonMappingException, IOException {
		// Settings settings = new
		// Factory().load("src/test/resources/test1.json");
		Probe p = settings.getNodes().get(0).getProbes().get(0);
		ProbeData pd = globalState.getProbeData(p);
		MessageProcessorTest mp = new MessageProcessorTest(null);
		
		assertSame(p, pd.getProbe());
		assertThat(pd, is(not(nullValue())));
		assertThat(p.isConnectedToOutput(), is(true));
		
		pd.clear();
		long t = 0;
		Data in = new Data(t, 0.0);
		pd.add(mp, in);

		t += 1000;
		in = new Data(t, 200.0);
		pd.add(mp, in);
		assertThat(mp.cnt, is(0));

		t += 1000;
		in = new Data(t, 200.0);
		pd.add(mp, in);
		assertThat(mp.cnt, is(0));

		// trigger after at least 3 seconds
		t += 1000 * 4;
		in = new Data(t, 3300.0);
		pd.add(mp, in);
		assertThat(mp.trigger, is(Type.M_SOFT_THRESHOLD_1));
		assertThat(mp.enabled, is(true));
		assertThat(mp.cnt, is(1));
		t += 1000 * 1;
		in = new Data(t, 3200.0);
		pd.add(mp, in);
		assertThat(mp.trigger, is(Type.M_SOFT_THRESHOLD_1));
		assertThat(mp.enabled, is(false));
		t += 1000 * 1;
		in = new Data(t, 3300.0);
		pd.add(mp, in);
		assertThat(mp.trigger, is(Type.M_SOFT_THRESHOLD_1));
		assertThat(mp.enabled, is(true));

		// while (t < 1000* 60 * 60) {
		// t += 1000;
		// in = new Data(t, 3300.0);
		// pd.add(mp, in);
		// }
		// assertThat(mp.trigger, is(Type.M_SOFT_THRESHOLD_2));
		// assertThat(mp.enabled, is(true));

		t += 1000 * 60 * 59;
		in = new Data(t, 3300.0);
		pd.add(mp, in);
		assertThat(mp.trigger, is(Type.M_SOFT_THRESHOLD_1));
		assertThat(mp.enabled, is(true));

		t += 1000 * 60;
		in = new Data(t, 3300.0);
		pd.add(mp, in);
		assertThat(mp.trigger, is(Type.M_SOFT_THRESHOLD_2));
		assertThat(mp.enabled, is(true));

		t += 1000 * 60 * 59;
		in = new Data(t, 3300.0);
		pd.add(mp, in);
		assertThat(mp.trigger, is(Type.M_SOFT_THRESHOLD_2));
		assertThat(mp.enabled, is(true));

		t += 1000 * 60;
		in = new Data(t, 3300.0);
		pd.add(mp, in);
		assertThat(mp.trigger, is(Type.M_SOFT_THRESHOLD_3));
		assertThat(mp.enabled, is(true));

		while (mp.enabled) {
			t += 1000;
			in = new Data(t, 3200.0);
			pd.add(mp, in);
			assertThat(mp.trigger, is(Type.M_SOFT_THRESHOLD_3));
		}

		t += 1000;
		in = new Data(t, 3300.0);
		pd.add(mp, in);
		assertThat(mp.trigger, is(Type.M_SOFT_THRESHOLD_3));
		assertThat(mp.enabled, is(true));

		t += 3000;
		in = new Data(t, 4000.0);
		pd.add(mp, in);
		assertThat(mp.trigger, is(Type.M_HARD_THRESHOLD));
		assertThat(mp.enabled, is(true));

		t += 3000;
		in = new Data(t, 3200.0);
		pd.add(mp, in);
		assertThat(mp.trigger, is(Type.M_SOFT_THRESHOLD_3));
		assertThat(mp.enabled, is(true));

		t += 1000;
		in = new Data(t, 3200.0);
		pd.add(mp, in);
		assertThat(mp.trigger, is(Type.M_SOFT_THRESHOLD_3));
		assertThat(mp.enabled, is(false));

		assertThat(pd.queueLength(), is(16));
		pd.clear();
		assertThat(pd.queueLength(), is(0));
	}

}
