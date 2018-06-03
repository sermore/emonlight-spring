package net.reliqs.emonlight.xbeegw.xbee;

import com.digi.xbee.api.RemoteXBeeDevice;

public interface XbeeProcessor {
    RemoteXBeeDevice addDevice(String address);

    void sendDataAsync(RemoteXBeeDevice device, byte[] data);

    void resetLocalDevice();

    void resetRemoteDevice(RemoteXBeeDevice device);
}
