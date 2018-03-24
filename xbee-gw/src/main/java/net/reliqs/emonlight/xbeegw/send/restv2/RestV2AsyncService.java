package net.reliqs.emonlight.xbeegw.send.restv2;

import net.reliqs.emonlight.xbeegw.send.AbstractAsyncService;
import net.reliqs.emonlight.xbeegw.send.StoreData;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;

public class RestV2AsyncService extends AbstractAsyncService<StoreData> {

    private String url;
    private RestTemplate restTemplate;

    public RestV2AsyncService(RestTemplateBuilder rb, String url, int maxRetries) {
        super(maxRetries);
        this.restTemplate = rb.build();
        this.url = url;
    }

    @Override
    protected boolean send(StoreData t) {
        String res = restTemplate.postForObject(url, t, String.class);
        return "OK".equals(res);
    }
}
