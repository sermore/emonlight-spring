package net.reliqs.emonlight.xbeegw.send.jpa;

import javax.persistence.*;
import java.sql.Timestamp;
import java.time.Instant;

@Entity
@Table(name = "data")
public class JpaData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    // FIXME datetime is mysql specific!
    @Column(nullable = false, length = 3, columnDefinition = "datetime(3)")
//    @Column(nullable = false, length = 3)
    private Timestamp time;
    @Column(nullable = false)
    private Float value;

    @ManyToOne
    private JpaProbe probe;

    public JpaData() {
    }

    public JpaData(JpaProbe probe, Instant time, Float value) {
        this.probe = probe;
        this.time = Timestamp.from(time);
        this.value = value;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Timestamp getTime() {
        return time;
    }

    public void setTime(Timestamp time) {
        this.time = time;
    }

    public Float getValue() {
        return value;
    }

    public void setValue(Float value) {
        this.value = value;
    }

    public JpaProbe getProbe() {
        return probe;
    }

    public void setProbe(JpaProbe probe) {
        this.probe = probe;
    }

}
