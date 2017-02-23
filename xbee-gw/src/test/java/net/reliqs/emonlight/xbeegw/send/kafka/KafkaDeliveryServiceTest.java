package net.reliqs.emonlight.xbeegw.send.kafka;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.util.ArrayDeque;
import java.util.Queue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import net.reliqs.emonlight.commons.kafka.utils.KafkaUtils;
import net.reliqs.emonlight.xbeegw.config.Server;
import net.reliqs.emonlight.xbeegw.config.ServerMap;
import net.reliqs.emonlight.xbeegw.config.Settings;
import net.reliqs.emonlight.xbeegw.send.kafka.KafkaConfig;
import net.reliqs.emonlight.xbeegw.send.kafka.KafkaDeliveryServiceTest.MyConfig;
import net.reliqs.emonlight.xbeegw.send.services.DeliveryService;
import net.reliqs.emonlight.xbeegw.send.services.DeliveryServiceFactory;
import net.reliqs.emonlight.xbeegw.xbee.Data;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = MyConfig.class)
@ActiveProfiles("test-router")
public class KafkaDeliveryServiceTest {

	@Profile("test-router")
	@SpringBootApplication(scanBasePackageClasses = { KafkaConfig.class, DeliveryServiceFactory.class, Settings.class })
	@EnableAsync
	static class MyConfig {

		@Bean
		KafkaUtils utils() {
			return new KafkaUtils();
		}
	}

	@Autowired
	@Qualifier("kafkaDeliveryService")
	DeliveryService kds;

	@Autowired
	Settings settings;

	@Autowired
	KafkaUtils utils;

	// @Bean
	// KafkaUtils getUtils() {
	// return new KafkaUtils();
	// }

	@Test
	public void testSend() throws InterruptedException {

		utils.initTopics(settings.getKafkaTopics());
		Server s = settings.getServers().stream().filter(ss -> ss.isKafkaEnabled()).findFirst().get();
		assertNotNull(s);

		Queue<Data> q = new ArrayDeque<>();
		long t = 0;
		Data in = new Data(t, 0.0);
		q.add(in);

		t += 1000;
		in = new Data(t, 100.0);
		q.add(in);

		t += 1000;
		in = new Data(t, 140.0);
		q.add(in);

		kds.addInit(s.getName());
		ServerMap sm = s.getMaps().get(0);
		kds.add(sm.getNodeId(), sm.getApiKey(), q.iterator());
		kds.addComplete();

		assertThat(kds.isReady(), is(true));
		kds.post();
		assertThat(kds.isEmpty(), is(false));
		Thread.sleep(1000);
		assertThat(kds. isEmpty(), is(true));

	}

}
