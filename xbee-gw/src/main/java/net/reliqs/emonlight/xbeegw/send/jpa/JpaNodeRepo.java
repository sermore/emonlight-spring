package net.reliqs.emonlight.xbeegw.send.jpa;

import net.reliqs.emonlight.commons.config.Node;
import net.reliqs.emonlight.xbeegw.GwException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaNodeRepo extends JpaRepository<JpaNode, Long> {
    static final Logger log = LoggerFactory.getLogger(JpaNodeRepo.class);

    @Cacheable("nodes")
    JpaNode findByName(String name);

    long countByName(String name);

    default JpaNode createNodeIfNotExists(JpaProbeRepo probeRepo, Node n) {
        JpaNode node;
        long cnt = countByName(n.getName());
        if (cnt == 0) {
            node = new JpaNode();
            node.setName(n.getName());
            node.setAddress(n.getAddress());
            node.setMode(n.getMode());
            node = save(node);
            log.trace("JPA: node created {}", node);
            final JpaNode newNode = node;
            n.getProbes().forEach(p -> probeRepo.createProbeIfNotExists(newNode, p));
        } else if (cnt > 1) {
            throw new GwException("JPA: Node " + n.getName() + " not unique");
        } else {
            node = findByName(n.getName());
        }
        return node;
    }

}
