package net.reliqs.emonlight.web.controllers;

import net.reliqs.emonlight.commons.config.Settings;
import net.reliqs.emonlight.web.data.DataQueue;
import net.reliqs.emonlight.web.data.StoreData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZoneOffset;

@RestController
public class DataController {

    @Autowired
    private DataQueue queue;
    @Autowired
    private Settings settings;
    @Autowired
    private ZoneOffset zoneOffset;

    @RequestMapping("/receive")
    @ResponseBody
    public ResponseEntity<String> receive(StoreData data) {
        if (data != null) {
            if (queue.add(settings, zoneOffset, data)) {
                return new ResponseEntity<>("OK", HttpStatus.OK);
            }
        }
        return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
    }
}
