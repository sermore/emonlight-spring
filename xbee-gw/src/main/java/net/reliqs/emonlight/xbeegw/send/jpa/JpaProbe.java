package net.reliqs.emonlight.xbeegw.send.jpa;

import net.reliqs.emonlight.commons.config.Probe;

import javax.persistence.*;

@Entity
@Table(name = "probe")
public class JpaProbe {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    private String name;
    @Enumerated(EnumType.STRING)
    private Probe.Type type;

    @ManyToOne
    private JpaNode node;

    public JpaProbe() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
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

    @Override
    public String toString() {
        return "JpaProbe{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", type=" + type +
                ", node=" + node +
                '}';
    }
}
