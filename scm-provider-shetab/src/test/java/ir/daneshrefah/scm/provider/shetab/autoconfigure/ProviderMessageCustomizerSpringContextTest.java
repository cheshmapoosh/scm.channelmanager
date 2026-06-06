package ir.daneshrefah.scm.provider.shetab.autoconfigure;

import ir.daneshrefah.scm.common.provider.autoconfigure.ProviderMessageCustomizerAutoConfiguration;
import ir.daneshrefah.scm.common.provider.config.ProviderRegistryProperties;
import ir.daneshrefah.scm.common.provider.message.ProviderMessageCustomizerFactoryRegistry;
import ir.daneshrefah.scm.common.provider.message.ProviderMessageCustomizerPipelineFactory;
import ir.daneshrefah.scm.provider.shetab.customizer.HpsShetabOutletProviderMessageCustomizerFactory;
import ir.daneshrefah.scm.provider.shetab.customizer.HpsShetabTerminalProviderMessageCustomizerFactory;
import ir.daneshrefah.scm.provider.shetab.customizer.ShetabCvv2ProviderMessageCustomizerFactory;
import ir.daneshrefah.scm.provider.shetab.customizer.ShetabExpiryProviderMessageCustomizerFactory;
import ir.daneshrefah.scm.provider.shetab.customizer.ShetabMacProviderMessageCustomizerFactory;
import ir.daneshrefah.scm.provider.shetab.customizer.ShetabPinBlockProviderMessageCustomizerFactory;
import ir.daneshrefah.scm.provider.shetab.iso.ShetabPackagerFactory;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.io.DefaultResourceLoader;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProviderMessageCustomizerSpringContextTest {

    @Test
    void commonCustomizerBeansAndShetabFactoriesAreAvailableInSpringContext() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.register(ProviderMessageCustomizerAutoConfiguration.class);
            context.registerBean(ShetabPackagerFactory.class, () -> new ShetabPackagerFactory(new DefaultResourceLoader()));
            context.registerBean(HpsShetabOutletProviderMessageCustomizerFactory.class);
            context.registerBean(HpsShetabTerminalProviderMessageCustomizerFactory.class);
            context.registerBean(ShetabExpiryProviderMessageCustomizerFactory.class);
            context.registerBean(ShetabCvv2ProviderMessageCustomizerFactory.class);
            context.registerBean(ShetabPinBlockProviderMessageCustomizerFactory.class);
            context.registerBean(ShetabMacProviderMessageCustomizerFactory.class);
            context.refresh();

            ProviderMessageCustomizerFactoryRegistry registry = context.getBean(ProviderMessageCustomizerFactoryRegistry.class);

            assertNotNull(context.getBean(ProviderRegistryProperties.class));
            assertNotNull(context.getBean(ProviderMessageCustomizerPipelineFactory.class));
            assertTrue(registry.contains("hps-shetab-outlet"));
            assertTrue(registry.contains("hps-shetab-terminal"));
            assertTrue(registry.contains("shetab-expiry"));
            assertTrue(registry.contains("shetab-cvv2"));
            assertTrue(registry.contains("shetab-pin-block"));
            assertTrue(registry.contains("shetab-mac"));
        }
    }
}
