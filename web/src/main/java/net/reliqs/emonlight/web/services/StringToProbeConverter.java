package net.reliqs.emonlight.web.services;

import net.reliqs.emonlight.commons.config.Probe;
import net.reliqs.emonlight.commons.config.Settings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Service;

@Service
public class StringToProbeConverter implements Converter<String, Probe> {

    @Autowired
    private ApplicationContext ctx;

    @Override
    public Probe convert(String source) {
        try {
            Integer id = Integer.valueOf(source);
            Settings settings = ctx.getBean(Settings.class);
            return settings.findProbeById(id);
        } catch (NumberFormatException e) {
            return null;
        }
    }

}
