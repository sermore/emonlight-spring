package net.reliqs.emonlight.commons.kafka.utils;

import kafka.admin.AdminUtils;
import kafka.admin.RackAwareMode;
import kafka.utils.ZKStringSerializer$;
import kafka.utils.ZkUtils;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.ZkConnection;

import javax.annotation.PreDestroy;
import java.util.Properties;

public class KafkaZkClient {

    private ZkUtils zkUtils;

    public KafkaZkClient(String zookeeperHosts) {
        super();
        int sessionTimeOutInMs = 15 * 1000; // 15 secs
        int connectionTimeOutInMs = 10 * 1000; // 10 secs

        ZkClient zkClient = new ZkClient(zookeeperHosts, sessionTimeOutInMs, connectionTimeOutInMs,
                ZKStringSerializer$.MODULE$);
        zkUtils = new ZkUtils(zkClient, new ZkConnection(zookeeperHosts), false);
    }

    public void createTopic(String topicName, int noOfPartitions, int noOfReplication) {
        Properties topicConfiguration = AdminUtils.createTopic$default$5();
        RackAwareMode rackAwareMode = AdminUtils.createTopic$default$6();

        AdminUtils.createTopic(zkUtils, topicName, noOfPartitions, noOfReplication, topicConfiguration, rackAwareMode);
    }

    public boolean topicExists(String topicName) {
        return AdminUtils.topicExists(zkUtils, topicName);
    }

    public void deleteTopic(String topicName) {
        AdminUtils.deleteTopic(zkUtils, topicName);
    }

    @PreDestroy
    public void close() {
        zkUtils.close();
    }

}
