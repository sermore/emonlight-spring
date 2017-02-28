package net.reliqs.emonlight.xbeegw.publish;

import net.reliqs.emonlight.commons.xbee.Data;
import net.reliqs.emonlight.xbeegw.config.Probe;

/**
 * Created by sergio on 25/02/17.
 */
public interface Subscriber {
    void receive(Probe p, Data d);
}
