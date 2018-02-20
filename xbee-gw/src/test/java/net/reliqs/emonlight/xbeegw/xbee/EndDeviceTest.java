package net.reliqs.emonlight.xbeegw.xbee;

import net.reliqs.emonlight.xbeegw.Runner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jms.JmsAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

//import net.reliqs.emonlight.commons.kafka.utils.KafkaUtils;

@ActiveProfiles("test-end-device")
@RunWith(SpringRunner.class)
@SpringBootTest //(classes = MyConfig.class)
@ComponentScan(basePackages = {"net.reliqs.emonlight.xbeegw"})
//@Import({KafkaUtils.class})
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class, JmsAutoConfiguration.class})
@EnableConfigurationProperties
@EnableAsync
public class EndDeviceTest {

    @Autowired
    Runner runner;

    @MockBean
    XbeeGateway gateway;

    @Test
    public void testEndDevice() {
//        when(gateway.)
        runner.run(40000L);
    }

//    @Profile("test-end-device")
//    @SpringBootApplication(scanBasePackages = "net.reliqs.emonlight.xbeegw")
//    static class MyConfig {
//
//    }
}
