package net.reliqs.emonlight.xbeegw.send;

import net.reliqs.emonlight.commons.config.Probe;
import net.reliqs.emonlight.commons.config.Settings;
import net.reliqs.emonlight.commons.data.Data;
import net.reliqs.emonlight.xbeegw.TestApp;
import net.reliqs.emonlight.xbeegw.publish.Publisher;
import net.reliqs.emonlight.xbeegw.send.services.DeliveryService;
import org.junit.AfterClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.context.annotation.Scope;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Instant;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {TestApp.class, DispatcherTest.FakeService.class})
//@ComponentScan("net.reliqs.emonlight.xbeegw.send")
//@SpringBootTest(classes = {Settings.class, Dispatcher.class, Publisher.class})
@ActiveProfiles("integration,jpa")
public class DispatcherTest {

    static int postCount = 0;
    @Autowired
    Settings settings;

//    @Autowired
//    ApplicationContext ctx;
    //    @Autowired
//    JpaService service;
    @Autowired
    Publisher publisher;
    @Autowired
    Dispatcher dispatcher;
    @Autowired
    FakeService fakeService1;
    @Autowired
    FakeService fakeService2;

    @AfterClass
    public static void afterTest() {
        assertThat(postCount, is(15));
    }

    @Test
    @DirtiesContext
    public void testPreDestroy() {
//        publisher.addService(service);
        publisher.addService(fakeService1);
        publisher.addService(fakeService2);
        fakeService1.setCount(5);
        fakeService2.setCount(10);
        Probe p = settings.getProbes().findFirst().get();
        publisher.publish(p, p.getType(), new Data(Instant.now().toEpochMilli(), 145.43));
    }

    @TestComponent
    @Scope("prototype")
    static class FakeService implements DeliveryService {
        private static final Logger log = LoggerFactory.getLogger(FakeService.class);

        int count = 5;

        public void setCount(int count) {
            this.count = count;
        }

        @Override
        public void post() {
            count--;
            postCount++;
            log.debug("post {}", count);
        }

        @Override
        public boolean isReady() {
            return false;
        }

        @Override
        public boolean isQueueEmpty() {
            log.debug("isQueueEmpty {}", count);
            return count <= 0;
        }

        @Override
        public void receive(Probe p, Probe.Type type, Data d) {
            log.debug("receive");
        }
    }
}