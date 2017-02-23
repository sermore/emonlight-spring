package net.reliqs.emonlight.streams.config;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class Processor {	
	
	@NotNull
	private String name;
	
	@NotNull
	private String source;
	
	@Min(500)
	private long interval;
	
	private boolean running;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public long getInterval() {
		return interval;
	}

	public void setInterval(long interval) {
		this.interval = interval;
	}

	public boolean isRunning() {
		return running;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}
	
}
