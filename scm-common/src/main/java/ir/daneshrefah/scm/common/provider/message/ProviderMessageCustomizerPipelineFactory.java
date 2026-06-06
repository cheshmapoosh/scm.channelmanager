package ir.daneshrefah.scm.common.provider.message;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ProviderMessageCustomizerPipelineFactory {
    private final ProviderMessageCustomizerFactoryRegistry registry;
    private final ObjectMapper objectMapper;

    public ProviderMessageCustomizerPipelineFactory(
            ProviderMessageCustomizerFactoryRegistry registry,
            ObjectMapper objectMapper
    ) {
        if (registry == null) {
            throw new IllegalArgumentException("Provider message customizer factory registry is required");
        }
        if (objectMapper == null) {
            throw new IllegalArgumentException("ObjectMapper is required");
        }
        this.registry = registry;
        this.objectMapper = objectMapper;
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
        Class<?> configType = factory.configType();
        if (configType == null || configType == Void.class || configType == Void.TYPE) {
            return null;
        }
        try {
            return objectMapper.convertValue(normalizeMap(config), configType);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Invalid config for provider message customizer type: " + factory.type(), e);
        }
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

    @SuppressWarnings("unchecked")
    private Object normalizeMap(Object value) {
        if (value instanceof Map<?, ?> map) {
            Map<String, Object> normalized = new LinkedHashMap<>();
            map.forEach((key, item) -> {
                if (key != null) {
                    normalized.put(toCamelCase(String.valueOf(key)), normalizeMap(item));
                }
            });
            return normalized;
        }
        if (value instanceof List<?> list) {
            return list.stream().map(this::normalizeMap).toList();
        }
        return value;
    }

    private String toCamelCase(String value) {
        String key = StringUtils.defaultString(value).trim();
        if (key.isEmpty() || (!key.contains("-") && !key.contains("_"))) {
            return key;
        }
        StringBuilder builder = new StringBuilder();
        boolean upperNext = false;
        for (char character : key.toCharArray()) {
            if (character == '-' || character == '_') {
                upperNext = true;
                continue;
            }
            if (upperNext) {
                builder.append(Character.toUpperCase(character));
                upperNext = false;
            } else {
                builder.append(character);
            }
        }
        return builder.toString();
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
