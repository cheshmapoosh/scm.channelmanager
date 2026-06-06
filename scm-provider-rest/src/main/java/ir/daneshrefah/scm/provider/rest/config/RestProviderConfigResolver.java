package ir.daneshrefah.scm.provider.rest.config;

import ir.daneshrefah.scm.common.provider.config.ProviderRegistryProperties;
import ir.daneshrefah.scm.common.provider.message.ProviderMessageCustomizerDefinition;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
@Slf4j
public class RestProviderConfigResolver {
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
    private final RestProviderProperties legacyProperties;
    private final AtomicBoolean legacyWarningLogged = new AtomicBoolean();

    @Autowired
    public RestProviderConfigResolver(
            ProviderRegistryProperties providerRegistryProperties,
            RestProviderProperties legacyProperties
    ) {
        this.providerRegistryProperties = providerRegistryProperties == null
                ? new ProviderRegistryProperties()
                : providerRegistryProperties;
        this.legacyProperties = legacyProperties == null ? new RestProviderProperties() : legacyProperties;
    }

    public RestProviderConfigResolver(RestProviderProperties legacyProperties) {
        this(new ProviderRegistryProperties(), legacyProperties);
    }

    public RestProviderResolvedConfig resolve(String provider, RestProviderEndpointOverrides overrides) {
        String providerName = normalizeProviderName(provider);
        if (providerName == null) {
            throw new IllegalArgumentException("REST provider is required");
        }

        ProviderRegistryProperties.Provider unified = findUnifiedProvider(providerName);
        if (unified != null) {
            return resolveUnified(providerName, unified, overrides);
        }
        return resolveLegacy(providerName, overrides);
    }

    private RestProviderResolvedConfig resolveUnified(
            String providerName,
            ProviderRegistryProperties.Provider instance,
            RestProviderEndpointOverrides overrides
    ) {
        validateType(providerName, instance.getType());
        if (Boolean.FALSE.equals(instance.getEnabled())) {
            throw new IllegalArgumentException("REST provider " + providerName + " is disabled");
        }
        String baseUrl = StringUtils.trimToNull(first(instance.getBaseUrl(), instance.getEndpoint()));
        if (baseUrl == null) {
            throw new IllegalArgumentException("REST provider " + providerName + " must define base-url");
        }
        int responseTimeoutMs = value(instance.getResponseTimeoutMs(), 6000);
        if (overrides != null && overrides.timeoutMs() != null) {
            responseTimeoutMs = overrides.timeoutMs();
        }
        RestProviderResolvedConfig.RateLimit rateLimit = resolvedRateLimit(instance.getRateLimit(), overrides, providerName);
        return new RestProviderResolvedConfig(
                providerName,
                "rest",
                baseUrl,
                value(instance.getConnectTimeoutMs(), 3000),
                responseTimeoutMs,
                booleanValue(instance.getVirtualThreadsEnabled(), true),
                booleanValue(instance.getInsecureSsl(), false),
                resolveRedirect(instance.getFollowRedirects()),
                StringUtils.defaultIfBlank(instance.getDefaultMethod(), "POST").trim(),
                cleanStringMap(instance.getHeaders()),
                cleanObjectMap(instance.getProviderConfig()),
                listOf(instance.getMessageCustomizers()),
                new RestProviderResolvedConfig.Proxy(
                        trim(instance.getProxy().getHost()),
                        instance.getProxy().getPort(),
                        trim(instance.getProxy().getUsername()),
                        trim(instance.getProxy().getPassword())
                ),
                new RestProviderResolvedConfig.Auth(
                        resolveAuthType(instance.getAuth().getType()),
                        StringUtils.defaultIfBlank(instance.getAuth().getHeaderName(), "Authorization").trim(),
                        trim(instance.getAuth().getPrefix()),
                        trim(instance.getAuth().getToken()),
                        trim(instance.getAuth().getUsername()),
                        trim(instance.getAuth().getPassword()),
                        booleanValue(instance.getAuth().getBasicBase64(), true)
                ),
                new RestProviderResolvedConfig.Security(
                        listOf(nonEmpty(instance.getSecurity().getSensitiveHeaders(), DEFAULT_SENSITIVE_HEADERS)),
                        listOf(nonEmpty(instance.getSecurity().getSensitiveBodyKeys(), DEFAULT_SENSITIVE_BODY_KEYS)),
                        value(instance.getSecurity().getMaxBodyLogLength(), 400)
                ),
                rateLimit
        );
    }

    private RestProviderResolvedConfig resolveLegacy(String providerName, RestProviderEndpointOverrides overrides) {
        logLegacyWarning();
        RestProviderProperties.Instance instance = findLegacyProvider(providerName);
        RestProviderProperties.Instance defaults = nonNull(legacyProperties.getDefaults(), new RestProviderProperties.Instance());

        String baseUrl = StringUtils.trimToNull(first(
                instance.getBaseUrl(),
                instance.getEndpoint(),
                defaults.getBaseUrl(),
                defaults.getEndpoint()
        ));
        if (baseUrl == null) {
            throw new IllegalArgumentException("REST provider " + providerName + " must define baseUrl (or endpoint)");
        }

        int responseTimeoutMs = value(first(instance.getResponseTimeoutMs(), defaults.getResponseTimeoutMs()), 6000);
        if (overrides != null && overrides.timeoutMs() != null) {
            responseTimeoutMs = overrides.timeoutMs();
        }

        RestProviderProperties.Proxy mergedProxy = mergeProxy(defaults.getProxy(), instance.getProxy());
        RestProviderProperties.Auth mergedAuth = mergeAuth(defaults.getAuth(), instance.getAuth());
        RestProviderProperties.Security mergedSecurity = mergeSecurity(defaults.getSecurity(), instance.getSecurity());
        RestProviderProperties.Token mergedToken = mergeToken(defaults.getToken(), instance.getToken());
        RestProviderProperties.RateLimit mergedRateLimit = mergeRateLimit(defaults.getRateLimit(), instance.getRateLimit());

        return new RestProviderResolvedConfig(
                providerName,
                "rest",
                baseUrl,
                value(first(instance.getConnectTimeoutMs(), defaults.getConnectTimeoutMs()), 3000),
                responseTimeoutMs,
                Boolean.TRUE.equals(first(instance.getVirtualThreadsEnabled(), defaults.getVirtualThreadsEnabled())),
                Boolean.TRUE.equals(first(instance.getInsecureSsl(), defaults.getInsecureSsl())),
                resolveRedirect(first(instance.getFollowRedirects(), defaults.getFollowRedirects())),
                StringUtils.defaultIfBlank(first(instance.getDefaultMethod(), defaults.getDefaultMethod()), "POST").trim(),
                mergeHeaders(defaults.getHeaders(), instance.getHeaders()),
                mergeObjectMap(defaults.getProviderConfig(), instance.getProviderConfig()),
                legacyMessageCustomizers(mergedToken),
                new RestProviderResolvedConfig.Proxy(
                        trim(mergedProxy.getHost()),
                        mergedProxy.getPort(),
                        trim(mergedProxy.getUsername()),
                        trim(mergedProxy.getPassword())
                ),
                new RestProviderResolvedConfig.Auth(
                        resolveAuthType(mergedAuth.getType()),
                        StringUtils.defaultIfBlank(mergedAuth.getHeaderName(), "Authorization").trim(),
                        trim(mergedAuth.getPrefix()),
                        trim(mergedAuth.getToken()),
                        trim(mergedAuth.getUsername()),
                        trim(mergedAuth.getPassword()),
                        Boolean.TRUE.equals(first(mergedAuth.getBasicBase64(), Boolean.TRUE))
                ),
                new RestProviderResolvedConfig.Security(
                        listOf(nonEmpty(mergedSecurity.getSensitiveHeaders(), DEFAULT_SENSITIVE_HEADERS)),
                        listOf(nonEmpty(mergedSecurity.getSensitiveBodyKeys(), DEFAULT_SENSITIVE_BODY_KEYS)),
                        value(mergedSecurity.getMaxBodyLogLength(), 400)
                ),
                resolvedLegacyRateLimit(mergedRateLimit, overrides)
        );
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
        if (name.isBlank()) {
            throw new IllegalArgumentException("Invalid REST provider name: " + providerName);
        }
        if (!"rest".equalsIgnoreCase(type) && !"rest-provider".equalsIgnoreCase(type) && !"restprovider".equalsIgnoreCase(type)) {
            throw new IllegalArgumentException("Invalid REST provider name: " + providerName + ". Expected rest:<name> or rest-provider:<name>");
        }
        return name;
    }

    private ProviderRegistryProperties.Provider findUnifiedProvider(String providerName) {
        Map<String, ProviderRegistryProperties.Provider> providers = nonNull(providerRegistryProperties.getProviders(), Map.of());
        ProviderRegistryProperties.Provider exact = providers.get(providerName);
        if (exact != null) {
            return exact;
        }
        return providers.entrySet()
                .stream()
                .filter(entry -> entry.getKey() != null
                        && entry.getKey().toLowerCase(Locale.ROOT).equals(providerName.toLowerCase(Locale.ROOT)))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
    }

    private RestProviderProperties.Instance findLegacyProvider(String providerName) {
        Map<String, RestProviderProperties.Instance> providers = nonNull(legacyProperties.getProviders(), Map.of());
        RestProviderProperties.Instance exact = providers.get(providerName);
        if (exact != null) {
            return exact;
        }
        return providers.entrySet()
                .stream()
                .filter(entry -> entry.getKey() != null
                        && entry.getKey().toLowerCase(Locale.ROOT).equals(providerName.toLowerCase(Locale.ROOT)))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("REST provider " + providerName + " is not configured"));
    }

    private void validateType(String providerName, String type) {
        String normalized = StringUtils.trimToNull(type);
        if (normalized == null) {
            throw new IllegalArgumentException("Provider " + providerName + " must define type=rest");
        }
        if (!"rest".equalsIgnoreCase(normalized)) {
            throw new IllegalArgumentException("Provider " + providerName + " has unsupported REST type: " + type);
        }
    }

    private RestProviderResolvedConfig.HttpRedirect resolveRedirect(String value) {
        String redirect = StringUtils.defaultIfBlank(value, "NORMAL").trim().toUpperCase(Locale.ROOT);
        try {
            return RestProviderResolvedConfig.HttpRedirect.valueOf(redirect);
        } catch (Exception ignored) {
            throw new IllegalArgumentException("Unsupported followRedirects value: " + value + ". Allowed: NEVER, NORMAL, ALWAYS");
        }
    }

    private RestProviderResolvedConfig.AuthType resolveAuthType(String value) {
        String authType = StringUtils.defaultIfBlank(value, "NONE").trim().toUpperCase(Locale.ROOT);
        try {
            return RestProviderResolvedConfig.AuthType.valueOf(authType);
        } catch (Exception ignored) {
            throw new IllegalArgumentException("Unsupported auth type: " + value + ". Allowed: NONE, BASIC, BEARER, JWT, API_KEY");
        }
    }

    private RestProviderResolvedConfig.RateLimit resolvedRateLimit(
            ProviderRegistryProperties.RateLimit rateLimit,
            RestProviderEndpointOverrides overrides,
            String providerName
    ) {
        ProviderRegistryProperties.RateLimit safe = rateLimit == null ? new ProviderRegistryProperties.RateLimit() : rateLimit;
        boolean enabled = Boolean.TRUE.equals(safe.getEnabled());
        String bucket = StringUtils.trimToNull(safe.getBucket());
        if (enabled && bucket == null) {
            throw new IllegalArgumentException("REST provider " + providerName + " rate-limit.bucket is required when rate-limit is enabled");
        }
        String key = StringUtils.defaultIfBlank(safe.getKey(), "provider-operation");
        if (overrides != null) {
            enabled = overrides.rateLimitEnabled() != null ? overrides.rateLimitEnabled() : enabled;
            bucket = StringUtils.defaultIfBlank(overrides.rateLimitBucket(), bucket);
            key = StringUtils.defaultIfBlank(overrides.rateLimitKey(), key);
        }
        return new RestProviderResolvedConfig.RateLimit(enabled, bucket, key);
    }

    private RestProviderResolvedConfig.RateLimit resolvedLegacyRateLimit(
            RestProviderProperties.RateLimit rateLimit,
            RestProviderEndpointOverrides overrides
    ) {
        boolean enabled = Boolean.TRUE.equals(first(rateLimit.getEnabled(), Boolean.FALSE));
        String bucket = StringUtils.trimToNull(rateLimit.getBucket());
        String key = StringUtils.defaultIfBlank(rateLimit.getKey(), "provider");
        if (overrides != null) {
            enabled = overrides.rateLimitEnabled() != null ? overrides.rateLimitEnabled() : enabled;
            bucket = StringUtils.defaultIfBlank(overrides.rateLimitBucket(), bucket);
            key = StringUtils.defaultIfBlank(overrides.rateLimitKey(), key);
        }
        return new RestProviderResolvedConfig.RateLimit(enabled, bucket, key);
    }

    private List<ProviderMessageCustomizerDefinition> legacyMessageCustomizers(RestProviderProperties.Token token) {
        if (token == null || !Boolean.TRUE.equals(first(token.getEnabled(), Boolean.FALSE))) {
            return List.of();
        }
        ProviderMessageCustomizerDefinition definition = new ProviderMessageCustomizerDefinition();
        definition.setType("rest-auth-url");
        definition.setConfig(legacyTokenConfig(token));
        return List.of(definition);
    }

    private Map<String, Object> legacyTokenConfig(RestProviderProperties.Token token) {
        Map<String, Object> config = new LinkedHashMap<>();
        put(config, "url", token.getUrl());
        put(config, "path", token.getPath());
        put(config, "method", token.getMethod());

        Map<String, Object> request = new LinkedHashMap<>();
        put(request, "headers", cleanStringMap(token.getHeaders()));
        put(request, "query", cleanStringMap(token.getQuery()));
        put(request, "form", cleanStringMap(token.getForm()));
        put(request, "body", cleanObjectMap(token.getBody()));
        put(request, "auth", legacyAuthConfig(token.getAuth()));
        put(config, "request", request);

        Map<String, Object> response = new LinkedHashMap<>();
        put(response, "token-field", token.getResponseTokenField());
        put(response, "expires-in-field", token.getResponseExpiresInField());
        put(response, "token-type-field", token.getResponseTokenTypeField());
        put(response, "default-token-type", token.getDefaultTokenType());
        put(config, "response", response);

        RestProviderProperties.Cache cache = token.getCache() == null ? new RestProviderProperties.Cache() : token.getCache();
        Map<String, Object> cacheConfig = new LinkedHashMap<>();
        put(cacheConfig, "name", token.getCacheName());
        put(cacheConfig, "key-prefix", first(cache.getKeyPrefix(), "provider-token"));
        put(cacheConfig, "auth-profile", first(token.getAuthProfile(), "default"));
        put(cacheConfig, "credential-key", first(token.getCredentialKey(), first(token.getCacheKey(), "access-token")));
        put(cacheConfig, "refresh-skew", first(cache.getRefreshSkew(), Duration.ofSeconds(value(token.getEarlyRefreshSeconds(), 30))));
        put(cacheConfig, "ttl-skew", first(cache.getTtlSkew(), Duration.ofSeconds(5)));
        put(config, "cache", cacheConfig);

        RestProviderProperties.Lock lock = token.getLock() == null ? new RestProviderProperties.Lock() : token.getLock();
        Map<String, Object> lockConfig = new LinkedHashMap<>();
        put(lockConfig, "key-prefix", first(lock.getKeyPrefix(), first(token.getLockName(), "provider-token-refresh-lock")));
        put(lockConfig, "wait-timeout", first(lock.getWaitTimeout(), Duration.ofSeconds(3)));
        put(lockConfig, "lease-time", first(lock.getLeaseTime(), Duration.ofSeconds(10)));
        put(lockConfig, "retry-delay", first(lock.getRetryDelay(), Duration.ofMillis(100)));
        put(config, "lock", lockConfig);

        RestProviderProperties.Apply apply = token.getApply() == null ? new RestProviderProperties.Apply() : token.getApply();
        Map<String, Object> applyConfig = new LinkedHashMap<>();
        put(applyConfig, "location", first(apply.getLocation(), "header"));
        put(applyConfig, "name", first(apply.getName(), "Authorization"));
        put(applyConfig, "format", first(apply.getFormat(), "{tokenType} {accessToken}"));
        put(config, "apply", applyConfig);
        return config;
    }

    private Map<String, Object> legacyAuthConfig(RestProviderProperties.Auth auth) {
        RestProviderProperties.Auth safe = auth == null ? new RestProviderProperties.Auth() : auth;
        Map<String, Object> result = new LinkedHashMap<>();
        put(result, "type", safe.getType());
        put(result, "header-name", safe.getHeaderName());
        put(result, "prefix", safe.getPrefix());
        put(result, "token", safe.getToken());
        put(result, "username", safe.getUsername());
        put(result, "password", safe.getPassword());
        put(result, "basic-base64", safe.getBasicBase64());
        return result;
    }

    private RestProviderProperties.Proxy mergeProxy(RestProviderProperties.Proxy defaults, RestProviderProperties.Proxy instance) {
        RestProviderProperties.Proxy fallback = nonNull(defaults, new RestProviderProperties.Proxy());
        RestProviderProperties.Proxy item = nonNull(instance, new RestProviderProperties.Proxy());
        RestProviderProperties.Proxy result = new RestProviderProperties.Proxy();
        result.setHost(first(item.getHost(), fallback.getHost()));
        result.setPort(first(item.getPort(), fallback.getPort()));
        result.setUsername(first(item.getUsername(), fallback.getUsername()));
        result.setPassword(first(item.getPassword(), fallback.getPassword()));
        return result;
    }

    private RestProviderProperties.Auth mergeAuth(RestProviderProperties.Auth defaults, RestProviderProperties.Auth instance) {
        RestProviderProperties.Auth fallback = nonNull(defaults, new RestProviderProperties.Auth());
        RestProviderProperties.Auth item = nonNull(instance, new RestProviderProperties.Auth());
        RestProviderProperties.Auth result = new RestProviderProperties.Auth();
        result.setType(first(item.getType(), fallback.getType()));
        result.setHeaderName(first(item.getHeaderName(), fallback.getHeaderName()));
        result.setPrefix(first(item.getPrefix(), fallback.getPrefix()));
        result.setToken(first(item.getToken(), fallback.getToken()));
        result.setUsername(first(item.getUsername(), fallback.getUsername()));
        result.setPassword(first(item.getPassword(), fallback.getPassword()));
        result.setBasicBase64(first(item.getBasicBase64(), fallback.getBasicBase64()));
        return result;
    }

    private RestProviderProperties.Security mergeSecurity(RestProviderProperties.Security defaults, RestProviderProperties.Security instance) {
        RestProviderProperties.Security fallback = nonNull(defaults, new RestProviderProperties.Security());
        RestProviderProperties.Security item = nonNull(instance, new RestProviderProperties.Security());
        RestProviderProperties.Security result = new RestProviderProperties.Security();
        result.setSensitiveHeaders(nonEmpty(item.getSensitiveHeaders(), fallback.getSensitiveHeaders()));
        result.setSensitiveBodyKeys(nonEmpty(item.getSensitiveBodyKeys(), fallback.getSensitiveBodyKeys()));
        result.setMaxBodyLogLength(first(item.getMaxBodyLogLength(), fallback.getMaxBodyLogLength()));
        return result;
    }

    private RestProviderProperties.Token mergeToken(RestProviderProperties.Token defaults, RestProviderProperties.Token instance) {
        RestProviderProperties.Token fallback = nonNull(defaults, new RestProviderProperties.Token());
        RestProviderProperties.Token item = nonNull(instance, new RestProviderProperties.Token());
        RestProviderProperties.Token result = new RestProviderProperties.Token();
        result.setEnabled(first(item.getEnabled(), fallback.getEnabled()));
        result.setAuthProfile(first(item.getAuthProfile(), fallback.getAuthProfile()));
        result.setCredentialKey(first(item.getCredentialKey(), fallback.getCredentialKey()));
        result.setCacheName(first(item.getCacheName(), fallback.getCacheName()));
        result.setCacheKey(first(item.getCacheKey(), fallback.getCacheKey()));
        result.setLockName(first(item.getLockName(), fallback.getLockName()));
        result.setEarlyRefreshSeconds(first(item.getEarlyRefreshSeconds(), fallback.getEarlyRefreshSeconds()));
        result.setDefaultExpiresInSeconds(first(item.getDefaultExpiresInSeconds(), fallback.getDefaultExpiresInSeconds()));
        result.setMethod(first(item.getMethod(), fallback.getMethod()));
        result.setUrl(first(item.getUrl(), fallback.getUrl()));
        result.setPath(first(item.getPath(), fallback.getPath()));
        result.setHeaders(mergeHeaders(fallback.getHeaders(), item.getHeaders()));
        result.setQuery(mergeHeaders(fallback.getQuery(), item.getQuery()));
        result.setForm(mergeHeaders(fallback.getForm(), item.getForm()));
        result.setBody(mergeObjectMap(fallback.getBody(), item.getBody()));
        result.setAuth(mergeAuth(fallback.getAuth(), item.getAuth()));
        result.setResponseTokenField(first(item.getResponseTokenField(), fallback.getResponseTokenField()));
        result.setResponseExpiresInField(first(item.getResponseExpiresInField(), fallback.getResponseExpiresInField()));
        result.setResponseTokenTypeField(first(item.getResponseTokenTypeField(), fallback.getResponseTokenTypeField()));
        result.setDefaultTokenType(first(item.getDefaultTokenType(), fallback.getDefaultTokenType()));
        result.setCache(mergeTokenCache(fallback.getCache(), item.getCache()));
        result.setLock(mergeTokenLock(fallback.getLock(), item.getLock()));
        result.setApply(mergeTokenApply(fallback.getApply(), item.getApply()));
        return result;
    }

    private RestProviderProperties.RateLimit mergeRateLimit(RestProviderProperties.RateLimit defaults, RestProviderProperties.RateLimit instance) {
        RestProviderProperties.RateLimit fallback = nonNull(defaults, new RestProviderProperties.RateLimit());
        RestProviderProperties.RateLimit item = nonNull(instance, new RestProviderProperties.RateLimit());
        RestProviderProperties.RateLimit result = new RestProviderProperties.RateLimit();
        result.setEnabled(first(item.getEnabled(), fallback.getEnabled()));
        result.setBucket(first(item.getBucket(), fallback.getBucket()));
        result.setKey(first(item.getKey(), fallback.getKey()));
        return result;
    }

    private RestProviderProperties.Cache mergeTokenCache(RestProviderProperties.Cache defaults, RestProviderProperties.Cache instance) {
        RestProviderProperties.Cache fallback = nonNull(defaults, new RestProviderProperties.Cache());
        RestProviderProperties.Cache item = nonNull(instance, new RestProviderProperties.Cache());
        RestProviderProperties.Cache result = new RestProviderProperties.Cache();
        result.setEnabled(first(item.getEnabled(), fallback.getEnabled()));
        result.setMode(first(item.getMode(), fallback.getMode()));
        result.setKeyPrefix(first(item.getKeyPrefix(), fallback.getKeyPrefix()));
        result.setRefreshSkew(first(item.getRefreshSkew(), fallback.getRefreshSkew()));
        result.setTtlSkew(first(item.getTtlSkew(), fallback.getTtlSkew()));
        return result;
    }

    private RestProviderProperties.Lock mergeTokenLock(RestProviderProperties.Lock defaults, RestProviderProperties.Lock instance) {
        RestProviderProperties.Lock fallback = nonNull(defaults, new RestProviderProperties.Lock());
        RestProviderProperties.Lock item = nonNull(instance, new RestProviderProperties.Lock());
        RestProviderProperties.Lock result = new RestProviderProperties.Lock();
        result.setEnabled(first(item.getEnabled(), fallback.getEnabled()));
        result.setKeyPrefix(first(item.getKeyPrefix(), fallback.getKeyPrefix()));
        result.setWaitTimeout(first(item.getWaitTimeout(), fallback.getWaitTimeout()));
        result.setLeaseTime(first(item.getLeaseTime(), fallback.getLeaseTime()));
        result.setRetryDelay(first(item.getRetryDelay(), fallback.getRetryDelay()));
        return result;
    }

    private RestProviderProperties.Apply mergeTokenApply(RestProviderProperties.Apply defaults, RestProviderProperties.Apply instance) {
        RestProviderProperties.Apply fallback = nonNull(defaults, new RestProviderProperties.Apply());
        RestProviderProperties.Apply item = nonNull(instance, new RestProviderProperties.Apply());
        RestProviderProperties.Apply result = new RestProviderProperties.Apply();
        result.setLocation(first(item.getLocation(), fallback.getLocation()));
        result.setName(first(item.getName(), fallback.getName()));
        result.setFormat(first(item.getFormat(), fallback.getFormat()));
        return result;
    }

    private void logLegacyWarning() {
        if (legacyWarningLogged.compareAndSet(false, true)) {
            log.warn("Using deprecated scm.provider.rest configuration. Migrate REST providers to scm.providers.<provider-code>.type=rest.");
        }
    }

    private Map<String, String> mergeHeaders(Map<String, String> defaults, Map<String, String> instance) {
        Map<String, String> headers = new LinkedHashMap<>();
        headers.putAll(nonNull(defaults, Map.of()));
        headers.putAll(nonNull(instance, Map.of()));
        headers.entrySet().removeIf(entry -> StringUtils.isBlank(entry.getKey()) || entry.getValue() == null);
        return Map.copyOf(headers);
    }

    private Map<String, Object> mergeObjectMap(Map<String, Object> defaults, Map<String, Object> instance) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.putAll(nonNull(defaults, Map.of()));
        result.putAll(nonNull(instance, Map.of()));
        result.entrySet().removeIf(entry -> StringUtils.isBlank(entry.getKey()) || entry.getValue() == null);
        return Map.copyOf(result);
    }

    private Map<String, String> cleanStringMap(Map<String, String> input) {
        Map<String, String> result = new LinkedHashMap<>();
        result.putAll(nonNull(input, Map.of()));
        result.entrySet().removeIf(entry -> StringUtils.isBlank(entry.getKey()) || entry.getValue() == null);
        return Map.copyOf(result);
    }

    private Map<String, Object> cleanObjectMap(Map<String, Object> input) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.putAll(nonNull(input, Map.of()));
        result.entrySet().removeIf(entry -> StringUtils.isBlank(entry.getKey()) || entry.getValue() == null);
        return Map.copyOf(result);
    }

    private void put(Map<String, Object> target, String key, Object value) {
        if (value != null) {
            target.put(key, value);
        }
    }

    private static <T> T first(T value, T fallback) {
        return Objects.nonNull(value) ? value : fallback;
    }

    @SafeVarargs
    private static <T> T first(T... values) {
        if (values == null) {
            return null;
        }
        for (T value : values) {
            if (Objects.nonNull(value)) {
                return value;
            }
        }
        return null;
    }

    private static <T> T nonNull(T value, T fallback) {
        return value != null ? value : fallback;
    }

    private static <T> List<T> listOf(List<T> value) {
        if (value == null || value.isEmpty()) {
            return List.of();
        }
        return List.copyOf(value);
    }

    private static <T> List<T> nonEmpty(List<T> value, List<T> fallback) {
        if (value != null && !value.isEmpty()) {
            return value;
        }
        return fallback;
    }

    private static int value(Integer value, int fallback) {
        return value != null ? value : fallback;
    }

    private static boolean booleanValue(Boolean value, boolean fallback) {
        return value != null ? value : fallback;
    }

    private static String trim(String value) {
        return StringUtils.trimToNull(value);
    }
}
