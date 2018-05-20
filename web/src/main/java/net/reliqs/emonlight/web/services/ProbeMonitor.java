package net.reliqs.emonlight.web.services;

import net.reliqs.emonlight.commons.config.Probe;
import net.reliqs.emonlight.commons.config.Settings;
import net.reliqs.emonlight.web.data.Data;
import net.reliqs.emonlight.web.data.ProbeMonitorData;
import net.reliqs.emonlight.web.data.StoreData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ProbeMonitor {
    private static final Logger log = LoggerFactory.getLogger(ProbeMonitor.class);

    private int historyDays;

    private Map<Integer, ProbeMonitorData> probes;

    public ProbeMonitor(Settings settings, DataRepo repo, int historyDays) {
        this.historyDays = historyDays;
        Instant tend = Instant.now();
        Instant tstart = tend.minus(this.historyDays, ChronoUnit.DAYS);
        log.debug("init, read back {} days starting from {}", this.historyDays, tstart);
        probes = new HashMap<>();
        settings.getProbes().forEach(p -> probes.put(p.getId(), new ProbeMonitorData(p, settings.getTzone(),
                repo.getData(Arrays.asList(p.getId()), tstart.toEpochMilli(), tend.toEpochMilli()).get(p.getId()), tstart)));
    }

    public ProbeMonitorData get(Integer probeId) {
        return probes.get(probeId);
    }

    public boolean add(StoreData data) {
        ProbeMonitorData pmd = get(data.getProbe());
        if (pmd != null) {
            if (pmd.getType() == Probe.Type.valueOf(data.getType())) {
                Data d = new Data(Instant.ofEpochMilli(data.getT()), data.getV());
                log.debug("add P{} {}", data.getProbe(), d);
                return pmd.add(d);
            } else {
                log.warn("data from unknown probe: {}", data);
                // Handle alert message
            }
            return true;
        }
        return false;
    }

    @Scheduled(initialDelay = 3600_000, fixedRate = 3600_000)
    private void scheduledCheck() {
        Instant t = Instant.now();
        log.debug("check {}", t);
        probes.values().forEach(p -> p.check(t));
    }

}
