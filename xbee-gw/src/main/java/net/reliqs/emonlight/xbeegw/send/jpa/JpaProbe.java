package net.reliqs.emonlight.xbeegw.send.jpa;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "probe")
public class JpaProbe {

    @Id
    private Integer id;

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
//                ", node=" + node +
                '}';
    }
}
