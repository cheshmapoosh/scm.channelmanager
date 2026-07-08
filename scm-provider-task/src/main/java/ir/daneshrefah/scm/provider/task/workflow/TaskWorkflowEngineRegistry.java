package ir.daneshrefah.scm.provider.task.workflow;

import ir.daneshrefah.scm.provider.task.autoconfigure.ScmTaskProviderProperties;
import ir.daneshrefah.scm.provider.task.autoconfigure.TaskProviderInstanceProperties;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class TaskWorkflowEngineRegistry {
    public static final String INTERNAL_PROVIDER_CODE = "internal";

    private final Map<String, TaskProviderInstanceProperties> providers;
    private final Map<String, TaskWorkflowEngine> enginesByType;

    public TaskWorkflowEngineRegistry(
            ScmTaskProviderProperties properties,
            Collection<TaskWorkflowEngine> engines
    ) {
        this.providers = Map.copyOf(normalizeProviders(properties));
        this.enginesByType = Map.copyOf(indexEngines(engines));
        validateEnabledProviders();
    }

    public String requireProviderCode(String providerCode) {
        String normalized = requireNonBlank(providerCode, "providerCode");
        if (!normalized.equals(providerCode)) {
            throw invalidProvider(providerCode,
                    "leading or trailing whitespace is not allowed");
        }
        if (!providers.containsKey(normalized)) {
            throw invalidProvider(providerCode,
                    "providerCode is not configured; configured providers="
                            + providers.keySet());
        }
        return normalized;
    }

    public TaskWorkflowEngine engineFor(String providerCode) {
        String normalizedProviderCode = requireProviderCode(providerCode);
        TaskProviderInstanceProperties provider = providers.get(normalizedProviderCode);
        if (!provider.isEnabled()) {
            throw invalidProvider(providerCode, "provider is disabled");
        }
        String engineType = engineType(provider, normalizedProviderCode);
        TaskWorkflowEngine engine = enginesByType.get(engineType);
        if (engine == null) {
            throw invalidEngineType(normalizedProviderCode, engineType);
        }
        return engine;
    }

    private Map<String, TaskProviderInstanceProperties> normalizeProviders(
            ScmTaskProviderProperties properties
    ) {
        Map<String, TaskProviderInstanceProperties> result = new LinkedHashMap<>();
        for (Map.Entry<String, TaskProviderInstanceProperties> entry
                : properties.resolvedProviders().entrySet()) {
            String providerCode = requireNonBlank(entry.getKey(), "providerCode");
            if (!providerCode.equals(entry.getKey())) {
                throw invalidProvider(entry.getKey(),
                        "leading or trailing whitespace is not allowed");
            }
            TaskProviderInstanceProperties value = entry.getValue();
            if (value == null) {
                throw invalidProvider(providerCode, "provider configuration is missing");
            }
            result.put(providerCode, value);
        }
        return result;
    }

    private Map<String, TaskWorkflowEngine> indexEngines(Collection<TaskWorkflowEngine> engines) {
        Map<String, TaskWorkflowEngine> result = new LinkedHashMap<>();
        if (engines == null) {
            return result;
        }
        for (TaskWorkflowEngine engine : engines) {
            if (engine == null) {
                continue;
            }
            String engineType = requireNonBlank(engine.engineType(), "engineType");
            TaskWorkflowEngine existing = result.putIfAbsent(engineType, engine);
            if (existing != null) {
                throw new IllegalStateException("Duplicate task workflow engine-type="
                        + engineType + " implementations: "
                        + existing.getClass().getName() + ", "
                        + engine.getClass().getName());
            }
        }
        return result;
    }

    private void validateEnabledProviders() {
        for (Map.Entry<String, TaskProviderInstanceProperties> entry : providers.entrySet()) {
            if (!entry.getValue().isEnabled()) {
                continue;
            }
            String engineType = engineType(entry.getValue(), entry.getKey());
            if (!enginesByType.containsKey(engineType)) {
                throw invalidEngineType(entry.getKey(), engineType);
            }
        }
    }

    private String engineType(
            TaskProviderInstanceProperties provider,
            String providerCode
    ) {
        String engineType = provider.getEngineType();
        if (!StringUtils.hasText(engineType)) {
            throw new IllegalStateException("Invalid scm.provider.task provider configuration. "
                    + "providerCode=" + providerCode + " requires non-blank engine-type");
        }
        String normalized = engineType.trim();
        if (!normalized.equals(engineType)) {
            throw new IllegalStateException("Invalid scm.provider.task provider configuration. "
                    + "providerCode=" + providerCode
                    + " engine-type must not have leading or trailing whitespace");
        }
        return normalized;
    }

    private String requireNonBlank(String value, String field) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalStateException("Invalid scm.provider.task provider configuration. "
                    + field + " must not be blank");
        }
        return value.trim();
    }

    private IllegalArgumentException invalidProvider(
            String providerCode,
            String reason
    ) {
        return new IllegalArgumentException("Invalid scm-task providerCode="
                + providerCode + ": " + reason);
    }

    private IllegalStateException invalidEngineType(
            String providerCode,
            String engineType
    ) {
        String available = enginesByType.keySet().stream()
                .sorted()
                .collect(Collectors.toList())
                .toString();
        return new IllegalStateException("Invalid scm.provider.task provider configuration. "
                + "providerCode=" + providerCode + " references unknown engine-type="
                + engineType + "; implemented engine-types=" + available);
    }
}
