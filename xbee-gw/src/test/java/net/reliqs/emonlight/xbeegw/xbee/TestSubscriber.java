package net.reliqs.emonlight.xbeegw.xbee;

import java.util.ArrayList;
import java.util.List;

import net.reliqs.emonlight.xbeegw.config.Probe;
import net.reliqs.emonlight.xbeegw.config.Probe.Type;
import net.reliqs.emonlight.xbeegw.publish.Data;
import net.reliqs.emonlight.xbeegw.publish.Subscriber;

final class TestSubscriber implements Subscriber {
    List<Probe> probes = new ArrayList<>();
    List<Type> types = new ArrayList<>();
    List<Data> data = new ArrayList<>();

    @Override
    public void receive(Probe p, Type type, Data d) {
        probes.add(p);
        types.add(type);
        data.add(d);
    }
}