package net.reliqs.emonlight.xbeegw.send.jms;

import net.reliqs.emonlight.xbeegw.config.Probe;
import net.reliqs.emonlight.xbeegw.config.Probe.Type;
import net.reliqs.emonlight.xbeegw.publish.Data;
import net.reliqs.emonlight.xbeegw.send.AbstractService;
import net.reliqs.emonlight.xbeegw.send.StoreData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by sergio on 25/02/17.
 */
public class JmsService extends AbstractService<StoreData, JmsAsyncService> {
    private static final Logger log = LoggerFactory.getLogger(JmsService.class);

    public JmsService(JmsAsyncService service) {
        super(service, "JMS", 0);
    }

    @Override
    protected StoreData createData(Probe p, Type t, Data d) {
        return new StoreData(p, t, d);
    }
}
