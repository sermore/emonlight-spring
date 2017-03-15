package net.reliqs.emonlight.streams.config;

import net.reliqs.emonlight.streams.config.StreamsAppConfigTest.MyConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {MyConfig.class})
public class StreamsAppConfigTest {

    @Autowired
    StreamsAppConfig config;

    @Test
    public void test() {
        assertThat(config).isNotNull();
        assertThat(config.getProcessors()).isNotEmpty();
        assertThat(config.getProcessors()).size().isEqualTo(3);
        assertThat(config.getProcessors().get(0).getName()).isEqualTo("10u");
        assertThat(config.getProcessors().get(0).getInterval()).isEqualTo(10000L);
        assertThat(config.getProcessors().get(0).isRunning()).isEqualTo(false);
        assertThat(config.getTopics()).isNotEmpty();
        assertThat(config.getTopics()).size().isEqualTo(1);
        assertThat(config.getTopics().keySet().iterator().next()).isEqualTo("kafka-pino_a7LiZVht-FNo3i8bUf61");
        assertThat(config.getTopics().values().iterator().next()).isEqualTo("TEST_PULSE");
    }

    @Configuration
    @EnableConfigurationProperties(StreamsAppConfig.class)
    static class MyConfig {
    }

}
