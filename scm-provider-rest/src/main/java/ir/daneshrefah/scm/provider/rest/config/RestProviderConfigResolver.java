package ir.daneshrefah.scm.provider.rest.config;

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

@Component
@Slf4j
public class RestProviderConfigResolver {
    public static final String COMPONENT_SCHEME = "scm-rest";

    private static final List<String> DEFAULT_SENSITIVE_HEADERS = List.of(
            "authorization",
            "proxy-authorization",
            "cookie",
            "set-cookie",
            "x-api-key",
            "api-key"
    );
    private static final List<String> DEFAULT_SENSITIVE_BODY_KEYS = List.of(
            "password",
            "token",
            "secret",
            "pin",
            "cvv",
            "pan",
            "card",
            "authorization"
    );

    private final ProviderRegistryProperties providerRegistryProperties;
    private final ProviderMessageCustomizerPipelineFactory pipelineFactory;
    private final ConcurrentMap<String, RestProviderResolvedConfig> resolvedConfigs = new ConcurrentHashMap<>();

    public RestProviderConfigResolver(
            ProviderRegistryProperties providerRegistryProperties,
            ProviderMessageCustomizerPipelineFactory pipelineFactory
    ) {
        this.providerRegistryProperties = providerRegistryProperties == null
                ? new ProviderRegistryProperties()
                : providerRegistryProperties;
        this.pipelineFactory = Objects.requireNonNull(pipelineFactory, "ProviderMessageCustomizerPipelineFactory is required");
    }

    public RestProviderResolvedConfig resolve(String provider, RestProviderEndpointOverrides overrides) {
        ProviderReference reference = normalizeProviderReference(provider);
        if (reference == null) {
            throw new IllegalArgumentException("REST provider is required");
        }
        RestProviderResolvedConfig base = resolvedConfigs.computeIfAbsent(cacheKey(reference.providerCode()), ignored -> resolveBase(reference));
        return base.withOverrides(overrides);
    }

    public String providerName(String provider) {
        ProviderReference reference = normalizeProviderReference(provider);
        return reference == null ? null : reference.providerCode();
    }

    public List<String> availableProviderCodes() {
        return providerRegistryProperties.entrySet().stream()
                .filter(entry -> entry.getKey() != null && isRestProvider(entry.getValue()))
                .map(Map.Entry::getKey)
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }

    private RestProviderResolvedConfig resolveBase(ProviderReference reference) {
        RegistryEntry entry = findProvider(reference.providerCode(), reference.scheme());
        RestProviderInstanceProperties instance = ProviderConfigurationBinder.bind(
                entry.properties(), RestProviderInstanceProperties.class, "REST provider " + entry.providerCode());
        validate(entry.providerCode(), reference.scheme(), instance);
        RestProviderResolvedConfig.RateLimit rateLimit = resolvedRateLimit(instance.getRateLimit(), entry.providerCode());
        ProviderMessageCustomizerContext customizerContext = new ProviderMessageCustomizerContext(
                entry.providerCode(),
                COMPONENT_SCHEME,
                providerUri(COMPONENT_SCHEME, entry.providerCode()),
                null,
                null,
                null,
                cleanObjectMap(instance.getProviderConfig()),
                instance,
                null,
                null
        );
        ProviderMessageCustomizerPipeline pipeline = pipelineFactory.build(customizerContext, instance.getMessageCustomizers());
        log.debug("REST provider runtime resolved provider={} customizers={}", entry.providerCode(),
                pipeline.entries().stream().map(item -> item.type() + "#" + item.order()).toList());
        return new RestProviderResolvedConfig(
                entry.providerCode(),
                COMPONENT_SCHEME,
                StringUtils.trimToNull(first(instance.getBaseUrl(), instance.getEndpoint())),
                value(instance.getConnectTimeoutMs(), 3000),
                value(instance.getResponseTimeoutMs(), 6000),
                booleanValue(instance.getVirtualThreadsEnabled(), true),
                booleanValue(instance.getInsecureSsl(), false),
                resolveRedirect(instance.getFollowRedirects()),
                StringUtils.defaultIfBlank(instance.getDefaultMethod(), "POST").trim(),
                cleanStringMap(instance.getHeaders()),
                cleanObjectMap(instance.getProviderConfig()),
                pipeline,
                new RestProviderResolvedConfig.Proxy(
                        trim(instance.getProxy().getHost()),
                        instance.getProxy().getPort(),
                        trim(instance.getProxy().getUsername()),
                        trim(instance.getProxy().getPassword())
                ),
                new RestProviderResolvedConfig.Security(
                        listOf(nonEmpty(instance.getSecurity().getSensitiveHeaders(), DEFAULT_SENSITIVE_HEADERS)),
                        listOf(nonEmpty(instance.getSecurity().getSensitiveBodyKeys(), DEFAULT_SENSITIVE_BODY_KEYS)),
                        value(instance.getSecurity().getMaxBodyLogLength(), 400)
                ),
                rateLimit
        );
    }

    private void validate(String providerName, String uriScheme, RestProviderInstanceProperties instance) {
        String configuredScheme = StringUtils.trimToNull(instance.getScheme());
        if (configuredScheme == null) {
            throw new IllegalArgumentException("Provider " + providerName + " must define scheme=" + COMPONENT_SCHEME);
        }
        if (!COMPONENT_SCHEME.equalsIgnoreCase(configuredScheme)) {
            throw new IllegalArgumentException("Provider URI scheme mismatch for provider '" + providerName
                    + "'. URI scheme is '" + uriScheme + "' but configured scheme is '" + configuredScheme + "'.");
        }
        if (Boolean.FALSE.equals(instance.getEnabled())) {
            throw new IllegalArgumentException("REST provider " + providerName + " is disabled");
        }
        if (StringUtils.isBlank(first(instance.getBaseUrl(), instance.getEndpoint()))) {
            throw new IllegalArgumentException("REST provider " + providerName + " must define base-url");
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
            throw new IllegalArgumentException("Invalid REST provider name: " + providerName);
        }
        if (!COMPONENT_SCHEME.equalsIgnoreCase(scheme)) {
            throw unsupportedScheme(scheme);
        }
        return new ProviderReference(name, scheme, providerUri(scheme, name));
    }

    private RestProviderResolvedConfig.HttpRedirect resolveRedirect(String value) {
        String redirect = StringUtils.defaultIfBlank(value, "NORMAL").trim().toUpperCase(Locale.ROOT);
        try {
            return RestProviderResolvedConfig.HttpRedirect.valueOf(redirect);
        } catch (Exception ignored) {
            throw new IllegalArgumentException("Unsupported followRedirects value: " + value + ". Allowed: NEVER, NORMAL, ALWAYS");
        }
    }

    private RestProviderResolvedConfig.RateLimit resolvedRateLimit(
            RestProviderInstanceProperties.RateLimit rateLimit,
            String providerName
    ) {
        RestProviderInstanceProperties.RateLimit safe = rateLimit == null ? new RestProviderInstanceProperties.RateLimit() : rateLimit;
        boolean enabled = Boolean.TRUE.equals(safe.getEnabled());
        String bucket = StringUtils.trimToNull(safe.getBucket());
        if (enabled && bucket == null) {
            throw new IllegalArgumentException("REST provider " + providerName + " rate-limit.bucket is required when rate-limit is enabled");
        }
        String key = StringUtils.defaultIfBlank(safe.getKey(), "provider-operation");
        return new RestProviderResolvedConfig.RateLimit(enabled, bucket, key);
    }

    private String cacheKey(String providerName) {
        return providerName.toLowerCase(Locale.ROOT);
    }

    private boolean isRestProvider(Map<String, Object> properties) {
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

    private String trim(String value) {
        return StringUtils.trimToNull(value);
    }

    private int value(Integer value, int fallback) {
        return value == null ? fallback : value;
    }

    private boolean booleanValue(Boolean value, boolean fallback) {
        return value == null ? fallback : value;
    }

    @SafeVarargs
    private <T> T first(T... values) {
        if (values == null) {
            return null;
        }
        for (T value : values) {
            if (value instanceof String text) {
                if (StringUtils.isNotBlank(text)) {
                    return value;
                }
            } else if (value != null) {
                return value;
            }
        }
        return null;
    }

    private <T> List<T> listOf(List<T> values) {
        return values == null ? List.of() : List.copyOf(values);
    }

    private <T> List<T> nonEmpty(List<T> values, List<T> fallback) {
        return values == null || values.isEmpty() ? fallback : values;
    }

    private Map<String, String> cleanStringMap(Map<String, String> values) {
        Map<String, String> cleaned = new LinkedHashMap<>();
        if (values != null) {
            values.forEach((key, value) -> {
                if (StringUtils.isNotBlank(key) && value != null) {
                    cleaned.put(key, value);
                }
            });
        }
        return Map.copyOf(cleaned);
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
