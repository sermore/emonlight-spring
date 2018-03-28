package net.reliqs.emonlight.commons.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;

import javax.validation.*;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SettingsService {
    private static final Logger log = LoggerFactory.getLogger(SettingsService.class);

    @Value("${settings.path:settings.yml}")
    private String path;

    void validate(Settings s) {
        log.debug("validate {}", s);
        if (s == null) {
            throw new ValidationException("Settings null");
        }
        ValidatorFactory vf = Validation.buildDefaultValidatorFactory();
        Validator validator = vf.getValidator();
        Set<ConstraintViolation<Settings>> res = validator.validate(s);
        if (!res.isEmpty()) {
            String msg = res.stream().map(c -> String.format("%s -> %s", c.getPropertyPath(), c.getMessage()))
                    .collect(Collectors.joining(", "));
            throw new ValidationException("Settings invalid: " + msg);
        }
    }

    Settings load(String pathName) {
        Path path = Paths.get(pathName);
        Yaml yaml = new Yaml();
        try (Reader reader = Files.newBufferedReader(path)) {
            Settings s = (Settings) yaml.load(reader);
            //            Map<String, Settings> map = new HashMap<>();
            //            map.put("settings", this);
            log.debug("loaded Settings from {}", path.toAbsolutePath());
            return s;
        } catch (IOException e) {
            log.error("Error reading settings file from " + path.toAbsolutePath(), e);
        }
        return null;
    }


    boolean dump(Settings settings, String pathName) {
        Path path = Paths.get(pathName);
        Yaml yaml = new Yaml();
        try (Writer writer = Files.newBufferedWriter(path)) {
            //            Map<String, Settings> map = new HashMap<>();
            //            map.put("settings", this);
            yaml.dump(settings, writer);
            return true;
        } catch (IOException e) {
            log.error("Error saving settings file to " + path.toAbsolutePath(), e);
        }
        return false;
    }

    public Settings load() {
        Settings s = load(path);
        validate(s);
        return s;
    }

    public Settings loadAndInitialize() {
        Settings s = load(path);
        s.init();
        return s;
    }

}
