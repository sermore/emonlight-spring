package net.reliqs.emonlight.xbeegw.send.jms;

import net.reliqs.emonlight.commons.config.Probe;
import net.reliqs.emonlight.commons.config.Probe.Type;
import net.reliqs.emonlight.commons.data.Data;
import net.reliqs.emonlight.commons.data.StoreData;
import net.reliqs.emonlight.xbeegw.send.AbstractService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by sergio on 25/02/17.
 */
public class JmsService extends AbstractService<StoreData, JmsAsyncService> {
    private static final Logger log = LoggerFactory.getLogger(JmsService.class);

    public JmsService(JmsAsyncService service, boolean enableBackup, String backupPath, int maxBatch, boolean realTime,
            long timeOutOnClose, int maxQueued) {
        super(service, "JMS", enableBackup, backupPath, maxBatch, realTime, timeOutOnClose, true, maxQueued);
    }

    @Override
    protected StoreData createData(Probe p, Type t, Data d) {
        return new StoreData(p, t, d);
    }
}
