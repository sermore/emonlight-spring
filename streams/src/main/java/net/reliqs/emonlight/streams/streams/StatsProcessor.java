package net.reliqs.emonlight.streams.streams;

import java.time.Instant;

import org.apache.kafka.streams.processor.Processor;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.state.KeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StatsProcessor implements Processor<Long, Double> {
	private static final Logger log = LoggerFactory.getLogger(StatsProcessor.class);

	private ProcessorContext context;

	private KeyValueStore<String, Double> sum;

	private KeyValueStore<String, Long> count;

	private final long interval;
	
	private final String topicLabel;

	private final String topic;

	private final String sinkMean;

	private final String sinkVar;

	private final String key1;

	private final String key2;

	private boolean running;

	public StatsProcessor(String topic, String topicLabel, long interval, boolean running, String sinkMean, String sinkVar) {
		this.topic = topic;
		this.topicLabel = topicLabel;
		this.interval = interval;
		this.running = running;
		this.sinkMean = sinkMean;
		this.sinkVar = sinkVar;
		key1 = topic + "_1_" + (running ? "win" : "all") + "_" + (interval);
		key2 = topic + "_2_" + (running ? "win" : "all") + "_" + (interval);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void init(ProcessorContext context) {
		this.context = context;
		this.context.schedule(interval);
		sum = (KeyValueStore<String, Double>) context.getStateStore("stats_sum");
		count = (KeyValueStore<String, Long>) context.getStateStore("stats_count");
	}

	@Override
	public void process(Long time, Double value) {
		Double s = sum.get(key1);
		s = s == null ? value : s + value;
		sum.put(key1, s);
		Long n = count.get(key1);
		n = n == null ? 1 : ++n;
		count.put(key1, n);
		Long tsum = count.get(key2);
		tsum = tsum == null ? time : tsum + time;
		count.put(key2, tsum);
		Double s2 = sum.get(key2);
		s2 = s2 == null ? 0.0 : s2;
		double mean = s / n;
		double delta = value - mean;
		mean += delta / n;
		double delta2 = value - mean;
		s2 += delta * delta2;
		sum.put(key2, s2);
		log.trace("{}[{}]: ADD n={}, s={}, s2={}", topicLabel, sinkMean, n, s, s2);
	}

	@Override
	public void punctuate(long timestamp) {
		Double s = sum.get(key1);
		Long n = count.get(key1);
		Long ts = count.get(key2);
		if (n != null) {
			Long t = running ? ts / n : timestamp;
			Double mean = s / n;
			if (sinkMean != null)
				context.forward(t, mean, sinkMean);
			Double s2 = sum.get(key2);
			if (n > 1 && s2 != null) {
				Double var = s2 / (n - 1);
				if (sinkVar != null)
					context.forward(t, var, sinkVar);
				log.debug("{}[{}]: EMIT {}[{}] n={}, m={}, var={}", topicLabel, sinkMean, Instant.ofEpochMilli(t),
						Instant.ofEpochMilli(timestamp), n, mean, var);
			}
			if (running) {
				sum.delete(key1);
				sum.delete(key2);
				count.delete(key1);
				count.delete(key2);
			}
			context.commit();
		}
	}

	@Override
	public void close() {
		sum.close();
		count.close();
	}

}
