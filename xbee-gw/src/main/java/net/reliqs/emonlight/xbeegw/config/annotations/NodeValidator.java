package net.reliqs.emonlight.xbeegw.config.annotations;

import java.util.List;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import net.reliqs.emonlight.xbeegw.config.Node;
import net.reliqs.emonlight.xbeegw.config.Probe;
import net.reliqs.emonlight.xbeegw.config.Probe.Type;

public class NodeValidator implements ConstraintValidator<ValidNode, Node> {

	@Override
	public void initialize(ValidNode constraintAnnotation) {
	}

	@Override
	public boolean isValid(Node value, ConstraintValidatorContext context) {
		boolean ret = true;
		context.disableDefaultConstraintViolation();
		List<Probe> lp = value.getProbes();
		if (value.getVccThreshold() > 0) {
			if (lp == null || lp.size() < 1 || lp.stream().filter(p -> p.getType() == Type.VCC).count() != 1) {
				context.buildConstraintViolationWithTemplate("monitoring Vcc threshold requires a probe of type VCC")
						.addConstraintViolation();
				ret = false;
			}
		}
		switch (value.getMode()) {
		case PULSE:
			if (lp == null || lp.stream().filter(p -> p.getType() == Type.PULSE).count() != 1) {
				context.buildConstraintViolationWithTemplate("mode PULSE requires 1 probe of type PULSE")
						.addConstraintViolation();
				ret = false;
			}
			break;
		case PULSE_DHT22:
			if (lp == null || lp.size() < 3 || lp.stream().filter(p -> p.getType() == Type.PULSE).count() != 1
					|| lp.stream().filter(p -> p.getType() == Type.DHT22_H).count() != 1
					|| lp.stream().filter(p -> p.getType() == Type.DHT22_T).count() != 1) {
				context.buildConstraintViolationWithTemplate(
						"mode PULSE_DHT22 requires at least 3 probes, one PULSE and two for DHT22")
						.addConstraintViolation();
				ret = false;
			}
			break;
		case DHT22:
			if (lp == null || lp.size() < 2 || lp.stream().filter(p -> p.getType() == Type.DHT22_H).count() != 1
					|| lp.stream().filter(p -> p.getType() == Type.DHT22_T).count() != 1) {
				context.buildConstraintViolationWithTemplate(
						"mode DHT22 requires 2 probes, one of type DHT22_H and one DHT22_T").addConstraintViolation();
				ret = false;
			}
			break;
		default:
			context.buildConstraintViolationWithTemplate("mode not correct").addConstraintViolation();
			ret = false;
		}
		return ret;
	}

}
