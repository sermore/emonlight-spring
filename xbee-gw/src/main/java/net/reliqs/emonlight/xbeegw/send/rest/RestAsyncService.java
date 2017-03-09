package net.reliqs.emonlight.xbeegw.send.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.client.RestTemplate;

import net.reliqs.emonlight.xbeegw.GwException;

public class RestAsyncService {
	private static final Logger log = LoggerFactory.getLogger(RestAsyncService.class);

	private RestTemplate restTemplate;

	public RestAsyncService(RestTemplateBuilder rb) {
		super();
		this.restTemplate = rb.build();
	}

	@Async
	public ListenableFuture<Boolean> post(String url, ServerDataJSON sd) {
		String res;
//		boolean ok;
//		 log.debug("REST {} -> {}",sd, url);
//		try {
			res = restTemplate.postForObject(url, sd, String.class);
			if (!"OK".equals(res))
				throw new GwException("unexpected return value: " + res);
			log.trace("REST {} <- {}", res, url);
//		} catch (RestClientException e) {
//			log.warn("REST FAIL: {}", url, e.getMessage());
//			ok = false;
//		}
		return new AsyncResult<>(true);
	}

}
