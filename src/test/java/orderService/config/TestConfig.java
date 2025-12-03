package orderService.config;

import orderService.security.RequestContext;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class TestConfig {
    @Bean
    @Primary
    public RequestContext getRequstContex() {
        return new MockRequestContext();
    }
}
