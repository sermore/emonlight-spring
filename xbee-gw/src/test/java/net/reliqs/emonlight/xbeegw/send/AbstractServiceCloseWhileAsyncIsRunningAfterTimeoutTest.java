package net.reliqs.emonlight.xbeegw.send;

import net.reliqs.emonlight.xbeegw.TestApp;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {TestApp.class, Config.class})
@ActiveProfiles("integration,test-queue")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class AbstractServiceCloseWhileAsyncIsRunningAfterTimeoutTest extends AbstractServiceOnCloseBaseTest {

    @Test
    public void testCloseWhileAsyncIsRunningAfterTimeout() throws InterruptedException {
        dispatcher.setTimeOut(500);
        testRunner = () -> this.testExpectedResults(true, 1, 2);
        fakeAsyncService.setResult(true);
        fakeAsyncService.setSleepTime(1000);
        fakeService.setMaxBatch(2);
        fakeService.setTimeOutOnClose(300);
        FakeService.populate(settings, fakeService);
        fakeService.post();
        assertThat(fakeService.isQueueEmpty(), is(false));
        Thread.sleep(100);
    }

}