package net.reliqs.emonlight.xbeegw.send.restv2;

import net.reliqs.emonlight.commons.config.Probe;
import net.reliqs.emonlight.xbeegw.publish.Data;
import net.reliqs.emonlight.xbeegw.send.AbstractService;
import net.reliqs.emonlight.xbeegw.send.StoreData;

public class RestV2Service extends AbstractService<StoreData, RestV2AsyncService> {


    public RestV2Service(RestV2AsyncService service, String logId, boolean enableBackup, String backupPath,
            int maxBatch, boolean realTime, long timeOutOnClose) {
        super(service, logId, enableBackup, backupPath, maxBatch, realTime, timeOutOnClose);
    }

    @Override
    protected StoreData createData(Probe p, Probe.Type t, Data d) {
        return new StoreData(p, t, d);
    }
}
