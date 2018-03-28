package net.reliqs.emonlight.web.data;

import net.reliqs.emonlight.commons.config.Node;
import net.reliqs.emonlight.commons.config.Probe;
import net.reliqs.emonlight.commons.config.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

public class DataQueue {
    private static final Logger log = LoggerFactory.getLogger(DataQueue.class);

    private Map<Integer, Deque<Data>> dataQueue = new HashMap<>();

    public boolean add(Settings settings, StoreData data) {
        Node n = settings.findNodeById(data.getNode());
        if (n != null) {
            Probe p = settings.findProbeById(data.getProbe());
            if (Probe.Type.valueOf(data.getType()) == p.getType()) {
                Deque<Data> q = dataQueue.get(p.getId());
                if (q == null) {
                    q = new ArrayDeque<>();
                    dataQueue.put(p.getId(), q);
                }
                Data d = new Data(Instant.ofEpochMilli(data.getT()), data.getV());
                log.debug("add {}", d);
                q.add(d);
            } else {
                // Handle alert message
            }
            return true;
        }
        return false;
    }

    public Deque<Data> get(Integer probeId) {
        return dataQueue.get(probeId);
    }

}
