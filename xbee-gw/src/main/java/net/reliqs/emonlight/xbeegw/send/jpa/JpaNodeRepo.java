package net.reliqs.emonlight.xbeegw.send.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaNodeRepo extends JpaRepository<JpaNode, Long> {
}
