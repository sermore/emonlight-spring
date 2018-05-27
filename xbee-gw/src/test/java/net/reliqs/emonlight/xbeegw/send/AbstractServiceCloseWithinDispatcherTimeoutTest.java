package net.reliqs.emonlight.xbeegw.send;

import net.reliqs.emonlight.xbeegw.TestApp;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {TestApp.class, Config.class})
@ActiveProfiles("integration,test-queue")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class AbstractServiceCloseWithinDispatcherTimeoutTest extends AbstractServiceOnCloseBaseTest {

    @BeforeClass
    public static void beforeClass() throws IOException {
        Files.deleteIfExists(Paths.get("TEST_backup.dat"));
    }

    @Test
    public void testCloseWithinDispatcherTimeout() {
        dispatcher.setTimeOut(2000);
        testRunner = () -> this.testExpectedResults(false, 0, 0);
        fakeAsyncService.setResult(true);
        fakeAsyncService.setSleepTime(200);
        fakeService.setMaxBatch(2);
        fakeService.setTimeOutOnClose(500);
        FakeService.populate(settings, fakeService);
        fakeService.post();
        assertThat(fakeService.isQueueEmpty(), is(false));
    }

}