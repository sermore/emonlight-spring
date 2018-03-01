package net.reliqs.emonlight.xbeegw;

import net.reliqs.emonlight.xbeegw.events.EventQueue;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

//import net.reliqs.emonlight.commons.kafka.utils.KafkaUtils;

@ActiveProfiles({"integration", "test-end-device"})
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestApp.class)
public class EndDeviceTest {

    @Autowired
    private EventQueue queue;

    @Test
    public void testEndDevice() {
        queue.run(5000L);
    }

}
