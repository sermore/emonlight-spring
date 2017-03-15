package net.reliqs.emonlight.xbeegw.xbee;

import net.reliqs.emonlight.xbeegw.config.Settings;
import net.reliqs.emonlight.xbeegw.send.Dispatcher;
import net.reliqs.emonlight.xbeegw.xbee.EndDeviceTest.MyConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

//import net.reliqs.emonlight.commons.kafka.utils.KafkaUtils;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = MyConfig.class)
//@Import({KafkaUtils.class})
@ActiveProfiles("test-end-device")
@EnableAsync
public class EndDeviceTest {

    @Autowired
    Settings settings;
    @Autowired
    Processor p;
    @Autowired
    Dispatcher t;

    @Test
    public void testEndDevice()
            throws InterruptedException {
        for (int i = 0; i < 40000; i++) {
            p.process();
            t.process();
        }
    }

    @Profile("test-end-device")
    @SpringBootApplication(scanBasePackages = "net.reliqs.emonlight.xbeegw")
    static class MyConfig {

    }
}
