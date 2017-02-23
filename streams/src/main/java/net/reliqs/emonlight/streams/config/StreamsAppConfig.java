package net.reliqs.emonlight.streams.config;

import java.util.List;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "config")
public class StreamsAppConfig {

	private Map<String, String> topics;

	private List<Processor> processors;

	public Map<String, String> getTopics() {
		return topics;
	}

	public void setTopics(Map<String, String> topics) {
		this.topics = topics;
	}

	public List<Processor> getProcessors() {
		return processors;
	}

	public void setProcessors(List<Processor> processors) {
		this.processors = processors;
	}

}
