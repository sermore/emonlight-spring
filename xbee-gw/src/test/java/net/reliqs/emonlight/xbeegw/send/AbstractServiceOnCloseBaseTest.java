package net.reliqs.emonlight.xbeegw.send;

import net.reliqs.emonlight.commons.config.Settings;
import net.reliqs.emonlight.commons.data.StoreData;
import net.reliqs.emonlight.commons.utils.ObjStoreToFile;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

public class AbstractServiceOnCloseBaseTest {

    protected static Runnable testRunner;
    @Autowired
    protected Settings settings;
    @Autowired
    protected FakeService fakeService;
    @Autowired
    protected FakeAsyncService fakeAsyncService;
    @Autowired
    protected Dispatcher dispatcher;

    @BeforeClass
    public static void beforeClass() throws IOException {
        Files.deleteIfExists(Paths.get("event-queue-backup.dat"));
        Files.deleteIfExists(Paths.get("TEST_backup.dat"));
    }

    @AfterClass
    public static void afterClass() {
        testRunner.run();
    }

    @Before
    public void beforeTest() {
        assertThat(fakeService.isQueueEmpty(), is(true));
        assertThat(fakeService.getQueue(), hasSize(0));
        assertThat(fakeService.getInFlight(), hasSize(0));
    }

    protected List<LinkedList<StoreData>> getSavedQueue() {
        ObjStoreToFile<LinkedList<StoreData>> s = new ObjStoreToFile<>("TEST_backup.dat", false);
        List<LinkedList<StoreData>> res = s.read();
        return res;
    }

    protected void testExpectedResults(boolean fileExists, int queueLen, int inFlightLen) {
        assertThat(Files.exists(Paths.get("TEST_backup.dat")), is(fileExists));
        assertThat(fakeService.isQueueEmpty(), is(!fileExists));
        assertThat(fakeService.getQueue(), hasSize(queueLen));
        assertThat(fakeService.getInFlight(), hasSize(inFlightLen));
        if (fileExists) {
            List<LinkedList<StoreData>> res = getSavedQueue();
            assertThat(res.get(0), hasSize(queueLen));
            assertThat(res.get(1), hasSize(inFlightLen));
        }
    }

}