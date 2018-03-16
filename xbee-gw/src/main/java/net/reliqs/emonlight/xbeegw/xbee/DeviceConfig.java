package net.reliqs.emonlight.xbeegw.xbee;

import net.reliqs.emonlight.commons.config.Node;
import net.reliqs.emonlight.commons.config.Node.OpMode;
import net.reliqs.emonlight.commons.config.Probe;
import net.reliqs.emonlight.commons.config.Probe.Type;

import java.nio.ByteBuffer;

class DeviceConfig {
    final String name;
    final OpMode mode;
    final int dataSampleTime;
    final int dht22SampleTime;
    final int vccSampleTime;
    final boolean vccFromADC;

    DeviceConfig(ByteBuffer b) {
        super();
        StringBuilder nameBuf = new StringBuilder();
        char c = (char) b.get();
        while (c != 0) {
            nameBuf.append(c);
            c = (char) b.get();
        }
        name = nameBuf.toString();
        byte m = b.get();
        mode = m >= 0 && m < OpMode.values().length ? OpMode.values()[m] : OpMode.UNCONFIGURED;
        dataSampleTime = b.getInt();
        dht22SampleTime = b.getInt();
        vccSampleTime = b.getInt();
        vccFromADC = b.get() == 1;
    }

    DeviceConfig(final NodeState ns) {
        Node n = ns.getNode();
        name = n.getName();
        mode = n.getMode();
        dataSampleTime = n.getSampleTime();
        Probe p = n.findProbeByType(Type.DHT22_T);
        if (p != null && p.getSampleTime() == 0) {
            p = n.findProbeByType(Type.DHT22_H);
        }
        dht22SampleTime = p != null && p.getSampleTime() > 0 ? p.getSampleTime() : dataSampleTime;
        p = n.findProbeByType(Type.VCC);
        vccSampleTime = p != null && p.getSampleTime() > 0 ? p.getSampleTime() : dataSampleTime * 3;
        vccFromADC = n.isVccFromADC();
    }

    byte[] buildResponse() {
        ByteBuffer bb = ByteBuffer.allocate(37);
        bb.put((byte) 'S');
        bb.put((byte) 'C');
        name.chars().forEach(c -> bb.put((byte) c));
        bb.put((byte) 0);
        bb.put((byte) mode.ordinal());
        bb.putInt(dataSampleTime);
        bb.putInt(dht22SampleTime);
        bb.putInt(vccSampleTime);
        bb.put((byte) (vccFromADC ? 1 : 0));
        return bb.array();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + dataSampleTime;
        result = prime * result + dht22SampleTime;
        result = prime * result + ((mode == null) ? 0 : mode.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + (vccFromADC ? 1231 : 1237);
        result = prime * result + vccSampleTime;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DeviceConfig other = (DeviceConfig) obj;
        if (dataSampleTime != other.dataSampleTime)
            return false;
        if (dht22SampleTime != other.dht22SampleTime)
            return false;
        if (mode != other.mode)
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (vccFromADC != other.vccFromADC)
            return false;
        if (vccSampleTime != other.vccSampleTime)
            return false;
        return true;
    }

}