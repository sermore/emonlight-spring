package net.reliqs.emonlight.xbeegw.send.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import net.reliqs.emonlight.xbeegw.config.Server;
import net.reliqs.emonlight.xbeegw.send.kafka.KafkaDeliveryService;
import net.reliqs.emonlight.xbeegw.send.rest.RestAsyncService;
import net.reliqs.emonlight.xbeegw.send.rest.RestDeliveryService;

@Configuration
public class DeliveryServiceFactory {
	private static final Logger log = LoggerFactory.getLogger(DeliveryServiceFactory.class);

	@Autowired
	private RestTemplateBuilder rb;

	@Autowired
	KafkaDeliveryService kafkaDeliveryService;

	@Bean
	@Scope("prototype")
	RestAsyncService restAsyncService() {
		return new RestAsyncService(rb);
	}

	@Bean
	@Scope("prototype")
	RestDeliveryService restDeliveryService() {
		RestDeliveryService s = new RestDeliveryService(restAsyncService());
		return s;
	}

	public DeliveryService getRestDeliveryServiceFromUrl(String url) {
		RestDeliveryService s = restDeliveryService();
		s.setUrl(url);
		return s;
	}

	public DeliveryService getSendService(Server server) {
		log.debug("setup delivery service for {}", server.getName());
		if (server.isKafkaEnabled()) {
			return kafkaDeliveryService;
		} else {
			return getRestDeliveryServiceFromUrl(server.getUrl());
		}
	}

}
