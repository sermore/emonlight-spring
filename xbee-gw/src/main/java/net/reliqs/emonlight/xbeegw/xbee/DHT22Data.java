package net.reliqs.emonlight.xbeegw.xbee;

import java.nio.ByteBuffer;

class DHT22Data {
	byte port;
	short h;
	short t;
	byte crc;

	DHT22Data(ByteBuffer in) {
		port = in.get();
		h = in.getShort();
		t = in.getShort();
		crc = in.get();
	}

	boolean check() {
		return (byte) (((byte) (h & 0xFF) + (byte) ((h >> 8) & 0xFF) + (byte) (t & 0xFF) + (byte) ((t >> 8) & 0xFF))
				& 0xFF) == crc;
	}

	double humidity() {
		return 0.0 + h / 10.0;
	}

	double temperature() {
		return 0.0 + ((t & 0x8000) == 0x8000 ? -(t & 0x7FF) : t) / 10.0;
	}
}