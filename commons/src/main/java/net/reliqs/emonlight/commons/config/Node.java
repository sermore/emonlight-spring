package net.reliqs.emonlight.commons.config;

import net.reliqs.emonlight.commons.config.annotations.ValidNode;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ValidNode
public class Node {

    @Size(min = 1)
    private String name;

    ;
    // @Size(min = 10, max = 10)
    private String address;
    @NotNull
    private OpMode mode;
    @Min(1)
    private int sampleTime;
    private boolean vccFromADC = false;
    @DecimalMin("0")
    @DecimalMax("3.3")
    private double vccThreshold;
    @Min(255)
    @Max(4095)
    private int adcRange = 4095;
    @DecimalMin("1.8")
    @DecimalMax("3.3")
    private double adcVRef = 3.3;
    @Valid
    private List<Probe> probes;
    private Map<ProbeKey, Probe> probeMap;

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

    public int getSampleTime() {
        return sampleTime;
    }

    public void setSampleTime(int sampleTime) {
        this.sampleTime = sampleTime;
    }

    public boolean isVccFromADC() {
        return vccFromADC;
    }

    public void setVccFromADC(boolean vccFromADC) {
        this.vccFromADC = vccFromADC;
    }

    public double getVccThreshold() {
        return vccThreshold;
    }

    public void setVccThreshold(double vccThreshold) {
        this.vccThreshold = vccThreshold;
    }

    public int getAdcRange() {
        return adcRange;
    }

    public void setAdcRange(int adcRange) {
        this.adcRange = adcRange;
    }

    public double getAdcVRef() {
        return adcVRef;
    }

    public void setAdcVRef(double adcVRef) {
        this.adcVRef = adcVRef;
    }

    public List<Probe> getProbes() {
        return probes;
    }

    public void setProbes(List<Probe> probes) {
        this.probes = probes;
    }

    public Probe getProbe(Probe.Type type, byte port) {
        return probeMap.get(new ProbeKey(type, port));
    }

    // FIXME handle multiple probes with same type
    public Probe getProbe(Probe.Type type) {
        return getProbe(type, getDefaultPort(type));
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

    void initProbeMap() {
        probeMap = new HashMap<>(probes.size());
        for (Probe p : probes) {
            probeMap.put(new ProbeKey(p.getType(), p.getPort()), p);
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
        return "N [" + name + ", " + address + "]";
    }

    public enum OpMode {
        UNCONFIGURED, PULSE, PULSE_DHT22, DHT22, PULSE_DS18B20, DS18B20
    }

}
