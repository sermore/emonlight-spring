package net.reliqs.emonlight.commons.config.annotations;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target(FIELD)
@Constraint(validatedBy = NodesValidator.class)
public @interface ValidNodes {
    String message() default "nodes invalid";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
