package net.reliqs.emonlight.xbeegw.send;

import net.reliqs.emonlight.commons.config.Probe;
import net.reliqs.emonlight.commons.config.Settings;
import net.reliqs.emonlight.xbeegw.publish.Data;
import net.reliqs.emonlight.xbeegw.publish.Publisher;
import net.reliqs.emonlight.xbeegw.state.ObjStoreToFile;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jms.JmsAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.LinkedList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.is;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {Settings.class})
@ActiveProfiles("test-settings")
@EnableConfigurationProperties
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class, JmsAutoConfiguration.class, KafkaAutoConfiguration.class})
@EnableAsync
public class AbstractServiceTest {

    private static int runCount = 0;
    private static int postCount = 0;
    private static Boolean[] results = {false, false};

    @Autowired
    private Settings settings;
    @Autowired
    private Publisher publisher;
    @Autowired
    private FakeService fakeService;
//    @Autowired
//    private Dispatcher dispatcher;

    @BeforeClass
    public static void beforeTest() throws IOException {
    }

    @AfterClass
    public static void afterTest() {
        assertThat(results, arrayContaining(true, true));
//        assertThat(saveOnCloseWithErrorsResult, is(true));
    }

    @Test
    @DirtiesContext
    public void testSaveOnClose() {
        populate();
    }

    @Test
    @DirtiesContext
    public void testSaveOnCloseWithErrors() {
        populate();
    }

    private void populate() {
        publisher.addService(fakeService);
        Probe p = settings.getProbes().findFirst().get();
        publisher.publish(p, p.getType(), new Data(Instant.now().toEpochMilli(), 145.43));
        publisher.publish(p, p.getType(), new Data(Instant.now().toEpochMilli(), 146.43));
        publisher.publish(p, p.getType(), new Data(Instant.now().toEpochMilli(), 147.43));
    }

    @TestConfiguration
    static class Config {
        @Bean(initMethod = "onInit", destroyMethod = "onClose")
        Controller controller() {
            return new Controller();
        }

        @Bean
        @DependsOn("controller")
        Dispatcher dispatcher() {
            return new Dispatcher(publisher());
        }

        @Bean
        Publisher publisher() {
            return new Publisher();
        }

        @Bean
        FakeAsyncService fakeAsyncService() {
            return new FakeAsyncService();
        }

        @Bean(initMethod = "onInit", destroyMethod = "onClose")
        FakeService fakeService() {
            return new FakeService(fakeAsyncService());
        }
    }

    static class FakeAsyncService extends AbstractAsyncService<StoreData> {

        @Override
        protected boolean send(StoreData t) {
            postCount++;
            if (runCount > 1)
                throw new RuntimeException("XXXX");
            else {
                try {
                    Thread.sleep(1000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return true;
            }
        }
    }

    static class FakeService extends AbstractService<StoreData, FakeAsyncService> {
        private static final Logger log = LoggerFactory.getLogger(DispatcherTest.FakeService.class);

        public FakeService(FakeAsyncService service) {
            super(service, "TEST", 1);
        }

        @Override
        protected StoreData createData(Probe p, Probe.Type t, Data d) {
            return new StoreData(p, t, d);
        }
    }

    static class Controller {
        public void onInit() throws IOException {
            postCount = 0;
            if (runCount == 0) {
                Files.deleteIfExists(Paths.get("TEST_store.dat"));
            }
        }

        public void onClose() {
            ObjStoreToFile<LinkedList<StoreData>> s = new ObjStoreToFile<>("TEST_store.dat", runCount > 0);
            List<LinkedList<StoreData>> res = s.read();
            assertThat(res.size(), is(2));
            assertThat(res.get(0).size(), is(runCount > 0 ? 3 : 2));
            assertThat(res.get(1).size(), is(runCount));
            assertThat(postCount, is(runCount + 1));
            results[runCount] = true;
            runCount++;
//            if (runCount++ == 0) {
//                saveOnCloseResult = true; // res.size() == 2 && res.get(0).size() == 2 && res.get(1).size() == 0 && postCount == 1;
//            } else {
//                saveOnCloseWithErrorsResult =  res.size() == 2 && res.get(0).size() == 2 && res.get(1).size() == 1 && postCount == 1;
//            }
        }
    }

}