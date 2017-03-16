package net.reliqs.emonlight.commons.kafka.utils;

import net.reliqs.emonlight.commons.kafka.utils.KafkaZkClientTest.MyConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = MyConfig.class)
@ActiveProfiles("test-router")
public class KafkaZkClientTest {

    @Autowired
    KafkaZkClient zk;

    @Test
    public void testCreateDeleteTopic() throws InterruptedException {
        String topicName = "testTopic";
        assertThat(zk.topicExists(topicName), is(false));
        zk.createTopic(topicName, 1, 1);
        assertThat(zk.topicExists(topicName), is(true));
        zk.deleteTopic(topicName);
        Thread.sleep(2000);
        assertThat(zk.topicExists(topicName), is(false));
    }

    @SpringBootApplication(scanBasePackages = "my.fake")
    @Profile("test-router")
    static class MyConfig {

        @Bean
        KafkaZkClient zk(@Value("${kafka.zookeeperHosts}") String zookeeperHosts) {
            return new KafkaZkClient(zookeeperHosts);
        }
    }

}
