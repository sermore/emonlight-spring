package net.reliqs.emonlight.web.services;

import net.reliqs.emonlight.commons.config.Probe;
import net.reliqs.emonlight.commons.config.Settings;
import net.reliqs.emonlight.commons.data.StoreData;
import net.reliqs.emonlight.commons.utils.CollectionStoreToFile;
import net.reliqs.emonlight.web.data.Data;
import net.reliqs.emonlight.web.data.ProbeMonitorData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ProbeMonitor {
    private static final Logger log = LoggerFactory.getLogger(ProbeMonitor.class);

    private final Map<Integer, ProbeMonitorData> probes;
    @Value("${probeMonitor.backup:probe-monitor-backup.dat}")
    private String backupPath;

    @Value("${probeMonitor.backup-enabled:true}")
    private boolean backupEnabled;

    private Settings settings;

    public ProbeMonitor(Settings settings) {
        this.settings = settings;
        this.probes = new HashMap<>();
    }

    @PostConstruct
    void init() {
        if (backupEnabled) {
            loadProbeData(settings);
        } else {
            log.debug("probeMonitor backup disabled, no data loaded");
        }
        settings.getProbes().filter(p -> !probes.containsKey(p.getId())).forEach(p -> {
            log.debug("init probeMonitorData {}", p.getId());
            probes.put(p.getId(), new ProbeMonitorData(p, settings.getTzone()));
        });
    }

    @PreDestroy
    void close() {
        if (backupEnabled) {
            saveProbeData();
        }
    }

    private void loadProbeData(Settings settings) {
        log.info("read probeMonitorData from {}", Paths.get(backupPath).toAbsolutePath());
        CollectionStoreToFile<ProbeMonitorData> s = new CollectionStoreToFile<>(backupPath);
        Collection<ProbeMonitorData> data = s.read(false);
        log.info("read {} probeMonitorData from {}", data.size(), Paths.get(backupPath).toAbsolutePath());
        for (ProbeMonitorData p : data) {
            if (settings.getProbes().anyMatch(q -> q.getId().equals(p.getId()))) {
                probes.put(p.getId(), p);
            } else {
                log.debug("skipped probeMonitorData {}", p.getId());
            }
        }
    }

    void saveProbeData() {
        log.info("save {} probeMonitorData to {}", probes.size(), Paths.get(backupPath).toAbsolutePath());
        CollectionStoreToFile<ProbeMonitorData> s = new CollectionStoreToFile<>(backupPath);
        s.write(probes.values());
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

    Collection<ProbeMonitorData> getProbes() {
        return this.probes.values();
    }

    public boolean isBackupEnabled() {
        return backupEnabled;
    }

}
