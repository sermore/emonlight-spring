package net.reliqs.emonlight.xbeegw.monitoring;

import java.util.HashMap;
import java.util.Map;

import net.reliqs.emonlight.xbeegw.config.Probe;
import net.reliqs.emonlight.xbeegw.publish.Data;

public class TriggerDataAbsent extends Trigger {

    private Map<Probe, Long> lastMessages;
    private Map<Probe, Integer> levels;
    
    
    public TriggerDataAbsent() {
        lastMessages = new HashMap<>();
        levels = new HashMap<>();
    }

    long getMaxTimeBetweenMessages(Probe probe) {
        return probe.getRealSampleTime() * 5;
    }


    @Override
    void process(Probe probe, Data data) {
        long lm = lastMessages.getOrDefault(probe, 0L);
        int level = levels.getOrDefault(probe, 0);
        if (lm > 0L && data.t - lm > getMaxTimeBetweenMessages(probe)) {
            
        }
    }

}
