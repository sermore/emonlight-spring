package net.reliqs.emonlight.web.utils;

import static java.lang.Math.PI;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestComponent;

import net.reliqs.emonlight.web.entities.Node;
import net.reliqs.emonlight.web.entities.NodeRepo;
import net.reliqs.emonlight.web.entities.Sample;
import net.reliqs.emonlight.web.entities.SampleRepo;

/**
 * Created by sergio on 2/21/17.
 */
@TestComponent
public class DataGeneration {

    @Autowired
    SampleRepo sampleRepo;

    @Autowired
    NodeRepo nodeRepo;

    public List<Node> generateNodes() {
        List<Node> l = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            Node n = new Node();
            n.setTitle("Node " + i);
            n.setAuthenticationToken("XXN" + i);
            n.setTimeZone("GMT+" + i);
            l.add(n);
        }
        return l;
    }

    public void generateConstantData(Node n, Instant start, Instant end, TemporalAmount tstep, Double value) {
        Instant t = start;
        while (t.isBefore(end)) {
            Sample s = new Sample();
            s.setNode(n);
            s.setSampleTime(new Timestamp(t.toEpochMilli()));
            s.setValue(value);
            t = t.plus(tstep);
            sampleRepo.save(s);
        }
    }

    public void generatePeriodicData(Node n, Instant start, Instant end, TemporalAmount tstep, Double maxValue, Duration period) {
        Instant t = start;
        while (t.isBefore(end)) {
            Sample s = new Sample();
            s.setNode(n);
            s.setSampleTime(new Timestamp(t.toEpochMilli()));
            double v = maxValue / 2 * (1 + Math.sin(2 * PI * (t.toEpochMilli() - start.toEpochMilli()) / period.toMillis()));
            s.setValue(v);
            t = t.plus(tstep);
            sampleRepo.save(s);
        }
    }

    public void generateRandomData(Node node, Instant start, Instant end, TemporalAmount tstep) {
        Random rng = new Random();
//        long tstep = rng.nextInt(20);
//        int tunit = 3 + rng.nextInt(5);
        double last = 0.0 + rng.nextInt(5000);
        double a1 = rng.nextGaussian() / 1000;
        double a2 = rng.nextGaussian() / 100;
//		double p1 = rng.nextGaussian();
//		double p2 = rng.nextGaussian();
        Instant t = start;
        while (t.isBefore(end)) {
            Sample s = new Sample();
            s.setNode(node);
            s.setSampleTime(new Timestamp(t.toEpochMilli()));
//            t = t.plus(tstep, ChronoUnit.values()[tunit]);
            t = t.plus(tstep);
            a1 += rng.nextGaussian() / 10000;
            a2 += rng.nextGaussian() / 1000;
            s.setValue(last + last * a1 + last * a2);
            last = s.getValue();
            sampleRepo.save(s);
        }
    }
        
}
