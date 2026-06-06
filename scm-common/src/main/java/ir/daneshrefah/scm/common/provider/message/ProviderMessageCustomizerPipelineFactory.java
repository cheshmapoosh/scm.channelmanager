package ir.daneshrefah.scm.common.provider.message;

import ir.daneshrefah.scm.common.provider.config.ProviderConfigurationBinder;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class ProviderMessageCustomizerPipelineFactory {
    private final ProviderMessageCustomizerFactoryRegistry registry;

    public ProviderMessageCustomizerPipelineFactory(ProviderMessageCustomizerFactoryRegistry registry) {
        if (registry == null) {
            throw new IllegalArgumentException("Provider message customizer factory registry is required");
        }
        this.registry = registry;
    }

    public ProviderMessageCustomizerPipeline build(
            ProviderMessageCustomizerContext context,
            List<ProviderMessageCustomizerDefinition> definitions
    ) {
        if (definitions == null || definitions.isEmpty()) {
            return ProviderMessageCustomizerPipeline.empty();
        }
        ProviderMessageCustomizerFactoryContext factoryContext = ProviderMessageCustomizerFactoryContext.from(context);
        List<ProviderMessageCustomizerPipeline.Entry> entries = new ArrayList<>();
        for (ProviderMessageCustomizerDefinition definition : definitions) {
            if (definition == null || StringUtils.isBlank(definition.getType())) {
                throw new IllegalArgumentException("Provider message customizer definition must define type");
            }
            ProviderMessageCustomizerFactory<?> factory = registry.getRequired(definition.getType());
            Object config = bindConfig(factory, definition.config());
            ProviderMessageCustomizer customizer = create(factory, factoryContext, config);
            int order = definition.getOrder() != null ? definition.getOrder() : factory.defaultOrder();
            entries.add(new ProviderMessageCustomizerPipeline.Entry(
                    definition.getType(),
                    new OrderedProviderMessageCustomizer(customizer, order)
            ));
        }
        return new ProviderMessageCustomizerPipeline(entries.stream()
                .sorted(Comparator
                        .comparingInt(ProviderMessageCustomizerPipeline.Entry::order)
                        .thenComparing(ProviderMessageCustomizerPipeline.Entry::type))
                .toList());
    }

    private Object bindConfig(ProviderMessageCustomizerFactory<?> factory, Map<String, Object> config) {
        return ProviderConfigurationBinder.bind(config, factory.configType(),
                "provider message customizer type " + factory.type());
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private ProviderMessageCustomizer create(
            ProviderMessageCustomizerFactory factory,
            ProviderMessageCustomizerFactoryContext context,
            Object config
    ) {
        ProviderMessageCustomizer customizer = factory.create(context, config);
        if (customizer == null) {
            throw new IllegalStateException("Provider message customizer factory returned null: " + factory.type());
        }
        return customizer;
    }

    private record OrderedProviderMessageCustomizer(ProviderMessageCustomizer delegate, int order)
            implements ProviderMessageCustomizer {
        @Override
        public void beforeSend(ProviderExchange exchange) {
            delegate.beforeSend(exchange);
        }

        @Override
        public void afterReceive(ProviderExchange exchange) {
            delegate.afterReceive(exchange);
        }
    }
}
