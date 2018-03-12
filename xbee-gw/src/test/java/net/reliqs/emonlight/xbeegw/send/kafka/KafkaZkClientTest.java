package net.reliqs.emonlight.xbeegw.send.kafka;

import net.reliqs.emonlight.commons.config.Settings;
import net.reliqs.emonlight.commons.kafka.utils.KafkaZkClient;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jms.JmsAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.test.rule.KafkaEmbedded;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {KafkaConfig.class, Settings.class})
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class, JmsAutoConfiguration.class, KafkaAutoConfiguration.class})
@EnableAsync
@ActiveProfiles("kafka")
public class KafkaZkClientTest {

    @ClassRule
    public static KafkaEmbedded embeddedKafka = new KafkaEmbedded(1, true, "topic");

    @TestConfiguration
    static class MyConfig {

        @Bean
        KafkaZkClient zk(@Value("${kafka.zookeeperHosts}") String zookeeperHosts) {
            return new KafkaZkClient(zookeeperHosts);
        }
    }

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

}
