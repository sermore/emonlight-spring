package net.reliqs.emonlight.xbeegw.xbee;

import com.digi.xbee.api.models.XBeeMessage;

import java.time.Instant;
import java.util.Arrays;

class DataMessage {
    final private Instant time;
    final private String address;
    final private byte[] data;

    DataMessage(XBeeMessage msg) {
        this.data = msg.getData();
        this.address = msg.getDevice().get64BitAddress().toString();
        this.time = Instant.now();
    }

    DataMessage(Instant time, String address, byte[] data) {
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
        return "DataMessage [time=" + time + ", address=" + address + ", data=" + Arrays.toString(data) + "]";
    }

}
