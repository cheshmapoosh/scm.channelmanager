package ir.daneshrefah.scm.web.observation.propagation;

import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.boot.web.client.RestTemplateCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ScmTracePropagationConfiguration {
    @Bean
    public RestTemplateCustomizer scmTraceParentRestTemplateCustomizer(ScmTracePropagationInterceptor interceptor) {
        return restTemplate -> restTemplate.getInterceptors().add(interceptor);
    }

    @Bean
    public RestClientCustomizer scmTraceParentRestClientCustomizer(ScmTracePropagationInterceptor interceptor) {
        return builder -> builder.requestInterceptor(interceptor);
    }
}
