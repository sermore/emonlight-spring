package net.reliqs.emonlight.xbeegw.send.jpa;

import net.reliqs.emonlight.commons.config.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaNodeRepo extends JpaRepository<JpaNode, Long> {
    static final Logger log = LoggerFactory.getLogger(JpaNodeRepo.class);

    @Cacheable("nodes")
    JpaNode findById(Integer id);

    default JpaNode createNodeIfNotExists(JpaProbeRepo probeRepo, Node n) {
        JpaNode node = findById(n.getId());
        if (node == null) {
            node = new JpaNode();
            node.setId(n.getId());
            node = save(node);
            log.trace("JPA: node created {}", node);
            final JpaNode newNode = node;
            n.getProbes().forEach(p -> probeRepo.createProbeIfNotExists(newNode, p));
        }
        return node;
    }

}
