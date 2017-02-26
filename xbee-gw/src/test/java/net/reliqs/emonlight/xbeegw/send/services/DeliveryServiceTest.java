package net.reliqs.emonlight.xbeegw.send.services;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Queue;

import net.reliqs.emonlight.xbeegw.config.Probe;
import net.reliqs.emonlight.xbeegw.config.Settings;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import net.reliqs.emonlight.xbeegw.send.kafka.KafkaConfig;
import net.reliqs.emonlight.xbeegw.send.services.DeliveryServiceTest.MyConfig;
import net.reliqs.emonlight.xbeegw.xbee.Data;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = MyConfig.class)
@ActiveProfiles("test-router")
public class DeliveryServiceTest {

	@Profile("test-router")
	@SpringBootApplication(scanBasePackageClasses = { KafkaConfig.class, DeliveryServiceFactory.class })
	@EnableAsync
	static class MyConfig {

	}

	@Autowired
	Settings settings;

	@Autowired
	DeliveryServiceFactory dsf;
	
	@Autowired
	DeliveryService kafkaDeliveryService;

	@Test
	public void testRestService() throws InterruptedException {

	    Probe p = settings.getProbes().findFirst().get();

		DeliveryService send1 = dsf.getRestDeliveryServiceFromUrl("http://pino/emonlight-dev/input/read.json");
		DeliveryService send2 = dsf.getRestDeliveryServiceFromUrl("http://acero/emonlight-dev/input/read.json");
		DeliveryService send3 = kafkaDeliveryService;

		assertNotSame(send1, send2);

        Data d = new Data(Instant.now().toEpochMilli(), 199);

		assertThat(send1.isReady(), is(false));
		assertThat(send2.isReady(), is(false));
		assertThat(send3.isReady(), is(false));

		send1.receive(p, d);
		send2.receive(p, d);
		send3.receive(p, d);

		assertThat(send1.isReady(), is(true));
		assertThat(send2.isReady(), is(true));

		send1.post();
		send2.post();
		send3.post();

		assertThat(send1.isReady(), is(false));
		assertThat(send2.isReady(), is(false));

		Thread.sleep(2000);

		assertThat(send1.isReady(), is(true));
		assertThat(send2.isReady(), is(true));

	}

}
