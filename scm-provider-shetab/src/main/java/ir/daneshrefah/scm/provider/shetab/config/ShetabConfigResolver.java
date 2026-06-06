package ir.daneshrefah.scm.provider.shetab.config;

import ir.daneshrefah.scm.common.provider.config.ProviderConfigurationBinder;
import ir.daneshrefah.scm.common.provider.config.ProviderRegistryProperties;
import ir.daneshrefah.scm.common.provider.message.ProviderMessageCustomizerContext;
import ir.daneshrefah.scm.common.provider.message.ProviderMessageCustomizerPipeline;
import ir.daneshrefah.scm.common.provider.message.ProviderMessageCustomizerPipelineFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Stream;

@Component
@Slf4j
public class ShetabConfigResolver {
    private final ProviderRegistryProperties providerRegistryProperties;
    private final ProviderMessageCustomizerPipelineFactory pipelineFactory;
    private final ConcurrentMap<String, ShetabResolvedConfig> resolvedConfigs = new ConcurrentHashMap<>();

    public ShetabConfigResolver(
            ProviderRegistryProperties providerRegistryProperties,
            ProviderMessageCustomizerPipelineFactory pipelineFactory
    ) {
        this.providerRegistryProperties = providerRegistryProperties == null
                ? new ProviderRegistryProperties()
                : providerRegistryProperties;
        this.pipelineFactory = Objects.requireNonNull(pipelineFactory, "ProviderMessageCustomizerPipelineFactory is required");
    }

    public ShetabResolvedConfig resolve(String provider, ShetabEndpointOverrides overrides) {
        String providerName = normalizeProviderName(provider);
        if (providerName == null) {
            throw new IllegalArgumentException("Shetab provider is required");
        }
        ShetabResolvedConfig base = resolvedConfigs.computeIfAbsent(cacheKey(providerName), ignored -> resolveBase(providerName));
        return base.withOverrides(overrides);
    }

    private ShetabResolvedConfig resolveBase(String providerName) {
        RegistryEntry entry = findProvider(providerName);
        ShetabProviderInstanceProperties instance = ProviderConfigurationBinder.bind(
                entry.properties(), ShetabProviderInstanceProperties.class, "Shetab provider " + entry.providerCode());
        validate(entry.providerCode(), instance);
        ProviderMessageCustomizerContext customizerContext = new ProviderMessageCustomizerContext(
                entry.providerCode(), "shetab", null, null, null, "shetab",
                cleanObjectMap(instance.getProviderConfig()), instance, null, null);
        ProviderMessageCustomizerPipeline pipeline = pipelineFactory.build(customizerContext, instance.getMessageCustomizers());
        log.debug("Shetab provider runtime resolved provider={} customizers={}", entry.providerCode(),
                pipeline.entries().stream().map(item -> item.type() + "#" + item.order()).toList());
        return new ShetabResolvedConfig(
                entry.providerCode(),
                "shetab",
                mergedEndpoints(instance.getEndpoints(), instance.getEndpoint()),
                instance.getPackagerClass(),
                instance.getPackagerXml(),
                value(instance.getConnectTimeoutMs(), 3000),
                value(instance.getSocketTimeoutMs(), 1000),
                value(instance.getResponseTimeoutMs(), 6000),
                value(instance.getSendTimeoutMs(), 1000),
                value(instance.getReconnectDelayMs(), 1000),
                value(instance.getSameEndpointReconnectAttempts(), 3),
                value(instance.getQueueCapacity(), 1000),
                cleanObjectMap(instance.getProviderConfig()),
                pipeline,
                resolvedRateLimit(instance.getRateLimit(), entry.providerCode()),
                new ShetabResolvedConfig.EndpointLease(
                        Boolean.TRUE.equals(first(instance.getEndpointLease().getEnabled(), Boolean.TRUE)),
                        value(instance.getEndpointLease().getTtlMs(), 30000L)
                )
        );
    }

    private void validate(String providerName, ShetabProviderInstanceProperties instance) {
        if (!"shetab".equalsIgnoreCase(StringUtils.trimToEmpty(instance.getType()))) {
            throw new IllegalArgumentException("Provider " + providerName + " must define type=shetab");
        }
        if (Boolean.FALSE.equals(instance.getEnabled())) {
            throw new IllegalArgumentException("Shetab provider " + providerName + " is disabled");
        }
        if (mergedEndpoints(instance.getEndpoints(), instance.getEndpoint()).isEmpty()) {
            throw new IllegalArgumentException("Shetab provider " + providerName + " must define endpoints or endpoint");
        }
        if (StringUtils.isBlank(instance.getPackagerClass()) && StringUtils.isBlank(instance.getPackagerXml())) {
            throw new IllegalArgumentException("Shetab provider " + providerName + " must define packager-class or packager-xml");
        }
    }

    private RegistryEntry findProvider(String providerName) {
        Map<String, Object> exact = providerRegistryProperties.provider(providerName);
        if (exact != null) {
            return new RegistryEntry(providerName, exact);
        }
        return providerRegistryProperties.entrySet().stream()
                .filter(entry -> entry.getKey() != null
                        && entry.getKey().toLowerCase(Locale.ROOT).equals(providerName.toLowerCase(Locale.ROOT)))
                .map(entry -> new RegistryEntry(entry.getKey(), entry.getValue()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Shetab provider " + providerName + " is not configured"));
    }

    private String normalizeProviderName(String provider) {
        String providerName = StringUtils.trimToNull(provider);
        if (providerName == null) {
            return null;
        }
        int separator = providerName.indexOf(':');
        if (separator < 0) {
            return providerName;
        }
        String type = providerName.substring(0, separator).trim();
        String name = providerName.substring(separator + 1).trim();
        if (!"shetab".equalsIgnoreCase(type) || name.isBlank()) {
            throw new IllegalArgumentException("Invalid Shetab provider name: " + providerName + ". Expected shetab:<name>");
        }
        return name;
    }

    private List<String> mergedEndpoints(List<String> endpoints, String endpointAlias) {
        return Stream.concat((endpoints == null ? List.<String>of() : endpoints).stream(), Stream.of(endpointAlias))
                .map(StringUtils::trimToNull)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    private ShetabResolvedConfig.RateLimit resolvedRateLimit(
            ShetabProviderInstanceProperties.RateLimit rateLimit,
            String providerName
    ) {
        ShetabProviderInstanceProperties.RateLimit safe = rateLimit == null ? new ShetabProviderInstanceProperties.RateLimit() : rateLimit;
        boolean enabled = Boolean.TRUE.equals(safe.getEnabled());
        String bucket = StringUtils.trimToNull(safe.getBucket());
        if (enabled && bucket == null) {
            throw new IllegalArgumentException("Shetab provider " + providerName + " rate-limit.bucket is required when rate-limit is enabled");
        }
        String key = StringUtils.defaultIfBlank(safe.getKey(), "provider-operation");
        return new ShetabResolvedConfig.RateLimit(enabled, bucket, key);
    }

    private String cacheKey(String providerName) {
        return providerName.toLowerCase(Locale.ROOT);
    }

    private int value(Integer value, int fallback) {
        return value == null ? fallback : value;
    }

    private long value(Long value, long fallback) {
        return value == null ? fallback : value;
    }

    @SafeVarargs
    private <T> T first(T... values) {
        if (values == null) {
            return null;
        }
        for (T value : values) {
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private Map<String, Object> cleanObjectMap(Map<String, Object> values) {
        Map<String, Object> cleaned = new LinkedHashMap<>();
        if (values != null) {
            values.forEach((key, value) -> {
                if (StringUtils.isNotBlank(key) && value != null) {
                    cleaned.put(key, value);
                }
            });
        }
        return Map.copyOf(cleaned);
    }

    private record RegistryEntry(String providerCode, Map<String, Object> properties) {
    }
}
