package ir.daneshrefah.scm.common.provider.autoconfigure;

import ir.daneshrefah.scm.common.provider.config.ProviderRegistryProperties;
import ir.daneshrefah.scm.common.provider.message.ProviderMessageCustomizerFactory;
import ir.daneshrefah.scm.common.provider.message.ProviderMessageCustomizerFactoryRegistry;
import ir.daneshrefah.scm.common.provider.message.ProviderMessageCustomizerPipelineFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.util.List;

@AutoConfiguration
@EnableConfigurationProperties(ProviderRegistryProperties.class)
public class ProviderMessageCustomizerAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ProviderMessageCustomizerFactoryRegistry providerMessageCustomizerFactoryRegistry(
            List<ProviderMessageCustomizerFactory<?>> factories
    ) {
        return new ProviderMessageCustomizerFactoryRegistry(factories);
    }

    @Bean
    @ConditionalOnMissingBean
    public ProviderMessageCustomizerPipelineFactory providerMessageCustomizerPipelineFactory(
            ProviderMessageCustomizerFactoryRegistry registry
    ) {
        return new ProviderMessageCustomizerPipelineFactory(registry);
    }
}
