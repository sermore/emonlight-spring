package net.reliqs.emonlight.xbeegw.xbee;

import com.digi.xbee.api.models.XBeeMessage;
import com.digi.xbee.api.utils.HexUtils;

import java.io.Serializable;
import java.time.Instant;

public class DataMessage implements Serializable {
    private static final long serialVersionUID = 1L;
    final private Instant time;
    final private String address;
    final private byte[] data;

    DataMessage(XBeeMessage msg) {
        this.data = msg.getData();
        this.address = msg.getDevice().get64BitAddress().toString();
        this.time = Instant.now();
    }

    public DataMessage(Instant time, String address, byte[] data) {
        this.data = data;
        this.address = address;
        this.time = time;
    }

    String getDeviceAddress() {
        return address;
    }

    byte[] getData() {
        return data;
    }

    Instant getTime() {
        return time;
    }

    @Override
    public String toString() {
        return "DM [time=" + time + ", address=" + address + ", data=" + HexUtils.byteArrayToHexString(data) + "]";
    }

}
