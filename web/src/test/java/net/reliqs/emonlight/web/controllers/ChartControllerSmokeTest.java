package net.reliqs.emonlight.web.controllers;

import static org.assertj.core.api.Assertions.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import net.reliqs.emonlight.web.controllers.ChartController;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ChartControllerSmokeTest {

    @Autowired
    private ChartController controller;

    @Test
    public void contexLoads() throws Exception {
        assertThat(controller).isNotNull();
    }
    
}
