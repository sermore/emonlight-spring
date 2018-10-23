package net.reliqs.emonlight.xbeegw.send.restv2;

import net.reliqs.emonlight.commons.data.StoreData;
import net.reliqs.emonlight.xbeegw.send.AbstractAsyncService;
import org.springframework.web.client.RestTemplate;

public class RestV2AsyncService extends AbstractAsyncService<StoreData> {

    private String url;
    private RestTemplate restTemplate;

    public RestV2AsyncService(String logId, int maxRetries, boolean ignoreErrors, RestTemplate restTemplate,
            String url) {
        super(logId, maxRetries, ignoreErrors);
        this.restTemplate = restTemplate;
        this.url = url;
    }

    @Override
    protected boolean send(StoreData t) {
        log.trace("{}: post {}", logId, url);
        String res = restTemplate.postForObject(url, t, String.class);
        log.trace("{}: result {}", logId, res);
        return "OK".equals(res);
    }

    public String getUrl() {
        return url;
    }

    RestTemplate getRestTemplate() {
        return restTemplate;
    }

}
