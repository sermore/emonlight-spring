package net.reliqs.emonlight.xbeegw.xbee;

import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.digi.xbee.api.exceptions.XBeeException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import net.reliqs.emonlight.xbeegw.MainApp;
import net.reliqs.emonlight.xbeegw.config.Settings;
import net.reliqs.emonlight.xbeegw.send.Dispatcher;
import net.reliqs.emonlight.xbeegw.xbee.Processor;
import net.reliqs.emonlight.xbeegw.xbee.EndDeviceTest.MyConfig;

@RunWith(SpringRunner.class)
@SpringBootTest(classes=MyConfig.class)
@ActiveProfiles("test-end-device")
@EnableAsync
public class EndDeviceTest {

	@Profile("test-end-device")
	@SpringBootApplication(scanBasePackages = "net.reliqs.emonlight.xbeegw", exclude = MainApp.class)
	static class MyConfig {

	}

	@Autowired
	Settings settings;

	@Autowired
	Processor p;

	@Autowired
	Dispatcher t;

	@Test
	public void testEndDevice()
			throws XBeeException, JsonParseException, JsonMappingException, IOException, InterruptedException {
		for (int i = 0; i < 40000; i++) {
			p.process();
			 if (i % 15 == 14)
			 t.process();
		}
	}
}
