package ir.daneshrefah.scm.provider.nab.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.provider.nab.camel.NabComponent;
import ir.daneshrefah.scm.provider.nab.config.NabProperties;
import org.apache.camel.CamelContext;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass(CamelContext.class)
@EnableConfigurationProperties(NabProperties.class)
@ConditionalOnProperty(prefix = "scm.provider.nab", name = "enabled", havingValue = "true", matchIfMissing = true)
public class NabProviderAutoConfiguration {

    @Bean("nab")
    @ConditionalOnMissingBean(name = "nab")
    public NabComponent nabComponent(CamelContext camelContext) {
        NabComponent component = new NabComponent();
        component.setCamelContext(camelContext);
        return component;
    }

    @Bean
    @ConditionalOnMissingBean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
