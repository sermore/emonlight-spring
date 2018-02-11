package net.reliqs.emonlight.xbeegw.send.jpa;

import net.reliqs.emonlight.xbeegw.config.Probe;
import net.reliqs.emonlight.xbeegw.send.StoreData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.concurrent.ListenableFuture;

import java.time.Instant;
import java.util.Queue;

public class JpaAsyncService {
    private static final Logger log = LoggerFactory.getLogger(JpaAsyncService.class);

    private JpaNodeRepo nodeRepo;
    private JpaProbeRepo probeRepo;
    private JpaDataRepo dataRepo;

    public JpaAsyncService(JpaNodeRepo nodeRepo, JpaProbeRepo probeRepo, JpaDataRepo dataRepo) {
        this.nodeRepo = nodeRepo;
        this.probeRepo = probeRepo;
        this.dataRepo = dataRepo;
    }

    @Async
    @Transactional
    public ListenableFuture<Integer> post(Queue<StoreData> inFlight) {
        int cnt = 0;
        while (!inFlight.isEmpty()) {
            StoreData t = inFlight.peek();
            if (send(t)) {
                cnt++;
            }
            inFlight.poll();
        }
        AsyncResult<Integer> res = new AsyncResult<>(cnt);
        return res;
    }

    private boolean send(StoreData t) {
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
