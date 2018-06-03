package net.reliqs.emonlight.commons.config;

import net.reliqs.emonlight.commons.config.annotations.ValidProbe;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.*;
import java.io.Serializable;
import java.util.Arrays;

@ValidProbe
@Validated
public class Probe implements Serializable {

    static final long serialVersionUID = 1L;

    @NotNull
    @Min(1)
    private Integer id;
    @Size(min = 1)
    private String name;
    @NotNull
    private Type type;
    @NotNull
    private Byte port;
    @Min(0)
    private Integer sampleTime;
    @DecimalMin("0")
    private Double adcMult = 0.0;
    @Min(400)
    @Max(1500)
    private Integer pulsesPerKilowattHour = 1000;
    @DecimalMin("0")
    private Double softThreshold;
    @Min(0)
    private Integer softThresholdTimeSec;
    @DecimalMin("0")
    private Double hardThreshold;
    @Min(0)
    private Integer hardThresholdTimeSec;
    private Node node;

    public Probe() {
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

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Byte getPort() {
        return port;
    }

    public void setPort(Byte port) {
        this.port = port;
    }

    public Integer getSampleTime() {
        return sampleTime;
    }

    public void setSampleTime(Integer sampleTime) {
        this.sampleTime = sampleTime;
    }

    public Double getAdcMult() {
        return adcMult;
    }

    public void setAdcMult(Double adcMult) {
        this.adcMult = adcMult;
    }

    public Integer getPulsesPerKilowattHour() {
        return pulsesPerKilowattHour;
    }

    public void setPulsesPerKilowattHour(Integer pulsesPerKilowattHour) {
        this.pulsesPerKilowattHour = pulsesPerKilowattHour;
    }

    public Double getSoftThreshold() {
        return softThreshold;
    }

    public void setSoftThreshold(Double softThreshold) {
        this.softThreshold = softThreshold;
    }

    public Integer getSoftThresholdTimeSec() {
        return softThresholdTimeSec;
    }

    public void setSoftThresholdTimeSec(Integer softThresholdTimeSec) {
        this.softThresholdTimeSec = softThresholdTimeSec;
    }

    public Double getHardThreshold() {
        return hardThreshold;
    }

    public void setHardThreshold(Double hardThreshold) {
        this.hardThreshold = hardThreshold;
    }

    public Integer getHardThresholdTimeSec() {
        return hardThresholdTimeSec;
    }

    public void setHardThresholdTimeSec(Integer hardThresholdTimeSec) {
        this.hardThresholdTimeSec = hardThresholdTimeSec;
    }

    public Node getNode() {
        return node;
    }

    public void setNode(Node node) {
        this.node = node;
    }

    public boolean hasThresholds() {
        return getSoftThreshold() != null && getSoftThreshold() > 0 || getHardThreshold() != null && getHardThreshold() > 0;
    }

    public int getTimeout() {
        if (type == Type.PULSE) {
            return 500_000;
        } else {
            int t = sampleTime > 0 ? sampleTime : getNode() != null ? getNode().getSampleTime() : 100_000;
            return t * 5;
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Probe other = (Probe) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return String.format("P[%d, %s, %s]", id, name, type);
    }

    public enum Type {
        PULSE, DHT22_T, DHT22_H, DS18B20, VCC, THRESOLD_ALARM, DATA_MISSING_ALARM;

        public static Type[] options() {
            return Arrays.stream(values()).filter(t -> t.ordinal() < THRESOLD_ALARM.ordinal()).toArray(Type[]::new);
        }
    }

}
