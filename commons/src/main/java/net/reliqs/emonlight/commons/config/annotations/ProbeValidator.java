package net.reliqs.emonlight.commons.config.annotations;

import net.reliqs.emonlight.commons.config.Probe;
import net.reliqs.emonlight.commons.config.Probe.Type;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class ProbeValidator implements ConstraintValidator<ValidProbe, Probe> {

    @Override
    public void initialize(ValidProbe constraintAnnotation) {
    }

    @Override
    public boolean isValid(Probe probe, ConstraintValidatorContext context) {
        boolean ret = true;
        context.disableDefaultConstraintViolation();
//        if (probe.getId() == 0) {
//            context.buildConstraintViolationWithTemplate("probe id is zero").addConstraintViolation();
//            ret = false;
//        }
        if (probe.hasThresholds() && probe.getType() != Type.PULSE) {
            context.buildConstraintViolationWithTemplate("thresholds can be used only with probe of type PULSE")
                    .addConstraintViolation();
            ret = false;
        }
        if (probe.getSoftThreshold() > 0 && probe.getSoftThresholdTimeSec() == 0) {
            context.buildConstraintViolationWithTemplate("soft threashold time not present").addConstraintViolation();
            ret = false;
        }
        if (probe.getHardThreshold() > 0 && probe.getHardThresholdTimeSec() == 0) {
            context.buildConstraintViolationWithTemplate("hard threshold time not present").addConstraintViolation();
        }
        return ret;
    }

}
