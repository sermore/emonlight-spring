package net.reliqs.emonlight.commons.config.annotations;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target(TYPE)
@Constraint(validatedBy = ProbeValidator.class)
public @interface ValidProbe {
    String message() default "Invalid probe";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
