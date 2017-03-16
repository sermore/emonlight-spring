package net.reliqs.emonlight.xbeegw.config.annotations;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target(TYPE)
@Constraint(validatedBy = NodeValidator.class)
public @interface ValidNode {
    String message() default "Invalid node";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
