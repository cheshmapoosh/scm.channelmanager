package ir.daneshrefah.scm.provider.rest.message;

import ir.daneshrefah.scm.common.provider.message.ProviderExchange;
import ir.daneshrefah.scm.common.provider.message.ProviderMessageCustomizer;
import ir.daneshrefah.scm.common.provider.message.ProviderMessageCustomizerContext;
import ir.daneshrefah.scm.common.provider.message.ProviderRequest;
import ir.daneshrefah.scm.common.provider.message.ProviderMessageCustomizerDefinition;
import ir.daneshrefah.scm.common.provider.message.ProviderMessageCustomizerFactory;
import ir.daneshrefah.scm.common.provider.message.ProviderMessageCustomizerFactoryContext;
import ir.daneshrefah.scm.common.provider.message.ProviderMessageCustomizerFactoryRegistry;
import ir.daneshrefah.scm.common.provider.message.ProviderMessageCustomizerPipeline;
import ir.daneshrefah.scm.common.provider.message.ProviderMessageCustomizerPipelineFactory;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProviderMessageCustomizerPipelineFactoryTest {

    @Test
    void bindsTypedConfigAndSortsByFactoryOrder() {
        ProviderMessageCustomizerPipelineFactory pipelineFactory = pipelineFactory();

        ProviderMessageCustomizerPipeline pipeline = pipelineFactory.build(context(), List.of(
                definition("beta", null, Map.of("field-name", "B")),
                definition("alpha", null, Map.of("field-name", "A"))
        ));

        assertEquals(List.of("alpha", "beta"), pipeline.entries().stream().map(ProviderMessageCustomizerPipeline.Entry::type).toList());
        ProviderExchange exchange = new ProviderExchange(new ProviderRequest("POST", URI.create("https://example"), Map.of(), Map.of()), context());
        pipeline.entries().getFirst().customizer().beforeSend(exchange);
        assertEquals("A", exchange.request().headers().get("X-Field"));
    }

    @Test
    void definitionOrderOverrideWins() {
        ProviderMessageCustomizerPipeline pipeline = pipelineFactory().build(context(), List.of(
                definition("alpha", 900, Map.of("field-name", "A")),
                definition("beta", 100, Map.of("field-name", "B"))
        ));

        assertEquals(List.of("beta", "alpha"), pipeline.entries().stream().map(ProviderMessageCustomizerPipeline.Entry::type).toList());
        assertEquals(100, pipeline.entries().getFirst().order());
    }


    @Test
    void springBootBinderPreservesBusinessMapKeys() {
        CapturingRestAuthFactory factory = new CapturingRestAuthFactory();
        ProviderMessageCustomizerPipelineFactory pipelineFactory = new ProviderMessageCustomizerPipelineFactory(
                new ProviderMessageCustomizerFactoryRegistry(List.of(factory)));

        pipelineFactory.build(context(), List.of(definition("rest-auth-url", null, Map.of(
                "url", "https://provider/token",
                "request", Map.of(
                        "headers", Map.of("Content-Type", "application/x-www-form-urlencoded"),
                        "form", Map.of(
                                "grant_type", "client_credentials",
                                "client_id", "client",
                                "client_secret", "secret"
                        )
                ),
                "response", Map.of("token-field", "access_token"),
                "cache", Map.of("name", "token-cache", "auth-profile", "default", "credential-key", "hps"),
                "apply", Map.of("name", "Authorization")
        ))));

        assertEquals("access_token", factory.config.response().getTokenField());
        assertEquals("application/x-www-form-urlencoded", factory.config.request().getHeaders().get("Content-Type"));
        assertEquals("client_credentials", factory.config.request().getForm().get("grant_type"));
        assertEquals("client", factory.config.request().getForm().get("client_id"));
        assertEquals("secret", factory.config.request().getForm().get("client_secret"));
    }

    @Test
    void unknownTypeFailsFast() {
        ProviderMessageCustomizerDefinition definition = definition("missing", null, Map.of());

        assertThrows(IllegalArgumentException.class, () -> pipelineFactory().build(context(), List.of(definition)));
    }

    @Test
    void invalidCustomizerConfigFailsFastWithTypeInMessage() {
        ProviderMessageCustomizerPipelineFactory pipelineFactory = new ProviderMessageCustomizerPipelineFactory(
                new ProviderMessageCustomizerFactoryRegistry(List.of(new NumericFactory())));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> pipelineFactory.build(context(), List.of(
                        definition("numeric", null, Map.of("timeout-ms", "not-a-number")))));

        assertTrue(exception.getMessage().contains("provider message customizer type numeric"));
    }

    @Test
    void emptyDefinitionListProducesEmptyPipeline() {
        assertTrue(pipelineFactory().build(context(), List.of()).isEmpty());
    }

    private ProviderMessageCustomizerPipelineFactory pipelineFactory() {
        return new ProviderMessageCustomizerPipelineFactory(
                new ProviderMessageCustomizerFactoryRegistry(List.of(
                        new TestFactory("alpha", 100),
                        new TestFactory("beta", 200)
                ))
        );
    }

    private ProviderMessageCustomizerDefinition definition(String type, Integer order, Map<String, Object> config) {
        ProviderMessageCustomizerDefinition definition = new ProviderMessageCustomizerDefinition();
        definition.setType(type);
        definition.setOrder(order);
        definition.setConfig(config);
        return definition;
    }

    private ProviderMessageCustomizerContext context() {
        return new ProviderMessageCustomizerContext("p", "rest", "svc", "op", "ch", "rest", Map.of(), null, "corr", "trace");
    }

    private record TestFactory(String type, int defaultOrder) implements ProviderMessageCustomizerFactory<TestConfig> {
        @Override
        public Class<TestConfig> configType() {
            return TestConfig.class;
        }

        @Override
        public ProviderMessageCustomizer create(ProviderMessageCustomizerFactoryContext context, TestConfig config) {
            return new NamedCustomizer(config);
        }
    }

    private record NamedCustomizer(TestConfig delegate) implements ProviderMessageCustomizer {
        @Override
        public int order() {
            return 1;
        }

        @Override
        public void beforeSend(ProviderExchange exchange) {
            exchange.request().putHeader("X-Field", delegate.getFieldName());
        }
    }

    private static final class TestConfig {
        private String fieldName;

        public String getFieldName() {
            return fieldName;
        }

        public void setFieldName(String fieldName) {
            this.fieldName = fieldName;
        }
    }

    private static final class NumericFactory implements ProviderMessageCustomizerFactory<NumericConfig> {
        @Override
        public String type() {
            return "numeric";
        }

        @Override
        public Class<NumericConfig> configType() {
            return NumericConfig.class;
        }

        @Override
        public int defaultOrder() {
            return 100;
        }

        @Override
        public ProviderMessageCustomizer create(ProviderMessageCustomizerFactoryContext context, NumericConfig config) {
            return new ProviderMessageCustomizer() {
                @Override
                public int order() {
                    return 100;
                }
            };
        }
    }

    private static final class NumericConfig {
        private Integer timeoutMs;

        public Integer getTimeoutMs() {
            return timeoutMs;
        }

        public void setTimeoutMs(Integer timeoutMs) {
            this.timeoutMs = timeoutMs;
        }
    }

    private static final class CapturingRestAuthFactory implements ProviderMessageCustomizerFactory<ir.daneshrefah.scm.provider.rest.customizer.RestAuthUrlProviderMessageCustomizerConfig> {
        private ir.daneshrefah.scm.provider.rest.customizer.RestAuthUrlProviderMessageCustomizerConfig config;

        @Override
        public String type() {
            return "rest-auth-url";
        }

        @Override
        public Class<ir.daneshrefah.scm.provider.rest.customizer.RestAuthUrlProviderMessageCustomizerConfig> configType() {
            return ir.daneshrefah.scm.provider.rest.customizer.RestAuthUrlProviderMessageCustomizerConfig.class;
        }

        @Override
        public int defaultOrder() {
            return 5000;
        }

        @Override
        public ProviderMessageCustomizer create(ProviderMessageCustomizerFactoryContext context,
                                               ir.daneshrefah.scm.provider.rest.customizer.RestAuthUrlProviderMessageCustomizerConfig config) {
            this.config = config;
            return new ProviderMessageCustomizer() {
                @Override
                public int order() {
                    return 5000;
                }
            };
        }
    }

}
