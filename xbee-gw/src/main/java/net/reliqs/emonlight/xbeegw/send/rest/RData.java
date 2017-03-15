package net.reliqs.emonlight.xbeegw.send.rest;

import net.reliqs.emonlight.xbeegw.config.Probe;
import net.reliqs.emonlight.xbeegw.config.Probe.Type;
import net.reliqs.emonlight.xbeegw.publish.Data;

/**
 * Created by sergio on 26/02/17.
 */
public class RData {
    final Probe probe;
    final Type type;
    final Data data;

    public RData(Probe probe, Type type, Data data) {
        this.probe = probe;
        this.type = type;
        this.data = data;
    }
}
