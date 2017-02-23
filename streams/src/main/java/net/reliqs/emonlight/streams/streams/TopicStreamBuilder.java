package net.reliqs.emonlight.streams.streams;

import org.apache.kafka.streams.processor.TopologyBuilder;

class TopicStreamBuilder {
	private TopologyBuilder builder;
	private String topic;
	private String topicLabel;

	public TopicStreamBuilder(TopologyBuilder builder, String topic, String topicLabel) {
		super();
		this.builder = builder;
		this.topic = topic;
		this.topicLabel = topicLabel;
	}

	private String n(String name) {
		return name + "_" + topic;
	}

	void buildSource(String source) {
		builder.addSource(n(source), topic);
	}

	void buildProcessor(String source, String processor, long interval, boolean running) {
		builder.addProcessor(n("proc_" + processor),
				() -> new StatsProcessor(topic, topicLabel, interval, running, n("sink_mean_" + processor),
						n("sink_var_" + processor)),
				n(source)).addSink(n("sink_mean_" + processor), n("mean_" + processor), n("proc_" + processor))
				.addSink(n("sink_var_" + processor), n("var_" + processor), n("proc_" + processor));
	}

	// builder.addSource(n("source"), topic)
	// .addProcessor(n("stats10u"),
	// () -> new StatsProcessor(topic, 10_000L, false, n("sink_mean10u"),
	// n("sink_var10u")),
	// n("source"))
	// .addProcessor(n("stats10"),
	// () -> new StatsProcessor(topic, 10_000L, true, n("sink_mean10"),
	// n("sink_var10")),
	// n("source"))
	// .addProcessor(n("stats60"),
	// () -> new StatsProcessor(topic, 60_000L, true, n("sink_mean60"),
	// n("sink_var60")),
	// n("source"))
	// .addProcessor(n("stats1h"),
	// () -> new StatsProcessor(topic, 3_600_000L, true, n("sink_mean1h"),
	// n("sink_var1h")),
	// n("stats10"))
	// // .addProcessor("variance", () -> new
	// // VarianceProcessor(topic,
	// // interval), "source")
	// // .addStateStore(sumStore, "stats",
	// // "variance").addStateStore(countStore, "stats",
	// // "variance")
	// .addSink(n("sink_mean10"), n("mean_10"), n("stats10"))
	// .addSink(n("sink_var10"), n("var_10"), n("stats10"))
	// .addSink(n("sink_mean60"), n("mean_60"), n("stats60"))
	// .addSink(n("sink_var60"), n("var_60"), n("stats60"))
	// .addSink(n("sink_mean1h"), n("mean_1h"), n("stats1h"))
	// .addSink(n("sink_var1h"), n("var_1h"), n("stats1h"))
	// .addSink(n("sink_mean10u"), n("mean_10u"), n("stats10u"))
	// .addSink(n("sink_var10u"), n("var_10u"), n("stats10u"));
	// }
}