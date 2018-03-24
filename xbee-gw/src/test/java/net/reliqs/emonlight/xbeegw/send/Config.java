package net.reliqs.emonlight.xbeegw.send;

import net.reliqs.emonlight.xbeegw.publish.Publisher;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
class Config {

    /**
     * It tests the state after the termination of the service.
     * This afterTestRunner is destroyed after the dispatcher, thanks to the dependsOn annotation.
     *
     * @return afterTestRunner
     */
    //    @Bean(initMethod = "onInit", destroyMethod = "onClose")
    //    AfterTestRunner afterTestRunner() {
    //        return new AfterTestRunner();
    //    }
    //
    //    @Bean
    //    @DependsOn({"afterTestRunner"})
    //    Dispatcher dispatcher(Publisher publisher) {
    //        return new Dispatcher(publisher);
    //    }
    //
    //        @Bean
    //        Publisher publisher() {
    //            return new Publisher();
    //        }
    @Bean
    //    @DependsOn({"dispatcher"})
    FakeAsyncService fakeAsyncService() {
        return new FakeAsyncService(1);
    }

    @Bean(initMethod = "onInit", destroyMethod = "onClose")
        //    @DependsOn({"dispatcher"})
    FakeService fakeService(Publisher publisher) {
        FakeService s = new FakeService(fakeAsyncService());
        publisher.addService(s);
        return s;
    }
}
