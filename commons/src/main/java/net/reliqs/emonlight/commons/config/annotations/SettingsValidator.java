package net.reliqs.emonlight.commons.config.annotations;

import net.reliqs.emonlight.commons.config.Node;
import net.reliqs.emonlight.commons.config.Probe;
import net.reliqs.emonlight.commons.config.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SettingsValidator implements ConstraintValidator<ValidSettings, Settings>, Serializable {
    static final long serialVersionUID = 1L;

    private static final Logger log = LoggerFactory.getLogger(SettingsValidator.class);


    public void initialize(ValidSettings constraint) {
    }

    public boolean isValid(Settings settings, ConstraintValidatorContext context) {
        log.debug("validate settings {}", settings);
        context.disableDefaultConstraintViolation();
        boolean valid = true;

        List<Node> nodes = settings.getNodes();
        if (nodes != null && !nodes.isEmpty()) {
            List<Integer> ids = nodes.stream().map(n -> n.getId()).collect(Collectors.toList());
            if (ids.stream().distinct().count() != ids.size()) {
                context.buildConstraintViolationWithTemplate("node ids not unique").addConstraintViolation();
                valid = false;
            }
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

        Stream<Probe> probes = settings.getProbes();
        if (probes != null) {
            List<Integer> ids = probes.map(Probe::getId).collect(Collectors.toList());
            if (ids.stream().distinct().count() != ids.size()) {
                context.buildConstraintViolationWithTemplate("probe ids not unique").addConstraintViolation();
                valid = false;
            }
        }

        int max = Math.max(nodes.stream().filter(n -> n.getId() != null).mapToInt(Node::getId).max().orElse(0),
                settings.getProbes().filter(p -> p.getId() != null).mapToInt(Probe::getId).max().orElse(0));
        if (settings.getIdCnt() != null && settings.getIdCnt() <= max) {
            context.buildConstraintViolationWithTemplate(String.format("idCnt (%d) is lower or equal than the greater id in use (%d)", settings.getIdCnt(), max)).addConstraintViolation();
            valid = false;
        }

        return valid;
    }
}
