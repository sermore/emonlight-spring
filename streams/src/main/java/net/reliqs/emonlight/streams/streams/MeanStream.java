package net.reliqs.emonlight.streams.streams;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.*;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.Properties;

public class MeanStream {

    @Value("${bootstrapServers}")
    private String bootstrapServers = "lime2:9092";

    @Value("${zookeeperHosts}")
    private String zkConnect = "lime2:2181";

    private String stateDir = "/tmp/kafka-streams";

    private String topic = "kafka-pino_a7LiZVht-FNo3i8bUf61";
    // private String topic = "kafka-pino_Rk6Bvfpxy5CrDDwq_YwD";

    private KafkaStreams streams;

    public MeanStream() {
    }

    KafkaStreams buildStream() throws IOException {
        final Properties config = new Properties();
        // Give the Streams application a unique name. The name must be unique
        // in the Kafka cluster
        // against which the application is run.
        config.put(StreamsConfig.APPLICATION_ID_CONFIG, "app_mean_" + topic);
        // Where to find Kafka broker(s).
        config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        // Where to find the corresponding ZooKeeper ensemble.
        config.put(StreamsConfig.ZOOKEEPER_CONNECT_CONFIG, zkConnect);
        // Where to find the Confluent schema registry instance(s)
        // streamsConfiguration.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG,
        // schemaRegistryUrl);
        // Specify default (de)serializers for record keys and for record
        // values.
        config.put(StreamsConfig.KEY_SERDE_CLASS_CONFIG, Serdes.Long().getClass().getName());
        config.put(StreamsConfig.VALUE_SERDE_CLASS_CONFIG, Serdes.Double().getClass().getName());
        // config.put(StreamsAppConfig.COMMIT_INTERVAL_MS_CONFIG, 1000);
        config.put(StreamsConfig.STATE_DIR_CONFIG, stateDir);
        // Records should be flushed every 10 seconds. This is less than the
        // default
        // in order to keep this example interactive.
        config.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 4 * 1000);
        config.put(StreamsConfig.TIMESTAMP_EXTRACTOR_CLASS_CONFIG, EventTimeExtractor.class);

        final KStreamBuilder builder = new KStreamBuilder();
        KStream<Long, Double> source = builder.stream(topic);

        long interval = 10000L;

//		Stores.create("sum_10_" + topic).withLongKeys().withDoubleValues().persistent().build();
//		Stores.create("count_10_" + topic).withLongKeys().withLongValues().persistent().build();
//		
//		source.process(() -> new Processor<Long, Double>() {
//
//			private ProcessorContext context;
//			private KeyValueStore<Long, Double> sum;
//			private KeyValueStore<Long, Long> count;
//
//			@Override
//			public void init(ProcessorContext context) {
//				this.context = context;
//				this.context.schedule(interval);
//				sum = (KeyValueStore<Long, Double>) context.getStateStore("sum_10_" + topic);
//				count = (KeyValueStore<Long, Long>) context.getStateStore("count_10_" + topic);
//			}
//
//			@Override
//			public void process(Long key, Double value) {
//				Double s = sum.get(1L);				
//				sum.put(1L, s == null ? 0 : s + value);
//				Long c = count.get(1L);
//				count.put(1L, c == null ? 1 : ++c);
//			}
//
//			@Override
//			public void punctuate(long timestamp) {
//				Double s = sum.get(1L);
//				Long c = count.get(1L);				
//				if (c != null) {
//					context.forward(timestamp, s/c);
//					sum.delete(1L);
//					count.delete(1L);
//					context.commit();
//				}
//			}
//
//			@Override
//			public void close() {
//				sum.close();
//				count.close();
//			}
//
//		}, "sum_10_" + topic, "count_10_" + topic);

        KGroupedStream<Long, Double> g = source.groupBy((k, v) -> 1L);

        KTable<Long, Long> count = g.count("count_" + topic);
        KTable<Long, Double> sum = g.reduce((v1, v2) -> v1 + v2, "sum_" + topic);
        KTable<Long, Double> mean = sum.join(count, (s, c) -> s.doubleValue() / c.doubleValue());
        mean.to("mean_" + topic);

        KTable<Windowed<Long>, Long> count10 = g.count(TimeWindows.of(interval), "st_count_10_" + topic);
        KTable<Windowed<Long>, Double> sum10 = g.reduce((v1, v2) -> v1 + v2, TimeWindows.of(interval),
                "st_sum_10_" + topic);

        KTable<Windowed<Long>, Double> mean10 = sum10.join(count10, (s, c) -> s.doubleValue() / c.doubleValue()).join(count10, (s, c) -> s.doubleValue());

        mean10.toStream((k, v) -> (k.key())).to("mean_10_" + topic);
        count10.mapValues(v -> v.doubleValue()).toStream((k, v) -> (k.window().end() + k.window().start()) / 2)
                .to("count_10_" + topic);
        sum10.toStream((k, v) -> (k.window().end() + k.window().start()) / 2).to("sum_10_" + topic);

        // sum10.toStream((k, v) -> String.format("%d %s", k.window().start(),
        // k.key())).to(Serdes.String(),
        // Serdes.Double(), "mean_10_" + topic);

        KafkaStreams streams = new KafkaStreams(builder, config);
        return streams;
    }

    public void start() throws IOException {
        streams = buildStream();
        streams.cleanUp();
        streams.start();

        // Add shutdown hook to respond to SIGTERM and gracefully close Kafka
        // Streams
        // Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
    }

    @PreDestroy
    public void close() {
        streams.close();
    }

}
