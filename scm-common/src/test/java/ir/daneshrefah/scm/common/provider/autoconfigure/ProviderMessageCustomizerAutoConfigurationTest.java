package ir.daneshrefah.scm.common.provider.autoconfigure;

import ir.daneshrefah.scm.common.provider.config.ProviderRegistryProperties;
import ir.daneshrefah.scm.common.provider.message.ProviderMessageCustomizerFactoryRegistry;
import ir.daneshrefah.scm.common.provider.message.ProviderMessageCustomizerPipelineFactory;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class ProviderMessageCustomizerAutoConfigurationTest {

    @Test
    void providerMessageCustomizerAutoConfigurationIsLoadedByBootImports() {
        SpringApplication application = new SpringApplication(TestApplication.class);
        application.setWebApplicationType(WebApplicationType.NONE);

        try (ConfigurableApplicationContext context = application.run()) {
            assertNotNull(context.getBean(ProviderRegistryProperties.class));
            assertNotNull(context.getBean(ProviderMessageCustomizerFactoryRegistry.class));
            assertNotNull(context.getBean(ProviderMessageCustomizerPipelineFactory.class));
        }
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    static class TestApplication {
    }
}
