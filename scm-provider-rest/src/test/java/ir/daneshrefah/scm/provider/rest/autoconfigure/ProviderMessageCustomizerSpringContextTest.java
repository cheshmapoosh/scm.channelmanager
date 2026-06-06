package ir.daneshrefah.scm.provider.rest.autoconfigure;

import ir.daneshrefah.scm.common.provider.autoconfigure.ProviderMessageCustomizerAutoConfiguration;
import ir.daneshrefah.scm.common.provider.config.ProviderRegistryProperties;
import ir.daneshrefah.scm.common.provider.message.ProviderMessageCustomizerFactoryRegistry;
import ir.daneshrefah.scm.common.provider.message.ProviderMessageCustomizerPipelineFactory;
import ir.daneshrefah.scm.provider.rest.customizer.HpsRestOutletProviderMessageCustomizerFactory;
import ir.daneshrefah.scm.provider.rest.customizer.RestAuthUrlProviderMessageCustomizerFactory;
import ir.daneshrefah.scm.provider.rest.customizer.RestStaticAuthProviderMessageCustomizerFactory;
import ir.daneshrefah.scm.provider.rest.token.ProviderAuthToken;
import ir.daneshrefah.scm.provider.rest.token.ProviderAuthTokenProvider;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProviderMessageCustomizerSpringContextTest {

    @Test
    void commonCustomizerBeansAndRestFactoriesAreAvailableInSpringContext() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.register(ProviderMessageCustomizerAutoConfiguration.class);
            context.registerBean(ProviderAuthTokenProvider.class,
                    () -> (providerConfig, authConfig, customizerContext) -> new ProviderAuthToken("masked", "Bearer"));
            context.registerBean(RestAuthUrlProviderMessageCustomizerFactory.class);
            context.registerBean(RestStaticAuthProviderMessageCustomizerFactory.class);
            context.registerBean(HpsRestOutletProviderMessageCustomizerFactory.class);
            context.refresh();

            ProviderMessageCustomizerFactoryRegistry registry = context.getBean(ProviderMessageCustomizerFactoryRegistry.class);

            assertNotNull(context.getBean(ProviderRegistryProperties.class));
            assertNotNull(context.getBean(ProviderMessageCustomizerPipelineFactory.class));
            assertTrue(registry.contains("rest-auth-url"));
            assertTrue(registry.contains("rest-static-auth"));
            assertTrue(registry.contains("hps-rest-outlet"));
        }
    }
}
