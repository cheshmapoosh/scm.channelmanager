package ir.daneshrefah.scm.provider.rest.message;

import com.fasterxml.jackson.databind.ObjectMapper;
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
    void unknownTypeFailsFast() {
        ProviderMessageCustomizerDefinition definition = definition("missing", null, Map.of());

        assertThrows(IllegalArgumentException.class, () -> pipelineFactory().build(context(), List.of(definition)));
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
                )),
                new ObjectMapper()
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
}
