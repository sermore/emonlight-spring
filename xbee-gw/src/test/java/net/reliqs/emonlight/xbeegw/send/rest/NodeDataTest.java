package net.reliqs.emonlight.xbeegw.send.rest;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import org.junit.Test;

import net.reliqs.emonlight.xbeegw.send.rest.NodeDataJSON;
import net.reliqs.emonlight.xbeegw.xbee.Data;

public class NodeDataTest {

	@Test
	public void test() {
		Data in = new Data(0, 199);
		NodeDataJSON nd1 = new NodeDataJSON(1, "XXX");
		nd1.addData(in);
		in = new Data(0, 199);
		NodeDataJSON nd2 = new NodeDataJSON(1, "XXX");
		nd2.addData(in);
//		assertThat(nd1.getId(), is(nd2.getId()));
//		assertThat(nd1.getK(), is(nd2.getK()));
//		assertThat(nd1.getD(), is(nd2.getD()));
		assertThat(nd1, is(nd2));
		
	}

}
