package net.reliqs.emonlight.xbeegw.send.rest;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import net.reliqs.emonlight.xbeegw.send.rest.RestDeliveryServiceTest.MyConfig;
import net.reliqs.emonlight.xbeegw.xbee.Data;

@ActiveProfiles("test-router")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = MyConfig.class)
public class RestDeliveryServiceTest {


	@SpringBootApplication(scanBasePackageClasses={RestAsyncService.class})
	@EnableAsync
	static class MyConfig {

		@Autowired
		RestTemplateBuilder rb;

		@Bean
		@Scope("prototype")
		RestAsyncService restAsyncService() { return new RestAsyncService(rb); }
	}

	@Autowired
	private RestAsyncService ra;
	@Autowired
	private RestAsyncService ra1;

	@Test
	public void testPrototype() {
		assertNotSame(ra, ra1);
	}
	
	@Test
	public void testAdd() {
		RestDeliveryService rest1 = new RestDeliveryService(ra);
		rest1.setUrl("http://pino/emonlight-dev/input/read.json");

		ServerDataJSON sd = new ServerDataJSON();
		NodeDataJSON nd = new NodeDataJSON(1, "XXX");
		sd.getNodes().add(nd);
		Queue<Data> q = new ArrayDeque<>();
		long t = 0;
		Data in = new Data(t, 0.0);
		nd.addData(in);
		q.add(in);

		t += 1000;
		in = new Data(t, 100.0);
		nd.addData(in);
		q.add(in);

		t += 1000;
		in = new Data(t, 140.0);
		nd.addData(in);
		q.add(in);

		rest1.addInit("server");
		rest1.add(1, "XXX", q.iterator());
		rest1.addComplete();

		assertThat(rest1.pollQueue(), is(sd));

	}

	@Test
	public void testPost() throws InterruptedException, ExecutionException {
		// RestDeliveryService rest1 = new RestDeliveryService(ra,
		// "http://pino/emonlight-dev/input/read.json");
		//
		// RestDeliveryService rest2 = new RestDeliveryService(ra,
		// "http://acero/emonlight-dev/input/read.json");
		//
		// assertThat(rest1, is(not(rest2)));

		Data in = new Data(Instant.now().toEpochMilli(), 199);
		NodeDataJSON nd = new NodeDataJSON(15, "mvXvsisjD_V9ze9iVokf");
		nd.addData(in);
		ServerDataJSON sd = new ServerDataJSON();
		sd.getNodes().add(nd);

		Future<Boolean> res = ra.post("http://pino/emonlight-dev/input/read.json", sd);
		assertThat(res.get(), is(true));

		res = ra.post("http://pino/emonlight-dev/input/read.json", new ServerDataJSON());
		try {
			res.get();
			fail();
		} catch (Exception e) {
			assertThat(e, instanceOf(ExecutionException.class));
		}

		res = ra.post("http://acero/emonlight-dev/input/read.json", sd);
		try {
			res.get();
			fail();
		} catch (Exception e) {
			assertThat(e, instanceOf(ExecutionException.class));
		}
	}

}
