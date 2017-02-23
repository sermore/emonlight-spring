package net.reliqs.emonlight.web.utils;

import net.reliqs.emonlight.web.entities.Node;
import net.reliqs.emonlight.web.entities.Sample;
import net.reliqs.emonlight.web.services.JpaDataRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestComponent;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Created by sergio on 2/21/17.
 */
@TestComponent
public class DbUtils {

    @Autowired
    JpaDataRepo dataRepo;

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

    public List<Node> persistNodes() {
        List<Node> l = new ArrayList<>();
        for (Node n : generateNodes()) {
            l.add(dataRepo.saveNode(n));
        }
        return l;
    }

    public List<Sample> generateRandomData(Node node, Instant start, Instant end) {
        Random rng = new Random();
        long tstep = rng.nextInt(20);
        int tunit = 3 + rng.nextInt(5);
        double last = 0.0 + rng.nextInt(5000);
        double a1 = rng.nextGaussian() / 1000;
        double a2 = rng.nextGaussian() / 100;
//		double p1 = rng.nextGaussian();
//		double p2 = rng.nextGaussian();
        List<Sample> data = new ArrayList<>();
        Instant t = start;
        while (t.isBefore(end)) {
            Sample s = new Sample();
            s.setNode(node);
            s.setSampleTime(new Timestamp(t.toEpochMilli()));
            t = t.plus(tstep, ChronoUnit.values()[tunit]);
            a1 += rng.nextGaussian() / 10000;
            a2 += rng.nextGaussian() / 1000;
            s.setValue(last + last * a1 + last * a2);
            last = s.getValue();
            data.add(s);
        }
        return data;
    }

    public void saveSampleData(Node node, Instant start, Instant end) {
        List<Sample> l = generateRandomData(node, start, end);
        dataRepo.saveSamples(l);
    }

}
