package net.reliqs.emonlight.xbeegw.publish;

import net.reliqs.emonlight.commons.config.Probe;
import net.reliqs.emonlight.commons.config.Probe.Type;

/**
 * Created by sergio on 25/02/17.
 */
public interface Subscriber {
    void receive(Probe p, Type type, Data d);
}
