package net.reliqs.emonlight.xbeegw.config;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import net.reliqs.emonlight.xbeegw.config.annotations.ValidProbe;

@ValidProbe
public class Probe {

	public enum Type {
		PULSE, SAMPLE, DHT22_T, DHT22_H, VCC, F_MEAN_5MIN, F_MEAN_15MIN, M_HARD_THRESHOLD, M_SOFT_THRESHOLD_3, M_SOFT_THRESHOLD_2, M_SOFT_THRESHOLD_1
	};

	@Size(min = 1)
	private String name;

	@NotNull
	private Type type;

	private boolean connectedToOutput;

	@Min(0)
	private int sampleTime;

	@DecimalMin("0")
	private double adcMult = 0.0;

	@Min(400)
	@Max(1500)
	private int pulsesPerKilowattHour = 1000;

	@DecimalMin("0")
	private double softThreshold;

	@Min(0)
	private int softThresholdTimeSec;

	@DecimalMin("0")
	private double hardThreshold;

	@Min(0)
	private int hardThresholdTimeSec;

	private Node node;

	private Probe source;

	private List<Probe> filters = new ArrayList<>();

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

	public boolean isConnectedToOutput() {
		return connectedToOutput;
	}

	public void setConnectedToOutput(boolean connectedToOutput) {
		this.connectedToOutput = connectedToOutput;
	}

	public int getSampleTime() {
		return sampleTime;
	}

	public void setSampleTime(int sampleTime) {
		this.sampleTime = sampleTime;
	}

	public double getAdcMult() {
		return adcMult;
	}

	public void setAdcMult(double adcMult) {
		this.adcMult = adcMult;
	}

	public int getPulsesPerKilowattHour() {
		return pulsesPerKilowattHour;
	}

	public void setPulsesPerKilowattHour(int pulsesPerKilowattHour) {
		this.pulsesPerKilowattHour = pulsesPerKilowattHour;
	}

	public double getSoftThreshold() {
		return softThreshold;
	}

	public void setSoftThreshold(double softThreshold) {
		this.softThreshold = softThreshold;
	}

	public int getSoftThresholdTimeSec() {
		return softThresholdTimeSec;
	}

	public void setSoftThresholdTimeSec(int softThresholdTimeSec) {
		this.softThresholdTimeSec = softThresholdTimeSec;
	}

	public double getHardThreshold() {
		return hardThreshold;
	}

	public void setHardThreshold(double hardThreshold) {
		this.hardThreshold = hardThreshold;
	}

	public int getHardThresholdTimeSec() {
		return hardThresholdTimeSec;
	}

	public void setHardThresholdTimeSec(int hardThresholdTimeSec) {
		this.hardThresholdTimeSec = hardThresholdTimeSec;
	}

	public Node getNode() {
		return node;
	}

	public void setNode(Node node) {
		this.node = node;
	}

	public Probe getSource() {
		return source;
	}

	public void setSource(Probe source) {
		this.source = source;
	}

	public List<Probe> getFilters() {
		return filters;
	}

	public void setFilters(List<Probe> filters) {
		this.filters = filters;
	}

	public boolean isFilter() {
		return getType().ordinal() > Type.VCC.ordinal();
	}

	public boolean hasThresholds() {
		return getSoftThreshold() > 0 || getHardThreshold() > 0;
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
		return "Probe [name=" + name + ", type=" + type + ", node=" + node + "]";
	}

}
