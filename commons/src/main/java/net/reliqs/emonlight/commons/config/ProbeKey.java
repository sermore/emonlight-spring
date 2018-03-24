package net.reliqs.emonlight.commons.config;

import java.io.Serializable;

/**
 * Created by sergio on 25/02/17.
 */
public class ProbeKey implements Serializable {

    static final long serialVersionUID = 1L;

    final Probe.Type type;
    final byte port;

    public ProbeKey(Probe.Type type, byte port) {
        this.type = type;
        this.port = port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProbeKey)) return false;

        ProbeKey probeKey = (ProbeKey) o;

        if (port != probeKey.port) return false;
        return type == probeKey.type;
    }

    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + (int) port;
        return result;
    }
}
