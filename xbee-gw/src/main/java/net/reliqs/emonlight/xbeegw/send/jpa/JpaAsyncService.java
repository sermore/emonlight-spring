package net.reliqs.emonlight.xbeegw.send.jpa;

import net.reliqs.emonlight.commons.config.Node;
import net.reliqs.emonlight.commons.config.Settings;
import net.reliqs.emonlight.xbeegw.send.AbstractAsyncService;
import net.reliqs.emonlight.xbeegw.send.StoreData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

public class JpaAsyncService extends AbstractAsyncService<StoreData> {
    private static final Logger log = LoggerFactory.getLogger(JpaAsyncService.class);

    private Settings settings;
    private JpaNodeRepo nodeRepo;
    private JpaProbeRepo probeRepo;
    private JpaDataRepo dataRepo;

    public JpaAsyncService(Settings settings, JpaNodeRepo nodeRepo, JpaProbeRepo probeRepo, JpaDataRepo dataRepo) {
        super();
        this.settings = settings;
        this.nodeRepo = nodeRepo;
        this.probeRepo = probeRepo;
        this.dataRepo = dataRepo;
    }

    @Override
    protected boolean send(StoreData t) {
        Node node = settings.findNodeByName(t.getNode());
        JpaNode jpaNode = nodeRepo.findById(node.getId());
        if (jpaNode == null) {
            log.error("JPA: node not found from {}", t);
            return false;
        }
        JpaProbe jpaProbe = probeRepo.findById(node.findProbeByName(t.getProbe()).getId());
        if (jpaProbe == null) {
            log.error("JPA: no single probe found from {}", t);
            return false;
        }
        JpaData data = new JpaData(jpaProbe, Instant.ofEpochMilli(t.getT()), Double.valueOf(t.getV()).floatValue());
        dataRepo.save(data);
//        log.trace("JPA OK {}", t);
        return true;
    }

}
