package net.reliqs.emonlight.xbeegw.send.jpa;

import net.reliqs.emonlight.commons.config.Node;
import net.reliqs.emonlight.commons.config.Settings;
import net.reliqs.emonlight.commons.data.StoreData;
import net.reliqs.emonlight.xbeegw.send.AbstractAsyncService;

import java.time.Instant;

public class JpaAsyncService extends AbstractAsyncService<StoreData> {

    private Settings settings;
    private JpaNodeRepo nodeRepo;
    private JpaProbeRepo probeRepo;
    private JpaDataRepo dataRepo;

    public JpaAsyncService(Settings settings, JpaNodeRepo nodeRepo, JpaProbeRepo probeRepo, JpaDataRepo dataRepo,
            int maxRetries, boolean ignoreErrors) {
        super("JPA", maxRetries, ignoreErrors);
        this.settings = settings;
        this.nodeRepo = nodeRepo;
        this.probeRepo = probeRepo;
        this.dataRepo = dataRepo;
    }

    @Override
    protected boolean send(StoreData t) {
        Node node = settings.findNodeById(t.getNode());
        JpaNode jpaNode = nodeRepo.findById(node.getId());
        if (jpaNode == null) {
            log.error("JPA: node not found from {}", t);
            return false;
        }
        JpaProbe jpaProbe = probeRepo.findById(t.getProbe());
        if (jpaProbe == null) {
            log.error("JPA: no single probe found from {}", t);
            return false;
        }
        assert jpaProbe.getNode().getId().equals(jpaNode.getId());
        JpaData data = new JpaData(jpaProbe, Instant.ofEpochMilli(t.getT()), Double.valueOf(t.getV()).floatValue());
        dataRepo.save(data);
//        log.trace("JPA OK {}", t);
        return true;
    }

}
