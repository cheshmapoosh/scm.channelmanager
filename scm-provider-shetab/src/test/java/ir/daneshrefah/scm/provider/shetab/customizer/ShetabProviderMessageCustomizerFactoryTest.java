package ir.daneshrefah.scm.provider.shetab.customizer;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.provider.message.ProviderMessageCustomizerContext;
import ir.daneshrefah.scm.common.provider.message.ProviderMessageCustomizerDefinition;
import ir.daneshrefah.scm.common.provider.message.ProviderMessageCustomizerFactory;
import ir.daneshrefah.scm.common.provider.message.ProviderMessageCustomizerFactoryContext;
import ir.daneshrefah.scm.common.provider.message.ProviderMessageCustomizerFactoryRegistry;
import ir.daneshrefah.scm.common.provider.message.ProviderMessageCustomizerPipeline;
import ir.daneshrefah.scm.common.provider.message.ProviderMessageCustomizerPipelineFactory;
import ir.daneshrefah.scm.provider.shetab.iso.ShetabPackagerFactory;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ShetabProviderMessageCustomizerFactoryTest {

    @Test
    void shetabSecurityCustomizersAreOrderedBeforeMac() {
        ProviderMessageCustomizerPipeline pipeline = new ProviderMessageCustomizerPipelineFactory(
                new ProviderMessageCustomizerFactoryRegistry(factories()),
                new ObjectMapper()
        ).build(context("shetab", "shetab"), List.of(
                definition("shetab-mac", Map.of("key", "0123456789ABCDEF")),
                definition("shetab-pin-block", Map.of("key", "0123456789ABCDEF")),
                definition("hps-shetab-outlet", Map.of("field", 42, "value", "123456789012345")),
                definition("shetab-cvv2", Map.of("field", 48, "tag", "P92")),
                definition("shetab-expiry", Map.of("field", 14))
        ));

        assertEquals(
                List.of("hps-shetab-outlet", "shetab-expiry", "shetab-cvv2", "shetab-pin-block", "shetab-mac"),
                pipeline.entries().stream().map(ProviderMessageCustomizerPipeline.Entry::type).toList()
        );
    }

    @Test
    void shetabMacFactoryRejectsRestTransport() {
        ShetabMacProviderMessageCustomizerFactory factory = new ShetabMacProviderMessageCustomizerFactory(new ShetabPackagerFactory(new DefaultResourceLoader()));
        ShetabMacProviderMessageCustomizerFactory.Config config = new ShetabMacProviderMessageCustomizerFactory.Config();
        config.setKey("0123456789ABCDEF");

        assertThrows(IllegalArgumentException.class, () -> factory.create(ProviderMessageCustomizerFactoryContext.from(context("rest", "rest")), config));
    }

    private List<ProviderMessageCustomizerFactory<?>> factories() {
        return List.of(
                new HpsShetabOutletProviderMessageCustomizerFactory(),
                new ShetabExpiryProviderMessageCustomizerFactory(),
                new ShetabCvv2ProviderMessageCustomizerFactory(),
                new ShetabPinBlockProviderMessageCustomizerFactory(),
                new ShetabMacProviderMessageCustomizerFactory(new ShetabPackagerFactory(new DefaultResourceLoader()))
        );
    }

    private ProviderMessageCustomizerDefinition definition(String type, Map<String, Object> config) {
        ProviderMessageCustomizerDefinition definition = new ProviderMessageCustomizerDefinition();
        definition.setType(type);
        definition.setConfig(config);
        return definition;
    }

    private ProviderMessageCustomizerContext context(String providerType, String transportType) {
        return new ProviderMessageCustomizerContext(
                "hps", providerType, "svc", "op", "mb", transportType, Map.of(), null, "corr", "trace");
    }
}
