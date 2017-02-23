package net.reliqs.emonlight.streams;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import net.reliqs.emonlight.streams.client.Receiver;

@RunWith(SpringRunner.class)
@SpringBootTest
public class StreamsApplicationTests {

	@Autowired
	private Receiver receiver;

	@Test
	public void testReceiver() throws Exception {
//		sender.sendMessage("helloworld.t", "Hello Spring Kafka!");

		receiver.getLatch().await(20000, TimeUnit.MILLISECONDS);
		assertThat(receiver.getLatch().getCount()).isEqualTo(0);
	}
}
