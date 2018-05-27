package net.reliqs.emonlight.xbeegw;

import net.reliqs.emonlight.commons.config.SettingsConfiguration;
import net.reliqs.emonlight.commons.config.SettingsService;
import net.reliqs.emonlight.xbeegw.xbee.XbeeProcessor;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jms.JmsAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@Import({SettingsConfiguration.class, SettingsService.class})
@EnableAutoConfiguration(
        exclude = {JmsAutoConfiguration.class,
                KafkaAutoConfiguration.class, WebMvcAutoConfiguration.class})
//@EnableConfigurationProperties({Settings.class})
@EnableAsync
@Profile("integration")
public class TestApp {

    @MockBean
    @Order(0)
    XbeeProcessor xbeeGateway;

}
