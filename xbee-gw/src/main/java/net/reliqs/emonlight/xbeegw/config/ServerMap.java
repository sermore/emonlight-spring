package net.reliqs.emonlight.xbeegw.config;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class ServerMap {

	@NotNull
	private Probe probe;
	
	@Min(1)
	private int nodeId;
	
	@Size(min = 5)
	private String apiKey;

	public Probe getProbe() {
		return probe;
	}

	public void setProbe(Probe probe) {
		this.probe = probe;
	}

	public int getNodeId() {
		return nodeId;
	}

	public void setNodeId(int nodeId) {
		this.nodeId = nodeId;
	}

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

}
