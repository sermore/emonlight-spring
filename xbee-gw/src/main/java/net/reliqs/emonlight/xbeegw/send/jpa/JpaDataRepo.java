package net.reliqs.emonlight.xbeegw.send.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaDataRepo extends JpaRepository<JpaData, Long> {
    JpaData findFirstByProbe(final JpaProbe p);
}
