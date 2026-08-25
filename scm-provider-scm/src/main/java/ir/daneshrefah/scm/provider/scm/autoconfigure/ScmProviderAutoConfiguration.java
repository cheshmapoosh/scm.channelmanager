package ir.daneshrefah.scm.provider.scm.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.provider.scm.camel.ScmComponent;
import ir.daneshrefah.scm.provider.scm.operation.ScmProviderOperationPayloadStrategy;
import ir.daneshrefah.scm.provider.scm.registry.ScmResourceRegistry;
import org.apache.camel.CamelContext;
import org.apache.camel.component.bean.BeanProcessor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnClass({CamelContext.class, BeanProcessor.class})
public class ScmProviderAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ScmResourceRegistry scmResourceRegistry(
            ApplicationContext applicationContext,
            CamelContext camelContext
    ) {
        return new ScmResourceRegistry(applicationContext, camelContext);
    }

    @Bean
    @ConditionalOnMissingBean(ScmProviderOperationPayloadStrategy.class)
    public ScmProviderOperationPayloadStrategy scmProviderOperationPayloadStrategy(
            ScmResourceRegistry resourceRegistry
    ) {
        return new ScmProviderOperationPayloadStrategy(resourceRegistry);
    }

    @Bean(name = ScmComponent.SCHEME)
    @ConditionalOnMissingBean(name = ScmComponent.SCHEME)
    public ScmComponent scmComponent(
            ScmResourceRegistry resourceRegistry,
            ObjectProvider<CamelContext> camelContextProvider,
            ObjectMapper objectMapper
    ) {
        CamelContext camelContext = camelContextProvider.getIfAvailable();
        return camelContext == null
                ? new ScmComponent(resourceRegistry, objectMapper)
                : new ScmComponent(camelContext, resourceRegistry, objectMapper);
    }
}
