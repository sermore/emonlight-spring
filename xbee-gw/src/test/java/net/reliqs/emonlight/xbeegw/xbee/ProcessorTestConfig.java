package net.reliqs.emonlight.xbeegw.xbee;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class ProcessorTestConfig {

    @Bean
    @Primary
    XbeeGateway xbeeGateway() {
        return Mockito.mock(XbeeGateway.class);
    }

}