package net.reliqs.emonlight.xbeegw.send.jpa;

import javax.persistence.*;
import java.util.Collection;

@Entity
@Table(name = "node")
public class JpaNode {

    @Id
    private Integer id;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "node")
    private Collection<JpaProbe> probes;

    public JpaNode() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Collection<JpaProbe> getProbes() {
        return probes;
    }

    public void setProbes(Collection<JpaProbe> probes) {
        this.probes = probes;
    }

    @Override
    public String toString() {
        return "JpaNode{" +
                "id=" + id +
                ", probes=" + probes +
                '}';
    }
}
