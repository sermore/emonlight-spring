package net.reliqs.emonlight.xbeegw.notifications;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.client.RestTemplate;

/**
 * Created by sergio on 01/04/17.
 */
//@Service
public class NotificationAsyncService {
    private static final Logger log = LoggerFactory.getLogger(NotificationAsyncService.class);

    private final RestTemplate restTemplate;
    private String serverKey;
    private String url;

    @Autowired
    public NotificationAsyncService(@Value("${notification.serverKey}") String serverKey, @Value("${notification.url}") String url) {
        this.url = url;
        this.serverKey = serverKey;
        this.restTemplate = new RestTemplateBuilder().build();
    }

    @Async
    public ListenableFuture<Boolean> send(NotificationJSON msg) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(HttpHeaders.AUTHORIZATION, "key="+ serverKey);
        HttpEntity<NotificationJSON> entity = new HttpEntity<NotificationJSON>(msg, headers);
        String result = restTemplate.postForObject(url, entity, String.class);
        log.debug("NOTIFY {}", result);
        return new AsyncResult<>(true);
    }


}
