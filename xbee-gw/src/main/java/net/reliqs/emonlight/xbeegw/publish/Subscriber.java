package net.reliqs.emonlight.xbeegw.publish;

import net.reliqs.emonlight.commons.config.Probe;
import net.reliqs.emonlight.commons.config.Probe.Type;
import net.reliqs.emonlight.commons.data.Data;

/**
 * Created by sergio on 25/02/17.
 */
public interface Subscriber {
    void receive(Probe p, Type type, Data d);
}
