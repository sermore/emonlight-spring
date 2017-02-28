package net.reliqs.emonlight.commons.xbee;

public class Data {

	public final long t;
	public final double v;

	public Data(long t, double v) {
		super();
		this.t = t;
		this.v = v;
	}

	@Override
	public String toString() {
		return String.format("D [t=%s, v=%s]", t, v);
	}
	
}
