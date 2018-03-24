package net.reliqs.emonlight.xbeegw.send;

import net.reliqs.emonlight.commons.config.Probe;
import net.reliqs.emonlight.commons.config.Settings;
import net.reliqs.emonlight.commons.config.SettingsConfiguration;
import net.reliqs.emonlight.commons.config.SettingsService;
import net.reliqs.emonlight.xbeegw.publish.Data;
import net.reliqs.emonlight.xbeegw.state.ObjStoreToFile;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.LinkedList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {SettingsConfiguration.class, SettingsService.class})
@ActiveProfiles("test-queue")
public class AbstractServiceTest {

    @Autowired
    private Settings settings;

    @Test
    public void testIsReadyAndIsQueueEmpty() {
        FakeAsyncService as = new FakeAsyncService(1);
        FakeService s = new FakeService(as);
        assertThat(s.isReady(), is(false));
        assertThat(s.isQueueEmpty(), is(true));
        FakeService.populate(settings, s);
        assertThat(s.isReady(), is(true));
        assertThat(s.isQueueEmpty(), is(false));
        s.setMaxBatch(1);
        s.post();
        assertThat(s.isReady(), is(true));
        assertThat(s.isQueueEmpty(), is(false));
        s.post();
        assertThat(s.isReady(), is(true));
        assertThat(s.isQueueEmpty(), is(false));
        s.post();
        assertThat(s.isReady(), is(false));
        assertThat(s.isQueueEmpty(), is(true));
    }

    @Test
    public void testReceive() {
        FakeAsyncService as = new FakeAsyncService(1);
        FakeService s = new FakeService(as);
        Probe p = settings.getProbes().findFirst().get();
        assertThat(s.getQueue(), hasSize(0));
        s.receive(p, p.getType(), new Data(Instant.now().toEpochMilli(), 145.43));
        assertThat(s.getQueue(), hasSize(1));
    }

    @Test
    public void testOnClose() throws IOException {
        String path = "TEST_backup.dat";
        Files.deleteIfExists(Paths.get(path));
        FakeAsyncService as = new FakeAsyncService(1);
        FakeService s = new FakeService(as);
        assertThat(s.isEnableBackup(), is(true));
        assertThat(s.getBackupPath(), is(path));
        FakeService.populate(settings, s);
        as.setResult(false);
        s.setMaxBatch(1);
        s.post();
        assertThat(s.getQueue(), hasSize(2));
        assertThat(s.getInFlight(), hasSize(1));
        s.onClose();
        assertThat(Files.exists(Paths.get(path)), is(true));
        ObjStoreToFile<LinkedList<StoreData>> store = new ObjStoreToFile<>(path, false);
        List<LinkedList<StoreData>> res = store.read();
        assertThat(res, hasSize(2));
        assertThat(res.get(0), hasSize(2));
        assertThat(res.get(1), hasSize(1));
    }

    @Test
    public void testOnInit() throws IOException {
        String path = "TEST_backup.dat";
        Files.deleteIfExists(Paths.get(path));
        Probe p = settings.getProbes().findFirst().get();

        ObjStoreToFile<LinkedList<StoreData>> store = new ObjStoreToFile<>(path, false);
        LinkedList<StoreData> q = new LinkedList<>();
        q.add(new StoreData(p, p.getType(), new Data(Instant.now().toEpochMilli(), 145.43)));
        store.add(q);
        q = new LinkedList<>();
        q.add(new StoreData(p, p.getType(), new Data(Instant.now().toEpochMilli(), 145.43)));
        q.add(new StoreData(p, p.getType(), new Data(Instant.now().toEpochMilli(), 145.43)));
        store.add(q);
        store.write();

        FakeAsyncService as = new FakeAsyncService(1);
        FakeService s = new FakeService(as);
        assertThat(s.getBackupPath(), is(path));
        s.setEnableBackup(false);
        s.onInit();
        assertThat(Files.exists(Paths.get(path)), is(true));
        assertThat(s.getQueue(), hasSize(0));
        assertThat(s.getInFlight(), hasSize(0));

        s.setEnableBackup(true);
        s.onInit();
        assertThat(Files.exists(Paths.get(path)), is(false));
        assertThat(s.getQueue(), hasSize(1));
        assertThat(s.getInFlight(), hasSize(2));
    }

    @Test
    public void testPost() {
        FakeAsyncService as = new FakeAsyncService(1);
        FakeService s = new FakeService(as);
        FakeService.populate(settings, s);

        s.setMaxBatch(1);
        s.post();
        assertThat(s.getQueue(), hasSize(2));
        assertThat(s.getInFlight(), hasSize(0));
        assertThat(as.getPostCount(), is(1));

        as.setPostCount(0);
        s.setMaxBatch(2);
        s.post();
        assertThat(s.getQueue(), hasSize(0));
        assertThat(s.getInFlight(), hasSize(0));
        assertThat(s.isReady(), is(false));
        assertThat(s.isQueueEmpty(), is(true));
        assertThat(as.getPostCount(), is(2));

        as.setResult(false);
        as.setPostCount(0);
        FakeService.populate(settings, s);
        s.setMaxBatch(0);
        s.post();
        assertThat(s.getQueue(), hasSize(0));
        assertThat(s.getInFlight(), hasSize(3));
        assertThat(as.getPostCount(), is(1));
        s.post();
        assertThat(s.getQueue(), hasSize(0));
        assertThat(s.getInFlight(), hasSize(3));
        assertThat(as.getPostCount(), is(2));

        as.setPostCount(0);
        as.setResult(true);
        s.setMaxBatch(0);
        s.setRealTime(true);
        FakeService.populate(settings, s);
        assertThat(s.getQueue(), hasSize(0));
        assertThat(s.getInFlight(), hasSize(0));
        assertThat(as.getPostCount(), is(6));
    }

}