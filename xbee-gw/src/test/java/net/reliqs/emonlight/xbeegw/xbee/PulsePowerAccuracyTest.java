package net.reliqs.emonlight.xbeegw.xbee;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.digi.xbee.api.exceptions.XBeeException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import net.reliqs.emonlight.xbeegw.TestRouterConfig;
import net.reliqs.emonlight.xbeegw.config.Probe;
import net.reliqs.emonlight.xbeegw.config.Settings;
import net.reliqs.emonlight.xbeegw.send.Dispatcher;
import net.reliqs.emonlight.xbeegw.xbee.Data;
import net.reliqs.emonlight.xbeegw.xbee.Processor;
import net.reliqs.emonlight.xbeegw.xbee.data.ProbeData;
import net.reliqs.emonlight.xbeegw.xbee.state.GlobalState;

@RunWith(SpringRunner.class)
@SpringBootTest(classes=TestRouterConfig.class)
@ActiveProfiles("test-router")
public class PulsePowerAccuracyTest {

	@Autowired
	Settings settings;
	
	@Autowired
	Processor p;
	
	@Autowired
	GlobalState globalState;
	
	@Autowired
	Dispatcher t;
	
	static void runCmd(String cmd) throws IOException, InterruptedException {
		Runtime rt = Runtime.getRuntime();
		Process p = rt.exec(cmd);
		assertThat(p.waitFor(), is(0));
		// String s = new Scanner(p.getInputStream(), "UTF-8").useDelimiter("\\A").next();
		// System.out.println(s);
	}

	@BeforeClass
	public static void setupPower() throws IOException, InterruptedException {
		runCmd("ssh pi@pino gpio pwm-ms && gpio pwmc 4095 && gpio -g pwm 18 2");
	}

	void setPower(double power) throws IOException, InterruptedException {
		long range = Math.round(16879120.879 / power);
		System.out.println("ssh pi@pino gpio pwmr " + range);
		runCmd("ssh pi@pino gpio pwmr " + range);
	}

	double calcTimeFromPower(double power) {
		return 3600.0 / power;
	}
	
	@After
	@Before
	public void afterSetup() throws IOException, InterruptedException {
		setPower(100);
	}

	@Test
	public void testPulsePowerAccuracy()
			throws JsonParseException, JsonMappingException, IOException, XBeeException, InterruptedException {
		Probe pr = settings.getNodes().get(0).getProbes().get(0);
		ProbeData pd = globalState.getProbeData(pr);
		double power0 = 0.0;
		for (double power = 5100.0; power > 0.0; power -= 500.0) {
			testPower(p, pd, power0, power);
			power0 = power;
		}

	}

	@Test
	public void testPulseHighPower() throws JsonParseException, JsonMappingException, IOException, XBeeException, InterruptedException {
//		Settings settings = new Factory().load("src/test/resources/test-router.json");
//		Processor p = new Processor(settings);
		Probe pr = settings.getNodes().get(0).getProbes().get(0);
		ProbeData pd = globalState.getProbeData(pr);
		setPower(15000);
		Instant t = Instant.now().plus(60, ChronoUnit.SECONDS);
		while (Instant.now().isBefore(t)) {
			p.process();
			Data d = pd.poll();
			if (d != null && d.v != 0) {
				assertThat(d.v, closeTo(15000.0, 100.0));
			}
		}
	}
	
	private void testPower(Processor p, ProbeData pd, double power0, double power)
			throws IOException, InterruptedException {
		double eps = power / 100;
		setPower(power);
		Data d;
		do {
			while (pd.queueLength() == 0) {
				p.process();
			}
			d = pd.poll();
		} while (Math.abs(d.v - power0) < eps);
		assertThat(d.v, closeTo(power, eps));
		while (pd.queueLength() > 0) {
			assertThat(pd.poll().v, closeTo(power, eps));			
		}
	}

}
