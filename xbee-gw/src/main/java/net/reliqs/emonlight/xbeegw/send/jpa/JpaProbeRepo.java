package net.reliqs.emonlight.xbeegw.send.jpa;

import net.reliqs.emonlight.xbeegw.GwException;
import net.reliqs.emonlight.xbeegw.config.Probe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaProbeRepo extends JpaRepository<JpaProbe, Long> {
    static final Logger log = LoggerFactory.getLogger(JpaNodeRepo.class);

    @Cacheable("probes")
    JpaProbe findByNodeAndName(final JpaNode node, final String name);

    long countByNodeAndName(final JpaNode node, final String name);

    default JpaProbe createProbeIfNotExists(JpaNode node, Probe p) {
        long cnt = countByNodeAndName(node, p.getName());
        JpaProbe probe;
        if (cnt == 0) {
            probe = new JpaProbe();
            probe.setNode(node);
            probe.setName(p.getName());
            probe.setType(p.getType());
            probe = save(probe);
            log.debug("JPA: probe created {}", probe);
        } else if (cnt > 1) {
            throw new GwException("JPA: Probe " + p.getName() + " not unique");
        } else {
            probe = findByNodeAndName(node, p.getName());
        }
        return probe;
    }

}
