package net.reliqs.emonlight.xbeegw.send;

import net.reliqs.emonlight.commons.config.Probe;
import net.reliqs.emonlight.commons.config.Settings;
import net.reliqs.emonlight.xbeegw.publish.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

class FakeService extends AbstractService<StoreData, FakeAsyncService> {
    private static final Logger log = LoggerFactory.getLogger(DispatcherTest.FakeService.class);

    public FakeService(FakeAsyncService service) {
        super(service, "TEST", true, "TEST_backup.dat", 1, false, 200, true, 0);
    }

    static void populate(Settings settings, FakeService fakeService) {
        Probe p = settings.getProbes().findFirst().get();
        fakeService.receive(p, p.getType(), new Data(Instant.now().toEpochMilli(), 145.43));
        fakeService.receive(p, p.getType(), new Data(Instant.now().toEpochMilli(), 146.43));
        fakeService.receive(p, p.getType(), new Data(Instant.now().toEpochMilli(), 147.43));
    }

    @Override
    protected StoreData createData(Probe p, Probe.Type t, Data d) {
        return new StoreData(p, t, d);
    }

}
