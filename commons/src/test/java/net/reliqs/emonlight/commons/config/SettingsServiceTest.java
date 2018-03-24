package net.reliqs.emonlight.commons.config;

import org.junit.AfterClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import javax.validation.ValidationException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {SettingsService.class, SettingsConfiguration.class})
@ActiveProfiles({"test-settings"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class SettingsServiceTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();
    @Autowired
    private SettingsService settingsService;

    @AfterClass
    public static void afterClass() throws IOException {
        Files.copy(Paths.get("src/test/resources/settings.yml.bak"), Paths.get("src/test/resources/settings.yml"),
                REPLACE_EXISTING);
    }

    @Test
    public void testDumpAndLoad() {
        Settings s = settingsService.load("src/test/resources/settings.yml.bak");
        s.setBaudRate(200);
        assertThat(settingsService.dump(s, "src/test/resources/settings.yml"), is(true));
        Settings s1 = settingsService.load("src/test/resources/settings.yml");
        assertThat(s1.getBaudRate(), is(200));
        assertEquals(2, s1.getNodes().size());

        assertThat(settingsService.load("not_existing"), is(nullValue()));
        assertThat(settingsService.dump(s, "src/test/resources/"), is(false));
    }

    @Test
    public void testForValidate() {
        Settings s = settingsService.load("src/test/resources/settings.yml.bak");
        thrown.expect(ValidationException.class);
        thrown.expectMessage("Settings invalid: nodes[0].id -> may not be null");
        s.getNodes().get(0).setId(null);
        settingsService.validate(s);
    }


}