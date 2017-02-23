package net.reliqs.emonlight.xbeegw.config.annotations;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import net.reliqs.emonlight.xbeegw.config.Probe;
import net.reliqs.emonlight.xbeegw.config.Probe.Type;

public class ProbeValidator implements ConstraintValidator<ValidProbe, Probe> {

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
