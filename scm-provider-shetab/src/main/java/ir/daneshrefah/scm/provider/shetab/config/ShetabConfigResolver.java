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
    public static final String COMPONENT_SCHEME = "scm-shetab";

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
        ProviderReference reference = normalizeProviderReference(provider);
        if (reference == null) {
            throw new IllegalArgumentException("Shetab provider is required");
        }
        ShetabResolvedConfig base = resolvedConfigs.computeIfAbsent(cacheKey(reference.providerCode()), ignored -> resolveBase(reference));
        return base.withOverrides(overrides);
    }

    public String providerName(String provider) {
        ProviderReference reference = normalizeProviderReference(provider);
        return reference == null ? null : reference.providerCode();
    }

    public List<String> availableProviderCodes() {
        return providerRegistryProperties.entrySet().stream()
                .filter(entry -> entry.getKey() != null && isShetabProvider(entry.getValue()))
                .map(Map.Entry::getKey)
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }

    private ShetabResolvedConfig resolveBase(ProviderReference reference) {
        RegistryEntry entry = findProvider(reference.providerCode(), reference.scheme());
        ShetabProviderInstanceProperties instance = ProviderConfigurationBinder.bind(
                entry.properties(), ShetabProviderInstanceProperties.class, "Shetab provider " + entry.providerCode());
        validate(entry.providerCode(), reference.scheme(), reference.providerUri(), instance);
        ProviderMessageCustomizerContext customizerContext = new ProviderMessageCustomizerContext(
                entry.providerCode(), COMPONENT_SCHEME, providerUri(COMPONENT_SCHEME, entry.providerCode()),
                null, null, null,
                cleanObjectMap(instance.getProviderConfig()), instance, null, null);
        ProviderMessageCustomizerPipeline pipeline = pipelineFactory.build(customizerContext, instance.getMessageCustomizers());
        log.debug("Shetab provider runtime resolved provider={} customizers={}", entry.providerCode(),
                pipeline.entries().stream().map(item -> item.type() + "#" + item.order()).toList());
        return new ShetabResolvedConfig(
                entry.providerCode(),
                COMPONENT_SCHEME,
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

    private void validate(String providerName,
                          String uriScheme,
                          String providerUri,
                          ShetabProviderInstanceProperties instance) {
        String configuredScheme = StringUtils.trimToNull(instance.getScheme());
        if (configuredScheme == null) {
            throw new IllegalArgumentException("Provider " + providerName + " must define scheme=" + COMPONENT_SCHEME);
        }
        if (!COMPONENT_SCHEME.equalsIgnoreCase(configuredScheme)) {
            throw new IllegalArgumentException("Provider URI scheme mismatch for provider '" + providerName
                    + "'. providerUri='" + providerUri
                    + "', URI scheme is '" + uriScheme
                    + "' but configured scheme is '" + configuredScheme + "'.");
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

    private RegistryEntry findProvider(String providerName, String scheme) {
        Map<String, Object> exact = providerRegistryProperties.provider(providerName);
        if (exact != null) {
            return new RegistryEntry(providerName, exact);
        }
        return providerRegistryProperties.entrySet().stream()
                .filter(entry -> entry.getKey() != null
                        && entry.getKey().toLowerCase(Locale.ROOT).equals(providerName.toLowerCase(Locale.ROOT)))
                .map(entry -> new RegistryEntry(entry.getKey(), entry.getValue()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Provider '" + providerName
                        + "' with scheme '" + scheme + "' is not configured. Available providers for scheme '" + scheme + "': "
                        + availableProviderCodes()));
    }

    private ProviderReference normalizeProviderReference(String provider) {
        String providerName = StringUtils.trimToNull(provider);
        if (providerName == null) {
            return null;
        }
        int separator = providerName.indexOf(':');
        if (separator < 0) {
            return new ProviderReference(providerName, COMPONENT_SCHEME, providerUri(COMPONENT_SCHEME, providerName));
        }
        String scheme = providerName.substring(0, separator).trim();
        String name = providerName.substring(separator + 1).trim();
        if (name.isBlank()) {
            throw new IllegalArgumentException("Invalid Shetab provider name: " + providerName);
        }
        if (!COMPONENT_SCHEME.equalsIgnoreCase(scheme)) {
            throw unsupportedScheme(scheme);
        }
        return new ProviderReference(name, scheme, providerUri(scheme, name));
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

    private boolean isShetabProvider(Map<String, Object> properties) {
        if (properties == null) {
            return false;
        }
        Object scheme = properties.get("scheme");
        return scheme != null && COMPONENT_SCHEME.equalsIgnoreCase(String.valueOf(scheme));
    }

    private IllegalArgumentException unsupportedScheme(String scheme) {
        return new IllegalArgumentException("Unsupported SCM provider scheme '" + scheme
                + "'. Use '" + COMPONENT_SCHEME + ":<providerCode>' instead.");
    }

    private String providerUri(String scheme, String providerCode) {
        return scheme + ":" + providerCode;
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

    private record ProviderReference(String providerCode, String scheme, String providerUri) {
    }
}
