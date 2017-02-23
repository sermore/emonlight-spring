package net.reliqs.emonlight.web.controllers;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import net.reliqs.emonlight.web.entities.Node;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.reliqs.emonlight.web.services.DataRepo;

import java.util.Arrays;
import java.util.List;

@RunWith(SpringRunner.class)
@WebMvcTest(ChartController.class)
public class ChartControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private DataRepo service;

	@Test
	public void dataShouldReturnData() throws Exception {
		Node n = new Node(); //(1L, "mean_10_kafka-pino_a7LiZVht-FNo3i8bUf61", 0, 0, true);
		when(service.findNode(1L)).thenReturn(n);
		List<Number[]> data = Arrays.asList(new Number[][] { { 1L, 100.2 }, { 2L, 105.3 }, { 3L, 110.4 } });
		when(service.getData(Arrays.asList(1L), 0)).thenReturn(data);
		String jsonContent = new ObjectMapper().writeValueAsString(data);
		this.mockMvc.perform(get("/data.json?id=1")).andDo(print()).andExpect(status().isOk()).andExpect(content().json(jsonContent));
	}

	@Test
	public void dataShouldReturnNothing() throws Exception {
		when(service.findNode(0L)).thenReturn(null);
		List<Number[]> data = Arrays.asList(new Number[][] {});
		when(service.getData(Arrays.asList(0L), 0)).thenReturn(data);
		String jsonContent = new ObjectMapper().writeValueAsString(data);
		this.mockMvc.perform(get("/data.json?id=0")).andDo(print()).andExpect(status().isOk()).andExpect(content().json(jsonContent));
	}

}
