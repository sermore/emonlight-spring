package net.reliqs.emonlight.xbeegw.send.restv2;

import net.reliqs.emonlight.commons.config.Probe;
import net.reliqs.emonlight.commons.config.Server;
import net.reliqs.emonlight.commons.data.Data;
import net.reliqs.emonlight.commons.data.StoreData;
import net.reliqs.emonlight.xbeegw.send.AbstractService;

public class RestV2Service extends AbstractService<StoreData, RestV2AsyncService> {

    public RestV2Service(RestV2AsyncService service, String logId, boolean enableBackup, String backupPath,
            int maxBatch, boolean realTime, long timeOutOnClose, boolean active, int maxQueued) {
        super(service, logId, enableBackup, backupPath, maxBatch, realTime, timeOutOnClose, active, maxQueued);
    }

    void configure(RestV2AsyncService asyncService, Server server, boolean enableBackup, int maxBatch) {
    }

    @Override
    protected StoreData createData(Probe p, Probe.Type t, Data d) {
        return new StoreData(p, t, d);
    }
}
