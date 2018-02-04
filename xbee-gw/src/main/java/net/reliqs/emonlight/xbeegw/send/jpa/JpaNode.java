package net.reliqs.emonlight.xbeegw.send.jpa;

import net.reliqs.emonlight.xbeegw.config.Node;

import javax.persistence.*;
import java.util.Collection;

@Entity
@Table(name = "node")
public class JpaNode {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String name;
    private String address;
    private Node.OpMode mode;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "node")
    private Collection<JpaProbe> probes;

    public JpaNode() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Node.OpMode getMode() {
        return mode;
    }

    public void setMode(Node.OpMode mode) {
        this.mode = mode;
    }

    public Collection<JpaProbe> getProbes() {
        return probes;
    }

    public void setProbes(Collection<JpaProbe> probes) {
        this.probes = probes;
    }
}
