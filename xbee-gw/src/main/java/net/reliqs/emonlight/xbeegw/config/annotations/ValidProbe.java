package net.reliqs.emonlight.xbeegw.config.annotations;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

@Retention(RUNTIME)
@Target(TYPE)
@Constraint(validatedBy=ProbeValidator.class)
public @interface ValidProbe {
    String message() default "Invalid probe";
    Class<?>[] groups() default { };
    Class<? extends Payload>[] payload() default { };
}
