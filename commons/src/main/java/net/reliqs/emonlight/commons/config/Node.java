package net.reliqs.emonlight.commons.config;

import net.reliqs.emonlight.commons.config.annotations.ValidNode;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ValidNode
@Validated
public class Node implements Serializable {

    static final long serialVersionUID = 1L;

    @NotNull
    @Min(1)
    private Integer id;

    @Size(min = 1)
    @NotNull
    private String name;
    @Size(min = 2, max = 25)
    @NotNull
    private String address;
    @NotNull
    private OpMode mode;
    @NotNull
    @Min(1)
    private Integer sampleTime;
    @NotNull
    private boolean vccFromADC = false;
    @DecimalMin("0")
    @DecimalMax("3.3")
    @NotNull
    private Double vccThreshold;
    @Min(255)
    @Max(4095)
    @NotNull
    private Integer adcRange = 4095;
    @DecimalMin("1.8")
    @DecimalMax("3.3")
    @NotNull
    private Double adcVRef = 3.3;
    @Valid
    private List<Probe> probes = new ArrayList<>();
    private Map<ProbeKey, Probe> probeMap;
    private Map<String, Probe> probeNameMap;

    public Node() {
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public OpMode getMode() {
        return mode;
    }

    public void setMode(OpMode mode) {
        this.mode = mode;
    }

    public Integer getSampleTime() {
        return sampleTime;
    }

    public void setSampleTime(Integer sampleTime) {
        this.sampleTime = sampleTime;
    }

    public boolean isVccFromADC() {
        return vccFromADC;
    }

    public void setVccFromADC(boolean vccFromADC) {
        this.vccFromADC = vccFromADC;
    }

    public Double getVccThreshold() {
        return vccThreshold;
    }

    public void setVccThreshold(Double vccThreshold) {
        this.vccThreshold = vccThreshold;
    }

    public Integer getAdcRange() {
        return adcRange;
    }

    public void setAdcRange(Integer adcRange) {
        this.adcRange = adcRange;
    }

    public Double getAdcVRef() {
        return adcVRef;
    }

    public void setAdcVRef(Double adcVRef) {
        this.adcVRef = adcVRef;
    }

    public List<Probe> getProbes() {
        return probes;
    }

    public void setProbes(List<Probe> probes) {
        this.probes = probes;
    }

    public Probe findProbeByTypeAndPort(Probe.Type type, byte port) {
        return probeMap.get(new ProbeKey(type, port));
    }

    // FIXME handle multiple probes with same type
    public Probe findProbeByType(Probe.Type type) {
        return findProbeByTypeAndPort(type, getDefaultPort(type));
    }

    public Probe findProbeByName(String name) {
        return probeNameMap.get(name);
    }

    public byte getDefaultPort(Probe.Type type) {
        switch (type) {
            case PULSE:
                return 3;
            case DHT22_H:
            case DHT22_T:
                return 10;
        }
        return 0;
    }

    void initMaps() {
        probeMap = new HashMap<>(probes.size());
        probeNameMap = new HashMap<>(probes.size());
        for (Probe p : probes) {
            probeMap.put(new ProbeKey(p.getType(), p.getPort()), p);
            probeNameMap.put(p.getName(), p);
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((address == null) ? 0 : address.hashCode());
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
        Node other = (Node) obj;
        if (address == null) {
            if (other.address != null)
                return false;
        } else if (!address.equals(other.address))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return String.format("N[%d, %s, %s]", id, name, address);
    }

    public enum OpMode {
        UNCONFIGURED, PULSE, PULSE_DHT22, DHT22, PULSE_DS18B20, DS18B20
    }

}
