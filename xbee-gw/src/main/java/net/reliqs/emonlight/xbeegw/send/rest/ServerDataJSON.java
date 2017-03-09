package net.reliqs.emonlight.xbeegw.send.rest;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ServerDataJSON {

	private List<NodeDataJSON> nodes = new ArrayList<>();

	public List<NodeDataJSON> getNodes() {
		return nodes;
	}

	public void setNodes(List<NodeDataJSON> nodes) {
		this.nodes = nodes;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((nodes == null) ? 0 : nodes.hashCode());
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
		ServerDataJSON other = (ServerDataJSON) obj;
		if (nodes == null) {
			if (other.nodes != null)
				return false;
		} else if (!nodes.equals(other.nodes))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return String.format("SD [nodes=%d, d=%d]", nodes.size(),
				nodes.stream().mapToInt(n -> n.getD().size()).sum());
	}
}
