package net.reliqs.emonlight.streams.streams;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;

import javax.annotation.PreDestroy;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.processor.StateStoreSupplier;
import org.apache.kafka.streams.processor.TopologyBuilder;
import org.apache.kafka.streams.state.Stores;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import net.reliqs.emonlight.commons.kafka.utils.KafkaUtils;
import net.reliqs.emonlight.streams.config.Processor;
import net.reliqs.emonlight.streams.config.StreamsAppConfig;

@Component
public class StreamProcess {

	@Autowired
	private KafkaUtils kafkaUtils;

	@Value("${kafka.bootstrap.servers}")
	private String bootstrapServers;

	@Value("${kafka.zookeeperHosts}")
	private String zkConnect;

	@Value("${streams.stateDir}")
	private String stateDir = "/tmp/kafka-streams";

	@Value("${streams.applicationId}")
	private String applicationId;

	private StreamsAppConfig config;

	private KafkaStreams streams;

	public StreamProcess(final StreamsAppConfig config) {
		this.config = config;
	}

	KafkaStreams buildStream() throws IOException {
		final Properties kconfig = new Properties();
		// Give the Streams application a unique name. The name must be unique
		// in the Kafka cluster
		// against which the application is run.
		kconfig.put(StreamsConfig.APPLICATION_ID_CONFIG, applicationId);
		// Where to find Kafka broker(s).
		kconfig.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
		// Where to find the corresponding ZooKeeper ensemble.
		kconfig.put(StreamsConfig.ZOOKEEPER_CONNECT_CONFIG, zkConnect);
		// Where to find the Confluent schema registry instance(s)
		// streamsConfiguration.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG,
		// schemaRegistryUrl);
		// Specify default (de)serializers for record keys and for record
		// values.
		kconfig.put(StreamsConfig.KEY_SERDE_CLASS_CONFIG, Serdes.Long().getClass().getName());
		kconfig.put(StreamsConfig.VALUE_SERDE_CLASS_CONFIG, Serdes.Double().getClass().getName());
		// config.put(StreamsAppConfig.COMMIT_INTERVAL_MS_CONFIG, 1000);
		kconfig.put(StreamsConfig.STATE_DIR_CONFIG, stateDir);
		// Records should be flushed every 10 seconds. This is less than the
		// default
		// in order to keep this example interactive.
		// config.put(StreamsAppConfig.COMMIT_INTERVAL_MS_CONFIG, 4 * 1000);
		kconfig.put(StreamsConfig.TIMESTAMP_EXTRACTOR_CLASS_CONFIG, EventTimeExtractor.class);

		StateStoreSupplier sumStore = Stores.create("stats_sum").withKeys(Serdes.String()).withValues(Serdes.Double())
				.persistent().build();
		StateStoreSupplier countStore = Stores.create("stats_count").withKeys(Serdes.String()).withValues(Serdes.Long())
				.persistent().build();

		final TopologyBuilder builder = new TopologyBuilder();
		List<String> nn = new ArrayList<>();
		// for (String topic : topics) {
		// }
		for (Entry<String, String> e : config.getTopics().entrySet()) {
			TopicStreamBuilder tb = new TopicStreamBuilder(builder, e.getKey(), e.getValue());
			tb.buildSource("source");
			for (Processor p : config.getProcessors()) {
				tb.buildProcessor("source", p.getName(), p.getInterval(), p.isRunning());
				nn.add("proc_" + p.getName() + "_" + e.getKey());
			}
		}
		builder.addStateStore(sumStore, nn.toArray(new String[0])).addStateStore(countStore, nn.toArray(new String[0]));

		KafkaStreams streams = new KafkaStreams(builder, kconfig);
		return streams;
	}

	public void start() throws IOException {
		initTopics();
		streams = buildStream();
		streams.cleanUp();
		streams.start();

		// Add shutdown hook to respond to SIGTERM and gracefully close Kafka
		// Streams
		// Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
	}

	void initTopics() {
		List<String> newTopics = new ArrayList<>();
		for (String t : config.getTopics().keySet()) {
			for (Processor p : config.getProcessors()) {
				newTopics.add("mean_" + p.getName() + "_" + t);
				newTopics.add("var_" + p.getName() + "_" + t);
			}
		}
		kafkaUtils.initTopics(newTopics);
	}

	@PreDestroy
	public void close() {
		if (streams != null) {
			streams.close();
		}
	}

}
