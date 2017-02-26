package net.reliqs.emonlight.xbeegw.send.services;

import net.reliqs.emonlight.xbeegw.config.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import net.reliqs.emonlight.xbeegw.config.Server;
import net.reliqs.emonlight.xbeegw.send.rest.RestAsyncService;
import net.reliqs.emonlight.xbeegw.send.rest.RestDeliveryService;

@Configuration
public class DeliveryServiceFactory {
	private static final Logger log = LoggerFactory.getLogger(DeliveryServiceFactory.class);

	@Autowired
	private RestTemplateBuilder rb;

	@Autowired
	Settings settings;

	@Bean
	@Scope("prototype")
	RestAsyncService restAsyncService() {
		return new RestAsyncService(rb);
	}

	@Bean
	@Scope("prototype")
	RestDeliveryService restDeliveryService() {
		RestDeliveryService s = new RestDeliveryService(settings, restAsyncService());
		return s;
	}

	public DeliveryService getRestDeliveryServiceFromUrl(String url) {
		RestDeliveryService s = restDeliveryService();
		s.setUrl(url);
		return s;
	}

	public DeliveryService getSendService(Server server) {
		log.debug("setup delivery service for {}", server.getName());
		return getRestDeliveryServiceFromUrl(server.getUrl());
	}

}
