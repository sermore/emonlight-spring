package net.reliqs.emonlight.web.entities;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created by sergio on 19/02/17.
 */
public interface NodeRepo extends JpaRepository<Node, Long> {

    Node findByAuthenticationToken(String authenticationToken);
}
