package net.reliqs.emonlight.web.entities;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;

/**
 * Created by sergio on 19/02/17.
 */
@Entity
public class Sample {

    @Id
    @SequenceGenerator(name = "sample_id_seq", sequenceName = "sample_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sample_id_seq")
//    @GeneratedValue(strategy = GenerationType.AUTO)
//    @NotNull
    private Long id;

    @NotNull
    @ManyToOne
    private Node node;

    @NotNull
    @Column(columnDefinition = "TIMESTAMP (6)")
    private Timestamp sampleTime;

    @NotNull
    private Double value;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Node getNode() {
        return node;
    }

    public void setNode(Node node) {
        this.node = node;
    }

    public Timestamp getSampleTime() {
        return sampleTime;
    }

    public void setSampleTime(Timestamp sampleTime) {
        this.sampleTime = sampleTime;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Sample)) return false;

        Sample sample = (Sample) o;

        if (id != null ? !id.equals(sample.id) : sample.id != null) return false;
        if (node != null ? !node.equals(sample.node) : sample.node != null) return false;
        if (sampleTime != null ? !sampleTime.equals(sample.sampleTime) : sample.sampleTime != null) return false;
        return value != null ? value.equals(sample.value) : sample.value == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (node != null ? node.hashCode() : 0);
        result = 31 * result + (sampleTime != null ? sampleTime.hashCode() : 0);
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }
}
