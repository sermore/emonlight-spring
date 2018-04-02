package net.reliqs.emonlight.commons.config.annotations;

import net.reliqs.emonlight.commons.config.Probe;
import net.reliqs.emonlight.commons.config.Probe.Type;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.io.Serializable;

public class ProbeValidator implements ConstraintValidator<ValidProbe, Probe>, Serializable {
    static final long serialVersionUID = 1L;

    @Override
    public void initialize(ValidProbe constraintAnnotation) {
    }

    @Override
    public boolean isValid(Probe probe, ConstraintValidatorContext context) {
        boolean ret = true;
        context.disableDefaultConstraintViolation();
        if (probe.hasThresholds() && probe.getType() != Type.PULSE) {
            context.buildConstraintViolationWithTemplate("thresholds can be used only with probe of type PULSE")
                    .addConstraintViolation();
            ret = false;
        }
        if (probe.getSoftThreshold() != null && probe.getSoftThreshold() > 0 &&
                (probe.getSoftThresholdTimeSec() == null || probe.getSoftThresholdTimeSec() == 0)) {
            context.buildConstraintViolationWithTemplate("soft threashold time not present").addPropertyNode("softThresholdTimeSec")
                    .addConstraintViolation();
            ret = false;
        }
        if (probe.getHardThreshold() != null && probe.getHardThreshold() > 0 &&
                (probe.getHardThresholdTimeSec() == null || probe.getHardThresholdTimeSec() == 0)) {
            context.buildConstraintViolationWithTemplate("hard threshold time not present").addPropertyNode("hardThresholdTimeSec")
                    .addConstraintViolation();
        }
        return ret;
    }

}
