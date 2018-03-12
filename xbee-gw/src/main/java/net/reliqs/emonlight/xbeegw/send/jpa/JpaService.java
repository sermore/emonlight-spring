package net.reliqs.emonlight.xbeegw.send.jpa;

import net.reliqs.emonlight.commons.config.Probe;
import net.reliqs.emonlight.xbeegw.publish.Data;
import net.reliqs.emonlight.xbeegw.send.AbstractService;
import net.reliqs.emonlight.xbeegw.send.StoreData;

public class JpaService extends AbstractService<StoreData, JpaAsyncService> {

    public JpaService(JpaAsyncService service) {
        super(service, "JPA", 0);
    }

    @Override
    protected StoreData createData(Probe p, Probe.Type t, Data d) {
        return new StoreData(p, t, d);
    }
}
