package net.reliqs.emonlight.xbeegw.xbee.data;

import net.reliqs.emonlight.xbeegw.xbee.Data;

interface ProbeDataIn {

	Data add(Data d);

	boolean useFirstIn();
	
}