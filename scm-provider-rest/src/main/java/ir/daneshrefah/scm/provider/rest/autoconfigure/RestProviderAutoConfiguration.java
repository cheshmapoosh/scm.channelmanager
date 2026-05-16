package ir.daneshrefah.scm.provider.rest.autoconfigure;

import ir.daneshrefah.scm.provider.rest.camel.RestProviderComponent;
import ir.daneshrefah.scm.provider.rest.config.RestProviderProperties;
import org.apache.camel.CamelContext;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
@ConditionalOnClass(CamelContext.class)
@EnableConfigurationProperties(RestProviderProperties.class)
@ConditionalOnProperty(prefix = "scm.provider.rest", name = "enabled", havingValue = "true", matchIfMissing = true)
public class RestProviderAutoConfiguration {

    @Bean("rest-provider")
    @ConditionalOnMissingBean(name = "rest-provider")
    public RestProviderComponent restProviderComponent(CamelContext camelContext) {
        RestProviderComponent component = new RestProviderComponent();
        component.setCamelContext(camelContext);
        return component;
    }

    @Bean(name = "restProviderVirtualThreadExecutor", destroyMethod = "close")
    @ConditionalOnMissingBean(name = "restProviderVirtualThreadExecutor")
    public ExecutorService restProviderVirtualThreadExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }
}
