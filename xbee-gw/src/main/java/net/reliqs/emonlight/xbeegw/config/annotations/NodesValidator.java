package net.reliqs.emonlight.xbeegw.config.annotations;

import net.reliqs.emonlight.xbeegw.config.Node;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.List;
import java.util.stream.Collectors;

public class NodesValidator implements ConstraintValidator<ValidNodes, List<Node>> {

    @Override
    public void initialize(ValidNodes constraintAnnotation) {
    }

    @Override
    public boolean isValid(List<Node> nodes, ConstraintValidatorContext context) {
        context.disableDefaultConstraintViolation();
        boolean valid = true;
        if (nodes != null && !nodes.isEmpty()) {
            List<String> names = nodes.stream().map(n -> n.getName()).collect(Collectors.toList());
            if (names.stream().distinct().count() != names.size()) {
                context.buildConstraintViolationWithTemplate("node names not unique").addConstraintViolation();
                valid = false;
            }
            if (nodes.stream().distinct().count() != nodes.size()) {
                context.buildConstraintViolationWithTemplate("node addresses not unique").addConstraintViolation();
                valid = false;
            }
            if (nodes.stream().allMatch(n -> n.getProbes().stream().distinct().count() != n.getProbes().size())) {
                context.buildConstraintViolationWithTemplate("probe names not unique").addConstraintViolation();
                valid = false;
            }
        } else {
            context.buildConstraintViolationWithTemplate("at least one node needs to be present")
                    .addConstraintViolation();
            valid = false;
        }
        return valid;
    }

}
