package net.reliqs.emonlight.commons.config.annotations;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target(TYPE)
@Constraint(validatedBy = SettingsValidator.class)
public @interface ValidSettings {
    String message() default "Invalid settings";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
