package net.reliqs.emonlight.xbeegw.send;

import net.reliqs.emonlight.xbeegw.config.Settings;
import net.reliqs.emonlight.xbeegw.publish.Publisher;
import net.reliqs.emonlight.xbeegw.state.GlobalState;
import net.reliqs.emonlight.xbeegw.xbee.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by sergio on 08/03/17.
 */
@Configuration
public class SendConfiguration {
    private static final Logger log = LoggerFactory.getLogger(SendConfiguration.class);

    @Bean
    Dispatcher dispatcher(final Settings settings, Processor processor, GlobalState globalState, Publisher publisher) {
        return new Dispatcher(settings, processor, globalState, publisher);
    }

}
