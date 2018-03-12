package net.reliqs.emonlight.commons.kafka.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

public class KafkaUtils {
    private static final Logger log = LoggerFactory.getLogger(KafkaUtils.class);

    @Value("${kafka.zookeeperHosts}")
    private String zookeeperHosts;

    @Value("${kafka.noOfPartitions}")
    private int noOfPartitions;

    @Value("${kafka.noOfReplication}")
    private int noOfReplication;

    private KafkaZkClient client;

    public void initTopics(final Iterable<String> topics) {
        for (String topic : topics) {
            if (!zk().topicExists(topic)) {
                log.info("create topic: {}", topic);
                zk().createTopic(topic, noOfPartitions, noOfReplication);
            } else {
                log.debug("topic exists: {}", topic);
            }
        }
        close();
    }

    private KafkaZkClient zk() {
        if (client == null)
            client = new KafkaZkClient(zookeeperHosts);
        return client;
    }

    private void close() {
        if (client != null) {
            zk().close();
            client = null;
        }
    }

}
