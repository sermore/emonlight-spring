package net.reliqs.emonlight.web.data;

import net.reliqs.emonlight.commons.config.Node;
import net.reliqs.emonlight.commons.config.Probe;
import net.reliqs.emonlight.commons.config.Settings;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayDeque;
import java.util.Map;
import java.util.Queue;

public class DataQueue {

    private Map<Integer, Queue<Data>> dataQueue;

    public boolean add(Settings settings, ZoneOffset zoneOffset, StoreData data) {
        Node n = settings.findNodeByName(data.getNode());
        if (n != null) {
            Probe p = n.findProbeByName(data.getProbe());
            if (Probe.Type.valueOf(data.getType()) == p.getType()) {
                Queue<Data> q = dataQueue.get(p.getId());
                if (q == null) {
                    q = new ArrayDeque<>();
                    dataQueue.put(p.getId(), q);
                }
                q.add(new Data(Instant.ofEpochMilli(data.getT()).atOffset(zoneOffset), data.getV()));
            } else {
                // Handle alert message
            }
            return true;
        }
        return false;
    }
}
