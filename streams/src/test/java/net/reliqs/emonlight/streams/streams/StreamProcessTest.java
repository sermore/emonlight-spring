package net.reliqs.emonlight.streams.streams;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.support.TopicPartitionInitialOffset;
import org.springframework.test.context.junit4.SpringRunner;

import net.reliqs.emonlight.commons.kafka.client.KafkaConfig;
import net.reliqs.emonlight.commons.kafka.client.KafkaListenerContainerBuilder;
import net.reliqs.emonlight.commons.kafka.utils.KafkaUtils;
import net.reliqs.emonlight.streams.config.Processor;
import net.reliqs.emonlight.streams.config.StreamsAppConfig;
import net.reliqs.emonlight.streams.streams.StreamProcessTest.MyConfig;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { MyConfig.class, StreamProcess.class, KafkaConfig.class, KafkaUtils.class })
public class StreamProcessTest {
	private static final Logger log = LoggerFactory.getLogger(StreamProcessTest.class);

	@Configuration
	@EnableConfigurationProperties(StreamsAppConfig.class)
	static class MyConfig {

		@Autowired
		StreamsAppConfig config;

		@Bean
		StreamProcess process1() {
			Processor p1 = config.getProcessors().remove(1);
			Processor p2 = config.getProcessors().remove(1);
			StreamProcess sp = new StreamProcess(config);
			config.getProcessors().add(p1);
			config.getProcessors().add(p2);
			return sp;
		}

		@Bean
		StreamProcess process2() {
			Processor p0 = config.getProcessors().remove(0);
			Processor p2 = config.getProcessors().remove(1);
			StreamProcess sp = new StreamProcess(config);
			config.getProcessors().add(0, p0);
			config.getProcessors().add(p2);
			return sp;
		}

		@Bean
		StreamProcess process3() {
			Processor p0 = config.getProcessors().remove(0);
			Processor p1 = config.getProcessors().remove(0);
			StreamProcess sp = new StreamProcess(config);
			config.getProcessors().add(0, p0);
			config.getProcessors().add(1, p1);
			return sp;
		}

	}

	static class Trace {
		long lastMsgTime;
		long interval;
		long maxDiff;
		boolean fail;
		long offset;
		long cnt;
		long maxError;
		ConsumerRecord<Long, Double> data;

		public Trace(long interval) {
			this.interval = interval;
			this.maxError = interval / 10;
		}
	}

	Map<String, Trace> traces = new HashMap<>();

	class TestMsgListener implements MessageListener<Long, Double> {

		@Override
		public void onMessage(ConsumerRecord<Long, Double> data) {
			Trace t = traces.get(data.topic());
			t.cnt++;
			long d = Math.abs(data.key() - t.lastMsgTime - t.interval);
			// if (!t.fail) {
			log.info("{}: T={} ({}), d={}, o={}\n", data.topic(), Instant.ofEpochMilli(data.key()),
					Instant.ofEpochMilli(data.timestamp()), d, data.offset());
			// }
			if (t.cnt > 3 && t.lastMsgTime > 0 && d > t.maxDiff) {
				t.maxDiff = d;
				t.data = data;
				t.offset = data.offset();
				if (t.maxDiff > t.maxError) {
					t.fail = true;
				}
			}
			t.lastMsgTime = data.key();
		}

	}

	// @Bean
	// StreamProcess process() { return new StreamProcess(config); }

	@Autowired
	StreamsAppConfig config;

	@Autowired
	StreamProcess process1;

	@Autowired
	StreamProcess process2;

	@Autowired
	StreamProcess process3;

	@Autowired
	KafkaListenerContainerBuilder builder;

	@Test
	public void testMean() throws IOException, InterruptedException {
		// assertThat(config).isNotNull();
		testTopic("kafka-pino_a7LiZVht-FNo3i8bUf61", 0, process1);
	}

	@Test
	public void testRunningMean() throws IOException, InterruptedException {
		testTopic("kafka-pino_a7LiZVht-FNo3i8bUf61", 1, process2);
	}

	@Test
	public void testRunningMean1h() throws IOException, InterruptedException {
		testTopic("kafka-pino_a7LiZVht-FNo3i8bUf61", 2, process3);
	}

	private void testTopic(String topic, int procIndex, StreamProcess process)
			throws IOException, InterruptedException {
		String topicLabel = config.getTopics().get(topic);
		Processor p = config.getProcessors().get(procIndex);
		String mean = "mean_" + p.getName() + "_" + topic;
		String var = "var_" + p.getName() + "_" + topic;
		traces.put(mean, new Trace(p.getInterval()));
		traces.put(var, new Trace(p.getInterval()));
		TopicPartitionInitialOffset[] topics = new TopicPartitionInitialOffset[] {
				new TopicPartitionInitialOffset("mean_" + p.getName() + "_" + topic, 0, -5000L, false),
				new TopicPartitionInitialOffset("var_" + p.getName() + "_" + topic, 0, -5000L, false) };
		KafkaMessageListenerContainer<Long, Double> c = builder.getContainer(topics);
		assertThat(c, is(notNullValue()));
		TestMsgListener m = new TestMsgListener();
		c.setupMessageListener(m);

		process.start();
		// Thread.sleep(3_000);
		c.start();
		Thread.sleep(15_000);
		process.close();
		c.stop();
		Trace t1 = traces.get(mean);
		log.info("DATA {}[{}] t={}, ts={}, o={}, d={}", topicLabel, t1.data.topic(),
				Instant.ofEpochMilli(t1.data.key()), Instant.ofEpochMilli(t1.data.timestamp()), t1.offset, t1.maxDiff);
		Trace t2 = traces.get(var);
		log.info("DATA {}[{}]: t={}, ts={}, o={}, d={}", topicLabel, t2.data.topic(),
				Instant.ofEpochMilli(t2.data.key()), Instant.ofEpochMilli(t2.data.timestamp()), t2.offset, t2.maxDiff);
		assertThat(t1.fail).isFalse();
		assertThat(t2.fail).isFalse();
	}

}
