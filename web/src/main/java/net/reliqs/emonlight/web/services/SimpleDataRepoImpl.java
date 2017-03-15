package net.reliqs.emonlight.web.services;

import net.reliqs.emonlight.web.entities.Node;
import net.reliqs.emonlight.web.entities.Sample;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

//@Service
public class SimpleDataRepoImpl implements DataRepo {

    Map<Long, Node> nodes;
    Map<String, Node> nodesByTopic;

    SimpleDataRepoImpl() {
        super();
        nodes = new HashMap<>();
        nodesByTopic = new HashMap<>();
//		addTopic("mean_10_kafka-pino_a7LiZVht-FNo3i8bUf61");
//		addTopic("kafka-pino_a7LiZVht-FNo3i8bUf61");
    }

//	private void addTopic(String topic) {
//		long id = nodes.size() + 1;
//		Node n = new Node(id, topic, 0, 0, false);
//		nodes.put(id, n);
//		nodesByTopic.put(topic, n);
//	}

    @Override
    public Node findNode(long id) {
        return nodes.get(id);
    }

    @Override
    public Node findNodeByAuthenticationToken(String token) {
        return null;
    }

    @Override
    public Iterable<Number[]> getData(Iterable<Long> nodeIds, long timeStart) {
        Node n = findNode(nodeIds.iterator().next());
        if (n != null) {
            Iterable<Number[]> data = generateRandomData(timeStart, 3);
            return data;
        }
        return new ArrayList<Number[]>();
    }

    @Override
    public List<Sample> getSamples(Long nodeId, Timestamp from) {
        return null;
    }

    @Override
    public Node saveNode(Node node) {
        return null;
    }

    @Override
    public List<Sample> saveSamples(Iterable<Sample> samples) {
        return null;
    }

    private List<Number[]> generateRandomData(long timeStart, int nd) {
        Instant now = timeStart == 0 ? Instant.now() : Instant.ofEpochMilli(timeStart);
        Random rng = new Random();
        int nn = rng.nextInt(1000) + 100;
        Number[][] data = new Number[nn * nd][3];
        long tstep = rng.nextInt(20);
        int tunit = 3 + rng.nextInt(5);
        double last = 0.0 + rng.nextInt(5000);
        double a1 = rng.nextGaussian() / 1000;
        double a2 = rng.nextGaussian() / 100;
//		double p1 = rng.nextGaussian();
//		double p2 = rng.nextGaussian();
        int k = 0;
        for (int i = 0; i < nn; i++) {
            data[i][0] = now.toEpochMilli();
            now = now.plus(tstep, ChronoUnit.values()[tunit]);
            a1 += rng.nextGaussian() / 10000;
            a2 += rng.nextGaussian() / 1000;
            data[i][1] = last + last * a1 + last * a2;
            data[i][2] = 0;
            for (int j = 1; j < nd; j++) {
                data[i + nn * j][0] = now.toEpochMilli();
                data[i + nn * j][1] = last + (last * a1 + last * a2) * j * rng.nextGaussian();
                data[i + nn * j][2] = j;
            }
            last = (double) data[i][1];
        }
        return Arrays.asList(data);
    }

//	@Override
//	public Node findNodeByTopic(String topic) {
//		return nodesByTopic.get(topic);
//	}
}
