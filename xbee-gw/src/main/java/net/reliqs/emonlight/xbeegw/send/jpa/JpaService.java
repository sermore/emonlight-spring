package net.reliqs.emonlight.xbeegw.send.jpa;

import net.reliqs.emonlight.commons.config.Probe;
import net.reliqs.emonlight.xbeegw.publish.Data;
import net.reliqs.emonlight.xbeegw.send.AbstractService;
import net.reliqs.emonlight.xbeegw.send.StoreData;

public class JpaService extends AbstractService<StoreData, JpaAsyncService> {

    public JpaService(JpaAsyncService service, boolean enableBackup, String backupPath, int maxBatch, boolean realTime,
            long timeOutOnClose, int maxQueued) {
        super(service, "JPA", enableBackup, backupPath, maxBatch, realTime, timeOutOnClose, true, maxQueued);
    }

    @Override
    protected StoreData createData(Probe p, Probe.Type t, Data d) {
        return new StoreData(p, t, d);
    }

    @Override
    public void receive(Probe p, Probe.Type t, Data d) {
        if (t == p.getType()) {
            super.receive(p, t, d);
        }
    }
}
