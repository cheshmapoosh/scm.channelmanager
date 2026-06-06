package ir.daneshrefah.scm.provider.shetab.config;

import ir.daneshrefah.scm.common.provider.config.ProviderRegistryProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

@Component
@Slf4j
public class ShetabConfigResolver {
    private final ProviderRegistryProperties providerRegistryProperties;
    private final ShetabProperties legacyProperties;
    private final AtomicBoolean legacyWarningLogged = new AtomicBoolean();

    @Autowired
    public ShetabConfigResolver(
            ProviderRegistryProperties providerRegistryProperties,
            ShetabProperties legacyProperties
    ) {
        this.providerRegistryProperties = providerRegistryProperties == null
                ? new ProviderRegistryProperties()
                : providerRegistryProperties;
        this.legacyProperties = legacyProperties == null ? new ShetabProperties() : legacyProperties;
    }

    public ShetabConfigResolver(ShetabProperties legacyProperties) {
        this(new ProviderRegistryProperties(), legacyProperties);
    }

    public ShetabResolvedConfig resolve(String provider, ShetabEndpointOverrides overrides) {
        String providerName = normalizeProviderName(provider);
        if (providerName == null) {
            throw new IllegalArgumentException("Shetab provider is required");
        }
        ProviderRegistryProperties.Provider unified = findUnifiedProvider(providerName);
        if (unified != null) {
            return resolveUnified(providerName, unified, overrides);
        }
        return resolveLegacy(providerName, overrides);
    }

    private ShetabResolvedConfig resolveUnified(
            String providerName,
            ProviderRegistryProperties.Provider instance,
            ShetabEndpointOverrides overrides
    ) {
        validateType(providerName, instance.getType());
        if (Boolean.FALSE.equals(instance.getEnabled())) {
            throw new IllegalArgumentException("Shetab provider " + providerName + " is disabled");
        }
        List<String> endpoints = mergedEndpoints(instance.getEndpoint(), instance.getEndpoints());
        if (endpoints.isEmpty()) {
            throw new IllegalArgumentException("Shetab provider " + providerName + " must define endpoint or endpoints");
        }
        if (StringUtils.isBlank(instance.getPackagerClass()) && StringUtils.isBlank(instance.getPackagerXml())) {
            throw new IllegalArgumentException("Shetab provider " + providerName + " must define packager-class or packager-xml");
        }
        int responseTimeout = value(instance.getResponseTimeoutMs(), 6000);
        return new ShetabResolvedConfig(
                providerName,
                "shetab",
                endpoints,
                instance.getPackagerClass(),
                instance.getPackagerXml(),
                value(instance.getConnectTimeoutMs(), 3000),
                value(instance.getSocketTimeoutMs(), 1000),
                overrides != null && overrides.timeoutMs() != null ? overrides.timeoutMs() : responseTimeout,
                value(instance.getSendTimeoutMs(), 1000),
                value(instance.getReconnectDelayMs(), 1000),
                value(instance.getSameEndpointReconnectAttempts(), 3),
                value(instance.getQueueCapacity(), 1000),
                cleanObjectMap(instance.getProviderConfig()),
                listOf(instance.getMessageCustomizers()),
                resolvedRateLimit(instance.getRateLimit(), overrides, providerName),
                new ShetabResolvedConfig.EndpointLease(
                        Boolean.TRUE.equals(first(instance.getEndpointLease().getEnabled(), Boolean.TRUE)),
                        value(instance.getEndpointLease().getTtlMs(), 30000L)
                ),
                emptySecurity()
        );
    }

    private ShetabResolvedConfig resolveLegacy(String providerName, ShetabEndpointOverrides overrides) {
        logLegacyWarning();
        ShetabProperties.Instance instance = findLegacyProvider(providerName);
        ShetabProperties.Instance defaults = legacyProperties.getDefaults();
        List<String> endpoints = resolveEndpoints(defaults, instance);
        if (endpoints.isEmpty()) {
            throw new IllegalArgumentException("Shetab provider " + providerName + " must define at least one endpoint (endpoint or endpoints)");
        }

        ShetabProperties.RateLimit rateLimit = mergeRateLimit(defaults.getRateLimit(), instance.getRateLimit());
        ShetabProperties.EndpointLease endpointLease = mergeEndpointLease(defaults.getEndpointLease(), instance.getEndpointLease());
        ShetabResolvedConfig.Security security = mergeSecurity(defaults.getSecurity(), instance.getSecurity());
        int responseTimeout = value(first(instance.getResponseTimeoutMs(), defaults.getResponseTimeoutMs()), 6000);

        return new ShetabResolvedConfig(
                providerName,
                "shetab",
                endpoints,
                first(instance.getPackagerClass(), defaults.getPackagerClass()),
                first(instance.getPackagerXml(), defaults.getPackagerXml()),
                value(first(instance.getConnectTimeoutMs(), defaults.getConnectTimeoutMs()), 3000),
                value(first(instance.getSocketTimeoutMs(), defaults.getSocketTimeoutMs()), 1000),
                overrides != null && overrides.timeoutMs() != null ? overrides.timeoutMs() : responseTimeout,
                value(first(instance.getSendTimeoutMs(), defaults.getSendTimeoutMs()), 1000),
                value(first(instance.getReconnectDelayMs(), defaults.getReconnectDelayMs()), 1000),
                value(first(instance.getSameEndpointReconnectAttempts(), defaults.getSameEndpointReconnectAttempts()), 3),
                value(first(instance.getQueueCapacity(), defaults.getQueueCapacity()), 1000),
                Map.of(),
                List.of(),
                resolvedLegacyRateLimit(rateLimit, overrides),
                new ShetabResolvedConfig.EndpointLease(
                        Boolean.TRUE.equals(endpointLease.getEnabled()),
                        value(endpointLease.getTtlMs(), 30000L)
                ),
                security
        );
    }

    private List<String> resolveEndpoints(ShetabProperties.Instance defaults, ShetabProperties.Instance instance) {
        List<String> instanceEndpoints = mergedEndpoints(instance.getEndpoint(), instance.getEndpoints());
        if (!instanceEndpoints.isEmpty()) {
            return instanceEndpoints;
        }
        return mergedEndpoints(defaults.getEndpoint(), defaults.getEndpoints());
    }

    private List<String> mergedEndpoints(String endpoint, List<String> endpoints) {
        return Stream.concat(Stream.of(endpoint), nonNullList(endpoints).stream())
                .map(StringUtils::trimToNull)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
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

    private ProviderRegistryProperties.Provider findUnifiedProvider(String providerName) {
        Map<String, ProviderRegistryProperties.Provider> providers = providerRegistryProperties.getProviders() == null
                ? Map.of()
                : providerRegistryProperties.getProviders();
        ProviderRegistryProperties.Provider exact = providers.get(providerName);
        if (exact != null) {
            return exact;
        }
        return providers.entrySet().stream()
                .filter(entry -> entry.getKey() != null
                        && entry.getKey().toLowerCase(Locale.ROOT).equals(providerName.toLowerCase(Locale.ROOT)))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
    }

    private ShetabProperties.Instance findLegacyProvider(String providerName) {
        Map<String, ShetabProperties.Instance> providers = legacyProperties.getProviders() == null
                ? Map.of()
                : legacyProperties.getProviders();
        ShetabProperties.Instance exact = providers.get(providerName);
        if (exact != null) {
            return exact;
        }
        return providers.entrySet()
                .stream()
                .filter(entry -> entry.getKey().toLowerCase(Locale.ROOT).equals(providerName.toLowerCase(Locale.ROOT)))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Shetab provider " + providerName + " is not configured"));
    }

    private void validateType(String providerName, String type) {
        if (!"shetab".equalsIgnoreCase(StringUtils.trimToEmpty(type))) {
            throw new IllegalArgumentException("Provider " + providerName + " must define type=shetab");
        }
    }

    private ShetabResolvedConfig.RateLimit resolvedRateLimit(
            ProviderRegistryProperties.RateLimit rateLimit,
            ShetabEndpointOverrides overrides,
            String providerName
    ) {
        ProviderRegistryProperties.RateLimit safe = rateLimit == null ? new ProviderRegistryProperties.RateLimit() : rateLimit;
        boolean enabled = Boolean.TRUE.equals(safe.getEnabled());
        String bucket = StringUtils.trimToNull(safe.getBucket());
        if (enabled && bucket == null) {
            throw new IllegalArgumentException("Shetab provider " + providerName + " rate-limit.bucket is required when rate-limit is enabled");
        }
        String key = StringUtils.defaultIfBlank(safe.getKey(), "provider-operation");
        if (overrides != null) {
            enabled = overrides.rateLimitEnabled() != null ? overrides.rateLimitEnabled() : enabled;
            bucket = StringUtils.defaultIfBlank(overrides.rateLimitBucket(), bucket);
            key = StringUtils.defaultIfBlank(overrides.rateLimitKey(), key);
        }
        return new ShetabResolvedConfig.RateLimit(enabled, bucket, key);
    }

    private ShetabResolvedConfig.RateLimit resolvedLegacyRateLimit(ShetabProperties.RateLimit rateLimit, ShetabEndpointOverrides overrides) {
        boolean enabled = Boolean.TRUE.equals(rateLimit.getEnabled());
        String bucket = value(rateLimit.getBucket(), "shetab-default");
        String key = value(rateLimit.getKey(), "provider");
        if (overrides != null) {
            enabled = overrides.rateLimitEnabled() != null ? overrides.rateLimitEnabled() : enabled;
            bucket = StringUtils.defaultIfBlank(overrides.rateLimitBucket(), bucket);
            key = StringUtils.defaultIfBlank(overrides.rateLimitKey(), key);
        }
        return new ShetabResolvedConfig.RateLimit(enabled, bucket, key);
    }

    private ShetabProperties.RateLimit mergeRateLimit(ShetabProperties.RateLimit defaults, ShetabProperties.RateLimit instance) {
        ShetabProperties.RateLimit result = new ShetabProperties.RateLimit();
        result.setEnabled(first(instance.getEnabled(), defaults.getEnabled()));
        result.setBucket(first(instance.getBucket(), defaults.getBucket()));
        result.setKey(first(instance.getKey(), defaults.getKey()));
        return result;
    }

    private ShetabProperties.EndpointLease mergeEndpointLease(ShetabProperties.EndpointLease defaults, ShetabProperties.EndpointLease instance) {
        ShetabProperties.EndpointLease result = new ShetabProperties.EndpointLease();
        result.setEnabled(first(instance.getEnabled(), defaults.getEnabled()));
        result.setTtlMs(first(instance.getTtlMs(), defaults.getTtlMs()));
        return result;
    }

    private ShetabResolvedConfig.Security mergeSecurity(ShetabProperties.Security defaults, ShetabProperties.Security instance) {
        ShetabProperties.Security defaultSecurity = defaults == null ? new ShetabProperties.Security() : defaults;
        ShetabProperties.Security instanceSecurity = instance == null ? new ShetabProperties.Security() : instance;

        ShetabProperties.Pin defaultPin = defaultSecurity.getPin() == null ? new ShetabProperties.Pin() : defaultSecurity.getPin();
        ShetabProperties.Pin instancePin = instanceSecurity.getPin() == null ? new ShetabProperties.Pin() : instanceSecurity.getPin();
        ShetabProperties.Mac defaultMac = defaultSecurity.getMac() == null ? new ShetabProperties.Mac() : defaultSecurity.getMac();
        ShetabProperties.Mac instanceMac = instanceSecurity.getMac() == null ? new ShetabProperties.Mac() : instanceSecurity.getMac();
        ShetabProperties.Expiry defaultExpiry = defaultSecurity.getExpiry() == null ? new ShetabProperties.Expiry() : defaultSecurity.getExpiry();
        ShetabProperties.Expiry instanceExpiry = instanceSecurity.getExpiry() == null ? new ShetabProperties.Expiry() : instanceSecurity.getExpiry();
        ShetabProperties.Cvv2 defaultCvv2 = defaultSecurity.getCvv2() == null ? new ShetabProperties.Cvv2() : defaultSecurity.getCvv2();
        ShetabProperties.Cvv2 instanceCvv2 = instanceSecurity.getCvv2() == null ? new ShetabProperties.Cvv2() : instanceSecurity.getCvv2();

        return new ShetabResolvedConfig.Security(
                new ShetabResolvedConfig.Pin(
                        Boolean.TRUE.equals(first(instancePin.getEnabled(), defaultPin.getEnabled())),
                        first(instancePin.getKey(), defaultPin.getKey()),
                        value(first(instancePin.getField(), defaultPin.getField()), 52),
                        value(first(instancePin.getPanField(), defaultPin.getPanField()), 2)
                ),
                new ShetabResolvedConfig.Mac(
                        Boolean.TRUE.equals(first(instanceMac.getEnabled(), defaultMac.getEnabled())),
                        first(instanceMac.getKey(), defaultMac.getKey()),
                        value(first(instanceMac.getField(), defaultMac.getField()), 128),
                        Boolean.TRUE.equals(first(instanceMac.getVerifyResponse(), defaultMac.getVerifyResponse())),
                        value(first(instanceMac.getPlaceholder(), defaultMac.getPlaceholder()), "AAAAAAAAAAAAAAAA"),
                        value(first(instanceMac.getPackedLengthBytes(), defaultMac.getPackedLengthBytes()), 16)
                ),
                new ShetabResolvedConfig.Expiry(
                        Boolean.TRUE.equals(first(instanceExpiry.getEnabled(), defaultExpiry.getEnabled())),
                        value(first(instanceExpiry.getField(), defaultExpiry.getField()), 14)
                ),
                new ShetabResolvedConfig.Cvv2(
                        Boolean.TRUE.equals(first(instanceCvv2.getEnabled(), defaultCvv2.getEnabled())),
                        value(first(instanceCvv2.getField(), defaultCvv2.getField()), 48),
                        value(first(instanceCvv2.getTag(), defaultCvv2.getTag()), "P92"),
                        value(first(instanceCvv2.getLengthDigits(), defaultCvv2.getLengthDigits()), 3),
                        value(first(instanceCvv2.getMinLength(), defaultCvv2.getMinLength()), 3),
                        value(first(instanceCvv2.getMaxLength(), defaultCvv2.getMaxLength()), 4)
                )
        );
    }

    private ShetabResolvedConfig.Security emptySecurity() {
        return new ShetabResolvedConfig.Security(
                new ShetabResolvedConfig.Pin(false, null, 52, 2),
                new ShetabResolvedConfig.Mac(false, null, 128, false, "AAAAAAAAAAAAAAAA", 16),
                new ShetabResolvedConfig.Expiry(false, 14),
                new ShetabResolvedConfig.Cvv2(false, 48, "P92", 3, 3, 4)
        );
    }

    private void logLegacyWarning() {
        if (legacyWarningLogged.compareAndSet(false, true)) {
            log.warn("Using deprecated scm.provider.shetab configuration. Migrate Shetab providers to scm.providers.<provider-code>.type=shetab.");
        }
    }

    private Map<String, Object> cleanObjectMap(Map<String, Object> input) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (input != null) {
            result.putAll(input);
        }
        result.entrySet().removeIf(entry -> StringUtils.isBlank(entry.getKey()) || entry.getValue() == null);
        return Map.copyOf(result);
    }

    private static <T> T first(T value, T fallback) {
        return Objects.nonNull(value) ? value : fallback;
    }

    private static int value(Integer value, int fallback) {
        return value != null ? value : fallback;
    }

    private static long value(Long value, long fallback) {
        return value != null ? value : fallback;
    }

    private static String value(String value, String fallback) {
        return StringUtils.defaultIfBlank(value, fallback);
    }

    private static <T> List<T> nonNullList(List<T> value) {
        return value == null ? List.of() : List.copyOf(value);
    }

    private static <T> List<T> listOf(List<T> value) {
        return value == null || value.isEmpty() ? List.of() : List.copyOf(value);
    }
}
