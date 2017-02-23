package net.reliqs.emonlight.xbeegw.xbee.data;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Queue;

import net.reliqs.emonlight.xbeegw.xbee.Data;

class ProbeDataWithQueueing implements ProbeDataIn, ProbeDataOut {
	// private final Probe probe;
	private final Queue<Data> data = new ArrayDeque<>();

	@Override
	public Data add(Data in) {
		synchronized (data) {
			data.add(in);
		}
		return in;
	}

	@Override
	public void clear() {
		synchronized (data) {
			data.clear();
		}
	}

	@Override
	public boolean useFirstIn() {
		return true;
	}

	@Override
	public int queueLength() {
		return data.size();
	}

	@Override
	public Data peek() {
		return data.peek();
	}

	@Override
	public Data poll() {
		return data.poll();
	}

	@Override
	public Iterator<Data> getIterator() {
		return data.iterator();
	}

	@Override
	public boolean isEmpty() {
		return data.isEmpty();
	}

}
