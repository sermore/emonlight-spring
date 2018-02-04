package net.reliqs.emonlight.xbeegw.send.jpa;

import net.reliqs.emonlight.xbeegw.config.Probe;

import javax.persistence.*;

@Entity
@Table(name = "probe")
public class JpaProbe {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String name;
    private Probe.Type type;

    @ManyToOne(cascade = CascadeType.PERSIST)
    private JpaNode node;

    public JpaProbe() {
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

    public Probe.Type getType() {
        return type;
    }

    public void setType(Probe.Type type) {
        this.type = type;
    }

    public JpaNode getNode() {
        return node;
    }

    public void setNode(JpaNode node) {
        this.node = node;
    }

}
