package net.reliqs.emonlight.xbeegw.send.jpa;

import net.reliqs.emonlight.commons.config.Probe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaProbeRepo extends JpaRepository<JpaProbe, Long> {
    static final Logger log = LoggerFactory.getLogger(JpaNodeRepo.class);

    @Cacheable("probes")
    JpaProbe findById(final Integer id);

    default JpaProbe createProbeIfNotExists(JpaNode node, Probe p) {
        JpaProbe probe = findById(p.getId());
        if (probe == null) {
            probe = new JpaProbe();
            probe.setId(p.getId());
            probe.setNode(node);
            probe = save(probe);
            log.debug("JPA: probe created {}", probe);
        }
        return probe;
    }

}
