package net.reliqs.emonlight.xbeegw.xbee;

import net.reliqs.emonlight.commons.config.Probe;
import net.reliqs.emonlight.commons.config.Probe.Type;
import net.reliqs.emonlight.xbeegw.publish.Data;
import net.reliqs.emonlight.xbeegw.publish.Subscriber;

import java.util.ArrayList;
import java.util.List;

public final class TestSubscriber implements Subscriber {

    public final List<Probe> probes = new ArrayList<>();
    public final List<Type> types = new ArrayList<>();
    public final List<Data> data = new ArrayList<>();

    @Override
    public void receive(Probe p, Type type, Data d) {
        probes.add(p);
        types.add(type);
        data.add(d);
    }


    public void clear() {
        probes.clear();
        types.clear();
        data.clear();
    }
}