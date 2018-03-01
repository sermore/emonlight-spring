package net.reliqs.emonlight.xbeegw.xbee;

import com.digi.xbee.api.RemoteXBeeDevice;
import com.digi.xbee.api.XBeeDevice;
import com.digi.xbee.api.XBeeNetwork;
import com.digi.xbee.api.exceptions.TimeoutException;
import com.digi.xbee.api.exceptions.XBeeException;
import com.digi.xbee.api.io.IOSample;
import com.digi.xbee.api.listeners.IDataReceiveListener;
import com.digi.xbee.api.listeners.IIOSampleReceiveListener;
import com.digi.xbee.api.models.XBee64BitAddress;
import com.digi.xbee.api.models.XBeeMessage;
import com.digi.xbee.api.utils.ByteUtils;
import net.reliqs.emonlight.xbeegw.GwException;
import net.reliqs.emonlight.xbeegw.config.Settings;
import net.reliqs.emonlight.xbeegw.events.EventQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;

@Service
class XbeeGateway implements XbeeProcessor, IDataReceiveListener, IIOSampleReceiveListener {
    private static final Logger log = LoggerFactory.getLogger(XbeeGateway.class);

    private XBeeDevice localDevice;
    private EventQueue queue;

    public XbeeGateway(Settings settings, EventQueue queue) throws XBeeException {
        this.queue = queue;
        init(settings);
    }

    private void init(Settings settings) throws XBeeException {
        localDevice = new XBeeDevice(settings.getSerialPort(), settings.getBaudRate());
        localDevice.setReceiveTimeout(settings.getReceiveTimeout());
        localDevice.open();
        localDevice.addDataListener(this);
        localDevice.addIOSampleListener(this);
        int sampleTime = settings.findMaxSampleTime();
        assert sampleTime > 0;
        setSleepParameters(sampleTime);
    }

    private void setSleepParameters(int sampleTime) throws TimeoutException, XBeeException {
        byte so = (byte) (sampleTime > 28000 ? 6 : 0);
        short sp = (short) (sampleTime > 28000 ? 1000 : sampleTime / 10);
        // 10 seconds (sampleTime / sn / 10);
        short sn = (short) (sampleTime > 28000 ? 1 + sampleTime * 3 / sp / 10 : 1);
        short st = 1;
        log.debug("sampleTime: {} => set sleep parameters SN={}, SO={}, SP={}, ST={}", sampleTime, sn, so, sp, st);
        localDevice.setParameter("SN", ByteUtils.shortToByteArray(sn));
//		localDevice.setParameter("SO", new byte[] { so });
        localDevice.setParameter("SP", ByteUtils.shortToByteArray(sp));
        localDevice.setParameter("ST", ByteUtils.shortToByteArray(st));
    }

    @PreDestroy
    void cleanup() {
        localDevice.close();
        log.debug("close");
    }

    @Override
    public RemoteXBeeDevice addDevice(String address) {
        XBeeNetwork network = localDevice.getNetwork();
        XBee64BitAddress addr = new XBee64BitAddress(address);
        RemoteXBeeDevice device = network.getDevice(addr);
        if (device == null) {
            device = new RemoteXBeeDevice(localDevice, addr);
            log.debug("created remote device {}", device);
        }
        return device;
    }

    @Override
    public void sendDataAsync(RemoteXBeeDevice device, byte[] data) {
        // log.debug("send {} to device {}",
        // HexUtils.byteArrayToHexString(data), device);
        try {
            localDevice.sendDataAsync(device, data);
        } catch (XBeeException e) {
            throw new GwException(e);
        }
    }

    @Override
    public void ioSampleReceived(RemoteXBeeDevice remoteDevice, IOSample ioSample) {
        log.warn("ioSample {} from {} discarded", ioSample, remoteDevice);
    }

    @Override
    public void dataReceived(XBeeMessage msg) {
        queue.offerDataMessage(new DataMessage(msg));
    }

}
