package net.reliqs.emonlight.web;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import net.reliqs.emonlight.web.entities.Node;
import net.reliqs.emonlight.web.entities.NodeRepo;
import net.reliqs.emonlight.web.entities.Sample;
import net.reliqs.emonlight.web.entities.SampleRepo;

public class DataSimulator {
	private static final Logger log = LoggerFactory.getLogger(DataSimulator.class);

	@Autowired
	NodeRepo nodeRepo;

	@Autowired
	SampleRepo sampleRepo;

	static class DataGen {

		private double last;

		private Random rng;

		private double a1;

		private double a2;

		private Node node;

		private SampleRepo sampleRepo;

		public DataGen(SampleRepo sampleRepo, Node node) {
			this.sampleRepo = sampleRepo;
			this.node = node;
			rng = new Random();
			last = 0.0 + rng.nextInt(5000);
			a1 = rng.nextGaussian() / 1000;
			a2 = rng.nextGaussian() / 100;
		}

		@Transactional
		public void generateInstantRandomData() {
			// double p1 = rng.nextGaussian();
			// double p2 = rng.nextGaussian();
			Sample s = new Sample();
			s.setNode(node);
			s.setSampleTime(new Timestamp(Instant.now().toEpochMilli()));
			// t = t.plus(tstep, ChronoUnit.values()[tunit]);
			a1 += rng.nextGaussian() / 10000;
			a2 += rng.nextGaussian() / 1000;
			s.setValue(last + last * a1 + last * a2);
			last = s.getValue();
			sampleRepo.save(s);
			log.debug("DATA Node[{}] {}", node.getId(), s.getValue());
		}

	}

	private int nn;

	private long tstep;

	public DataSimulator(int nn, long tstep) {
		super();
		this.nn = nn;
		this.tstep = tstep;
	}

	Node getOrCreateNode(long id) {
		Node node = nodeRepo.findOne(id);
		if (node == null) {
			node = new Node();
			node.setTitle("Node " + id);
			node.setAuthenticationToken("XXN" + id);
			node.setTimeZone("Europe/Rome");
			node = nodeRepo.save(node);
		}
		return node;
	}

	public void run() {
		DataGen[] dataGen = new DataGen[nn];
		for (int i = 0; i < nn; i++) {
			Node n = getOrCreateNode(i + 1);
			dataGen[i] = new DataGen(sampleRepo, n);
		}
		while (true) {
			for (int i = 0; i < nn; i++) {
				dataGen[i].generateInstantRandomData();
			}
			try {
				Thread.sleep(tstep);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
