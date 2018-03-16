package net.reliqs.emonlight.xbeegw.notifications;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.reliqs.emonlight.commons.config.SettingsConfiguration;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutionException;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * Created by sergio on 01/04/17.
 */
@ActiveProfiles("test-router")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {SettingsConfiguration.class, NotificationAsyncService.class})
@EnableConfigurationProperties
@EnableAsync
public class NotificationAsyncServiceTest {

    @Autowired
    private NotificationAsyncService service;

    @Value("${notification.deviceId}")
    private String deviceId;

    @Value("${notification.serverKey}")
    private String serverKey;

    @Ignore
    @Test
    public void testKeys() {
        assertThat(serverKey.length(), is(152));
        assertThat(deviceId.length(), is(152));
    }

    @Ignore
    @Test
    public void testJSON() throws JsonProcessingException {
        NotificationJSON msg = NotificationJSON.create("title", "test message.", "X");
        assertThat(new ObjectMapper().writeValueAsString(msg), is("{\"notification\":{\"title\":\"title\",\"body\":\"test message.\",\"icon\":\"/images/profile_placeholder.png\",\"click_action\":\"http://localhost:5000\"},\"to\":\"X\"}"));
    }

    @Ignore
    @Test
    public void test() throws ExecutionException, InterruptedException {
        NotificationJSON msg = NotificationJSON.create("title", "test message.", deviceId);
        ListenableFuture<Boolean> res = service.send(msg);
        assertThat(res.get(), is(true));
    }
}