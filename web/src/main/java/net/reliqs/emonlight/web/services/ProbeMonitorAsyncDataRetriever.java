package net.reliqs.emonlight.web.services;

import net.reliqs.emonlight.web.data.ProbeMonitorData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;

public class ProbeMonitorAsyncDataRetriever {
    private static final Logger log = LoggerFactory.getLogger(ProbeMonitorAsyncDataRetriever.class);
    private final ProbeMonitor probeMonitor;
    private DataRepo repo;

    public ProbeMonitorAsyncDataRetriever(DataRepo repo, ProbeMonitor probeMonitor) {
        this.repo = repo;
        this.probeMonitor = probeMonitor;
    }

    @Async
    public void populateProbeMonitorData() {
        for (ProbeMonitorData p : probeMonitor.getProbes()) {
            if (p.populate(repo) > 0L && probeMonitor.isBackupEnabled()) {
                probeMonitor.saveProbeData();
            }
        }
    }

}
