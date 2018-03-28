package net.reliqs.emonlight.web.services;

import net.reliqs.emonlight.commons.config.Probe;
import net.reliqs.emonlight.commons.config.Settings;
import net.reliqs.emonlight.web.data.Data;
import net.reliqs.emonlight.web.data.ProbeMonitorData;
import net.reliqs.emonlight.web.data.StoreData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class ProbeMonitor {
    private static final Logger log = LoggerFactory.getLogger(ProbeMonitor.class);

    private Map<Integer, ProbeMonitorData> probes;

    public ProbeMonitor(Settings settings) {
        log.debug("init");
        probes = new HashMap<>();
        settings.getProbes().forEach(p -> probes.put(p.getId(), new ProbeMonitorData(p)));
    }

    public ProbeMonitorData get(Integer probeId) {
        return probes.get(probeId);
    }

    public boolean add(StoreData data) {
        ProbeMonitorData pmd = get(data.getProbe());
        if (pmd != null) {
            if (pmd.getType() == Probe.Type.valueOf(data.getType())) {
                Data d = new Data(Instant.ofEpochMilli(data.getT()), data.getV());
                log.debug("add {}", d);
                return pmd.add(d);
            } else {
                log.warn("data from unknown probe: {}", data);
                // Handle alert message
            }
            return true;
        }
        return false;
    }

}
