package net.reliqs.emonlight.xbeegw.send.jpa;

import net.reliqs.emonlight.xbeegw.config.Probe;
import net.reliqs.emonlight.xbeegw.send.AbstractAsyncService;
import net.reliqs.emonlight.xbeegw.send.StoreData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

public class JpaAsyncService extends AbstractAsyncService<StoreData> {
    private static final Logger log = LoggerFactory.getLogger(JpaAsyncService.class);

    private JpaNodeRepo nodeRepo;
    private JpaProbeRepo probeRepo;
    private JpaDataRepo dataRepo;

    public JpaAsyncService(JpaNodeRepo nodeRepo, JpaProbeRepo probeRepo, JpaDataRepo dataRepo) {
        super();
        this.nodeRepo = nodeRepo;
        this.probeRepo = probeRepo;
        this.dataRepo = dataRepo;
    }

    @Override
    protected boolean send(StoreData t) {
        JpaNode node = nodeRepo.findByName(t.getNode());
        if (node == null) {
            log.error("JPA: node not found from {}", t);
            return false;
        }
        Probe.Type type = Probe.Type.valueOf(t.getType());
        JpaProbe probe = probeRepo.findByNodeAndName(node, t.getProbe());
        if (probe == null) {
            log.error("JPA: no single probe found from {}", t);
            return false;
        }
        JpaData data = new JpaData(probe, Instant.ofEpochMilli(t.getT()), Double.valueOf(t.getV()).floatValue());
        dataRepo.save(data);
//        log.trace("JPA OK {}", t);
        return true;
    }

}
