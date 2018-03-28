package net.reliqs.emonlight.web.controllers;

import net.reliqs.emonlight.web.data.StoreData;
import net.reliqs.emonlight.web.services.ProbeMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
public class DataController {
    private static final Logger log = LoggerFactory.getLogger(DataController.class);

    @Autowired
    private ProbeMonitor monitor;

    @PostMapping(value = "/data.json")
    @ResponseBody
    public ResponseEntity<String> receive(@RequestBody @Valid StoreData data, Errors errors) {
        if (data != null && !errors.hasErrors()) {
            if (monitor.add(data)) {
                return new ResponseEntity<>("OK", HttpStatus.OK);
            }
        }
        log.debug("data {}, errors {}", data, errors);
        //        return new ResponseEntity<>("OK", HttpStatus.OK);
        return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
    }
}
