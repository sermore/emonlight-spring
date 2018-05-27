package net.reliqs.emonlight.xbeegw.send;

import net.reliqs.emonlight.commons.config.Settings;
import net.reliqs.emonlight.xbeegw.TestApp;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {TestApp.class, Config.class})
@ActiveProfiles("integration,test-queue")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class AbstractServiceIntegrationTest {

    @Autowired
    private Settings settings;
    @Autowired
    private FakeService fakeService;
    @Autowired
    private FakeAsyncService fakeAsyncService;

    @BeforeClass
    public static void beforeClass() throws IOException {
        Files.deleteIfExists(Paths.get("TEST_backup.dat"));
    }

    @Test
    public void testPost() throws InterruptedException {
        fakeService.getQueue().clear();
        fakeService.getInFlight().clear();
        fakeService.setEnableBackup(false);
        fakeAsyncService.setPostCount(0);
        fakeAsyncService.setResult(true);
        fakeAsyncService.setSleepTime(300);
        fakeService.setMaxBatch(2);
        fakeService.setRealTime(false);
        FakeService.populate(settings, fakeService);
        fakeService.post();
        assertThat(fakeService.isReady(), is(false));
        assertThat(fakeService.isQueueEmpty(), is(false));
        assertThat(fakeService.getQueue(), hasSize(1));
        assertThat(fakeService.getInFlight(), hasSize(2));
        assertThat(fakeService.isRunning(), is(true));
        Thread.sleep(400);
        assertThat(fakeService.isRunning(), is(true));
        fakeService.post();
        assertThat(fakeService.getQueue(), hasSize(1));
        assertThat(fakeService.getInFlight(), hasSize(1));
        Thread.sleep(300);
        assertThat(fakeService.getQueue(), hasSize(1));
        assertThat(fakeService.getInFlight(), hasSize(0));
        assertThat(fakeAsyncService.getPostCount(), is(2));
        assertThat(fakeService.isReady(), is(true));
        fakeService.post();
        Thread.sleep(400);
        assertThat(fakeService.getQueue(), hasSize(0));
        assertThat(fakeService.getInFlight(), hasSize(0));
        assertThat(fakeAsyncService.getPostCount(), is(3));
        assertThat(fakeService.isQueueEmpty(), is(true));
    }

    @Test
    public void testPostWithFailures() throws InterruptedException {
        fakeService.getQueue().clear();
        fakeService.getInFlight().clear();
        fakeAsyncService.setPostCount(0);
        fakeAsyncService.setResult(false);
        fakeAsyncService.setSleepTime(300);
        fakeService.setEnableBackup(false);
        fakeService.setMaxBatch(2);
        fakeService.setRealTime(false);
        FakeService.populate(settings, fakeService);
        fakeService.post();
        assertThat(fakeService.isReady(), is(false));
        assertThat(fakeService.isQueueEmpty(), is(false));
        assertThat(fakeService.getQueue(), hasSize(1));
        assertThat(fakeService.getInFlight(), hasSize(2));
        assertThat(fakeService.isRunning(), is(true));
        Thread.sleep(200);
        fakeService.post();
        Thread.sleep(200);
        assertThat(fakeService.isRunning(), is(true));
        assertThat(fakeService.getQueue(), hasSize(1));
        assertThat(fakeService.getInFlight(), hasSize(2));
        fakeService.post();
        Thread.sleep(400);
        assertThat(fakeService.getQueue(), hasSize(1));
        assertThat(fakeService.getInFlight(), hasSize(2));
        assertThat(fakeAsyncService.getPostCount(), is(2));
        assertThat(fakeService.isReady(), is(true));
        fakeService.post();
        Thread.sleep(400);
        assertThat(fakeService.getQueue(), hasSize(1));
        assertThat(fakeService.getInFlight(), hasSize(2));
        assertThat(fakeAsyncService.getPostCount(), is(4));
        assertThat(fakeService.isQueueEmpty(), is(false));
    }


    @Test
    public void testPostWithExceptions() throws InterruptedException {
        fakeService.getQueue().clear();
        fakeService.getInFlight().clear();
        fakeAsyncService.setPostCount(0);
        fakeAsyncService.setResult(true);
        fakeAsyncService.setGenerateException(true);
        fakeAsyncService.setSleepTime(300);
        fakeService.setEnableBackup(false);
        fakeService.setMaxBatch(2);
        fakeService.setRealTime(false);
        FakeService.populate(settings, fakeService);
        fakeService.post();
        assertThat(fakeService.isReady(), is(false));
        assertThat(fakeService.isQueueEmpty(), is(false));
        assertThat(fakeService.getQueue(), hasSize(1));
        assertThat(fakeService.getInFlight(), hasSize(2));
        assertThat(fakeService.isRunning(), is(true));
        Thread.sleep(200);
        fakeService.post();
        Thread.sleep(200);
        assertThat(fakeService.isRunning(), is(false));
        assertThat(fakeService.getQueue(), hasSize(1));
        assertThat(fakeService.getInFlight(), hasSize(2));
        fakeService.post();
        Thread.sleep(400);
        assertThat(fakeService.getQueue(), hasSize(1));
        assertThat(fakeService.getInFlight(), hasSize(2));
        assertThat(fakeAsyncService.getPostCount(), is(2));
        assertThat(fakeService.isReady(), is(true));
        fakeService.post();
        Thread.sleep(400);
        assertThat(fakeService.getQueue(), hasSize(1));
        assertThat(fakeService.getInFlight(), hasSize(2));
        assertThat(fakeAsyncService.getPostCount(), is(3));
        assertThat(fakeService.isQueueEmpty(), is(false));
    }

}