package net.reliqs.emonlight.web.config;

import net.reliqs.emonlight.commons.config.ISettings;
import net.reliqs.emonlight.commons.config.Settings;
import net.reliqs.emonlight.commons.config.SettingsService;
import net.reliqs.emonlight.web.git.FileRepositoryImpl;
import net.reliqs.emonlight.web.services.DataRepo;
import net.reliqs.emonlight.web.services.FileRepository;
import net.reliqs.emonlight.web.services.ProbeMonitor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.annotation.SessionScope;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.thymeleaf.spring5.ISpringTemplateEngine;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.spring5.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ITemplateResolver;


@Configuration
public class WebConfig implements WebMvcConfigurer, ApplicationContextAware {

    private static final String UTF8 = "UTF-8";

    @Autowired
    private SettingsService settingsService;
    @Autowired
    private ApplicationContext applicationContext;

    //    @Bean
    //    public SettingsService settingsservice() {
    //        return new SettingsService();
    //    }

    @Bean
    public ProbeMonitor monitor(DataRepo dataRepo, @Value("${historyDays:20}") int historyDays) {
        Settings s = settingsService.loadAndInitialize();
        return new ProbeMonitor(s, dataRepo, historyDays);
    }

    @Bean
    @SessionScope
    public ISettings settings() {
        Settings s = settingsService.load();
        return s;
    }

    @Bean(initMethod = "initRepo")
    public FileRepository fileRepository(@Value("${settings.path:settings.yml}") String path) {
        return new FileRepositoryImpl(path);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Bean
    public ViewResolver htmlViewResolver() {
        ThymeleafViewResolver resolver = new ThymeleafViewResolver();
        resolver.setTemplateEngine(templateEngine(htmlTemplateResolver()));
        resolver.setContentType("text/html");
        resolver.setCharacterEncoding("UTF-8");
        resolver.setViewNames(new String[]{"*"});
        resolver.setOrder(5);
        return resolver;
    }

    @Bean
    public ViewResolver javascriptViewResolver() {
        ThymeleafViewResolver resolver = new ThymeleafViewResolver();
        resolver.setTemplateEngine(templateEngine(javascriptTemplateResolver()));
        resolver.setContentType("application/javascript");
        resolver.setCharacterEncoding("UTF-8");
        resolver.setViewNames(new String[]{"*.js"});
        return resolver;
    }

    @Bean
    public ViewResolver cssViewResolver() {
        ThymeleafViewResolver resolver = new ThymeleafViewResolver();
        resolver.setTemplateEngine(templateEngine(cssTemplateResolver()));
        resolver.setContentType("text/css");
        resolver.setCharacterEncoding(UTF8);
        resolver.setViewNames(new String[]{"*.css"});
        return resolver;
    }

    private ITemplateResolver htmlTemplateResolver() {
        SpringResourceTemplateResolver resolver = new SpringResourceTemplateResolver();
        resolver.setApplicationContext(applicationContext);
        resolver.setPrefix("classpath:/templates/");
        resolver.setSuffix(".html");
        resolver.setCacheable(false);
        resolver.setTemplateMode(TemplateMode.HTML);
        return resolver;
    }

    private ITemplateResolver javascriptTemplateResolver() {
        SpringResourceTemplateResolver resolver = new SpringResourceTemplateResolver();
        resolver.setApplicationContext(applicationContext);
        resolver.setPrefix("classpath:/static/js/");
        resolver.setCacheable(false);
        resolver.setTemplateMode(TemplateMode.JAVASCRIPT);
        return resolver;
    }

    private ITemplateResolver cssTemplateResolver() {
        SpringResourceTemplateResolver resolver = new SpringResourceTemplateResolver();
        resolver.setApplicationContext(applicationContext);
        resolver.setPrefix("classpath:/static/css/");
        resolver.setTemplateMode(TemplateMode.CSS);
        return resolver;
    }

    private ISpringTemplateEngine templateEngine(ITemplateResolver templateResolver) {
        SpringTemplateEngine engine = new SpringTemplateEngine();
        engine.setTemplateResolver(templateResolver);
        return engine;
    }
    //    @Bean
    //    public CommonsRequestLoggingFilter requestLoggingFilter() {
    //        CommonsRequestLoggingFilter loggingFilter = new CommonsRequestLoggingFilter();
    //        loggingFilter.setIncludeClientInfo(true);
    //        loggingFilter.setIncludeQueryString(true);
    //        loggingFilter.setIncludePayload(true);
    //        return loggingFilter;
    //    }

    //    @Bean
    //    public DataQueue dataQueue() {
    //        return new DataQueue();
    //    }

    //    @Bean
    //    @SessionScope
    //    public ZoneOffset clientOffset() {
    //        return serverZoneId().getRules().getof .from(serverZoneId());
    //    }

}
