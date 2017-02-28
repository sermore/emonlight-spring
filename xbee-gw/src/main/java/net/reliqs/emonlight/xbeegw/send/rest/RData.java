package net.reliqs.emonlight.xbeegw.send.rest;

import net.reliqs.emonlight.commons.xbee.Data;
import net.reliqs.emonlight.xbeegw.config.Probe;

/**
 * Created by sergio on 26/02/17.
 */
public class RData {
    final Probe probe;
    final Data data;

    public RData(Probe probe, Data data) {
        this.probe = probe;
        this.data = data;
    }
}
