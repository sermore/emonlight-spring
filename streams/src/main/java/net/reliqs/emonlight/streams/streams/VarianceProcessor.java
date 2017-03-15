package net.reliqs.emonlight.streams.streams;

import org.apache.kafka.streams.processor.Processor;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.state.KeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VarianceProcessor implements Processor<Long, Double> {
    private static final Logger log = LoggerFactory.getLogger(VarianceProcessor.class);

    private ProcessorContext context;
    private KeyValueStore<Long, Double> sum;
    private KeyValueStore<Long, Long> count;
    private long interval;
    private String topic;

    public VarianceProcessor(String topic, long interval) {
        this.topic = topic;
        this.interval = interval;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void init(ProcessorContext context) {
        this.context = context;
        this.context.schedule(interval);
        sum = (KeyValueStore<Long, Double>) context.getStateStore("sum_" + interval + "_" + topic);
        count = (KeyValueStore<Long, Long>) context.getStateStore("count_" + interval + "_" + topic);
    }

    @Override
    public void process(Long key, Double value) {
    }

    @Override
    public void punctuate(long timestamp) {
        Double s2 = sum.get(2L);
        Long n = count.get(1L);
        Long ts = count.get(2L);
        if (n != null && n > 1 && s2 != null) {
            context.forward(ts / n, s2 / (n - 1));
//			context.commit();
        }
        log.debug("var_{} n={}, s2={}", topic, n, s2);
    }

    @Override
    public void close() {
    }

}
