package ir.daneshrefah.scm.provider.rest.config;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

@Component
@RequiredArgsConstructor
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

    private final RestProviderProperties properties;

    public RestProviderResolvedConfig resolve(String provider, RestProviderEndpointOverrides overrides) {
        String providerName = normalizeProviderName(provider);
        if (providerName == null) {
            throw new IllegalArgumentException("REST provider is required");
        }

        RestProviderProperties.Instance instance = findProvider(providerName);
        RestProviderProperties.Instance defaults = nonNull(properties.getDefaults(), new RestProviderProperties.Instance());

        String baseUrl = StringUtils.trimToNull(first(
                instance.getBaseUrl(),
                instance.getEndpoint(),
                defaults.getBaseUrl(),
                defaults.getEndpoint()
        ));
        if (baseUrl == null) {
            throw new IllegalArgumentException("REST provider " + providerName + " must define baseUrl (or endpoint)");
        }

        int connectTimeoutMs = value(first(instance.getConnectTimeoutMs(), defaults.getConnectTimeoutMs()), 3000);
        int responseTimeoutMs = value(first(instance.getResponseTimeoutMs(), defaults.getResponseTimeoutMs()), 6000);
        if (overrides != null && overrides.timeoutMs() != null) {
            responseTimeoutMs = overrides.timeoutMs();
        }

        boolean virtualThreadsEnabled = Boolean.TRUE.equals(first(
                instance.getVirtualThreadsEnabled(),
                defaults.getVirtualThreadsEnabled()
        ));
        boolean insecureSsl = Boolean.TRUE.equals(first(instance.getInsecureSsl(), defaults.getInsecureSsl()));
        RestProviderResolvedConfig.HttpRedirect redirect = resolveRedirect(first(
                instance.getFollowRedirects(),
                defaults.getFollowRedirects()
        ));

        String defaultMethod = StringUtils.defaultIfBlank(first(instance.getDefaultMethod(), defaults.getDefaultMethod()), "POST").trim();

        RestProviderProperties.Proxy mergedProxy = mergeProxy(defaults.getProxy(), instance.getProxy());
        RestProviderProperties.Auth mergedAuth = mergeAuth(defaults.getAuth(), instance.getAuth());
        RestProviderProperties.Security mergedSecurity = mergeSecurity(defaults.getSecurity(), instance.getSecurity());
        RestProviderProperties.Token mergedToken = mergeToken(defaults.getToken(), instance.getToken());
        RestProviderProperties.Customizers mergedCustomizers = mergeCustomizers(defaults.getCustomizers(), instance.getCustomizers());
        RestProviderProperties.RateLimit mergedRateLimit = mergeRateLimit(defaults.getRateLimit(), instance.getRateLimit());

        Map<String, String> headers = mergeHeaders(defaults.getHeaders(), instance.getHeaders());
        Map<String, Object> providerConfig = mergeProviderConfig(defaults.getProviderConfig(), instance.getProviderConfig());
        RestProviderProperties.Auth mergedTokenAuth = nonNull(mergedToken.getAuth(), new RestProviderProperties.Auth());
        RestProviderResolvedConfig.Auth resolvedAuth = new RestProviderResolvedConfig.Auth(
                resolveAuthType(mergedAuth.getType()),
                StringUtils.defaultIfBlank(mergedAuth.getHeaderName(), "Authorization").trim(),
                StringUtils.trimToNull(mergedAuth.getPrefix()),
                StringUtils.trimToNull(mergedAuth.getToken()),
                StringUtils.trimToNull(mergedAuth.getUsername()),
                StringUtils.trimToNull(mergedAuth.getPassword()),
                Boolean.TRUE.equals(first(mergedAuth.getBasicBase64(), Boolean.TRUE))
        );
        boolean tokenEnabled = Boolean.TRUE.equals(first(mergedToken.getEnabled(), Boolean.FALSE));
        boolean authenticationCustomizerEnabled = Boolean.TRUE.equals(first(
                mergedCustomizers.getAuthentication(),
                tokenEnabled
        ));

        return new RestProviderResolvedConfig(
                providerName,
                baseUrl,
                connectTimeoutMs,
                responseTimeoutMs,
                virtualThreadsEnabled,
                insecureSsl,
                redirect,
                defaultMethod,
                headers,
                providerConfig,
                new RestProviderResolvedConfig.Customizers(authenticationCustomizerEnabled),
                new RestProviderResolvedConfig.Proxy(
                        StringUtils.trimToNull(mergedProxy.getHost()),
                        mergedProxy.getPort(),
                        StringUtils.trimToNull(mergedProxy.getUsername()),
                        StringUtils.trimToNull(mergedProxy.getPassword())
                ),
                resolvedAuth,
                new RestProviderResolvedConfig.Security(
                        listOf(nonEmpty(mergedSecurity.getSensitiveHeaders(), DEFAULT_SENSITIVE_HEADERS)),
                        listOf(nonEmpty(mergedSecurity.getSensitiveBodyKeys(), DEFAULT_SENSITIVE_BODY_KEYS)),
                        value(mergedSecurity.getMaxBodyLogLength(), 400)
                ),
                new RestProviderResolvedConfig.Token(
                        tokenEnabled,
                        value(mergedToken.getAuthProfile(), "default"),
                        value(mergedToken.getCredentialKey(), value(mergedToken.getCacheKey(), "access-token")),
                        value(mergedToken.getCacheName(), "rest_provider_token_cache"),
                        value(mergedToken.getCacheKey(), "access-token"),
                        value(mergedToken.getLockName(), "rest-provider-token"),
                        value(mergedToken.getEarlyRefreshSeconds(), 30),
                        value(mergedToken.getDefaultExpiresInSeconds(), 300),
                        value(mergedToken.getMethod(), "POST"),
                        StringUtils.trimToNull(mergedToken.getUrl()),
                        StringUtils.trimToNull(mergedToken.getPath()),
                        mergeHeaders(Map.of(), mergedToken.getHeaders()),
                        mergeHeaders(Map.of(), mergedToken.getQuery()),
                        nonNull(mergedToken.getBody(), Map.of()),
                        mergeHeaders(Map.of(), mergedToken.getForm()),
                        new RestProviderResolvedConfig.Auth(
                                resolveAuthType(mergedTokenAuth.getType()),
                                StringUtils.defaultIfBlank(mergedTokenAuth.getHeaderName(), "Authorization").trim(),
                                StringUtils.trimToNull(mergedTokenAuth.getPrefix()),
                                StringUtils.trimToNull(mergedTokenAuth.getToken()),
                                StringUtils.trimToNull(mergedTokenAuth.getUsername()),
                                StringUtils.trimToNull(mergedTokenAuth.getPassword()),
                                Boolean.TRUE.equals(first(mergedTokenAuth.getBasicBase64(), Boolean.TRUE))
                        ),
                        value(mergedToken.getResponseTokenField(), "access_token"),
                        value(mergedToken.getResponseExpiresInField(), "expires_in"),
                        value(mergedToken.getResponseTokenTypeField(), "token_type"),
                        value(mergedToken.getDefaultTokenType(), "Bearer"),
                        resolveTokenCache(mergedToken),
                        resolveTokenLock(mergedToken),
                        resolveTokenApply(mergedToken, resolvedAuth)
                ),
                resolvedRateLimit(mergedRateLimit, overrides)
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

    private RestProviderProperties.Instance findProvider(String providerName) {
        Map<String, RestProviderProperties.Instance> providers = nonNull(properties.getProviders(), Map.of());
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

    private RestProviderProperties.Customizers mergeCustomizers(
            RestProviderProperties.Customizers defaults,
            RestProviderProperties.Customizers instance
    ) {
        RestProviderProperties.Customizers fallback = nonNull(defaults, new RestProviderProperties.Customizers());
        RestProviderProperties.Customizers item = nonNull(instance, new RestProviderProperties.Customizers());
        RestProviderProperties.Customizers result = new RestProviderProperties.Customizers();
        result.setAuthentication(first(item.getAuthentication(), fallback.getAuthentication()));
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
        result.setBody(mergeBody(fallback.getBody(), item.getBody()));
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

    private RestProviderResolvedConfig.RateLimit resolvedRateLimit(
            RestProviderProperties.RateLimit rateLimit,
            RestProviderEndpointOverrides overrides
    ) {
        boolean enabled = Boolean.TRUE.equals(first(rateLimit.getEnabled(), Boolean.FALSE));
        String bucket = value(rateLimit.getBucket(), "rest-default");
        String key = value(rateLimit.getKey(), "provider");
        if (overrides != null) {
            enabled = overrides.rateLimitEnabled() != null ? overrides.rateLimitEnabled() : enabled;
            bucket = StringUtils.defaultIfBlank(overrides.rateLimitBucket(), bucket);
            key = StringUtils.defaultIfBlank(overrides.rateLimitKey(), key);
        }
        return new RestProviderResolvedConfig.RateLimit(enabled, bucket, key);
    }

    private Map<String, Object> mergeBody(Map<String, Object> defaults, Map<String, Object> instance) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.putAll(nonNull(defaults, Map.of()));
        body.putAll(nonNull(instance, Map.of()));
        body.entrySet().removeIf(entry -> StringUtils.isBlank(entry.getKey()) || entry.getValue() == null);
        return Map.copyOf(body);
    }

    private Map<String, Object> mergeProviderConfig(Map<String, Object> defaults, Map<String, Object> instance) {
        Map<String, Object> providerConfig = new LinkedHashMap<>();
        providerConfig.putAll(nonNull(defaults, Map.of()));
        providerConfig.putAll(nonNull(instance, Map.of()));
        providerConfig.entrySet().removeIf(entry -> StringUtils.isBlank(entry.getKey()) || entry.getValue() == null);
        return Map.copyOf(providerConfig);
    }

    private Map<String, String> mergeHeaders(Map<String, String> defaults, Map<String, String> instance) {
        Map<String, String> headers = new LinkedHashMap<>();
        headers.putAll(nonNull(defaults, Map.of()));
        headers.putAll(nonNull(instance, Map.of()));
        headers.entrySet().removeIf(entry -> StringUtils.isBlank(entry.getKey()) || entry.getValue() == null);
        return Map.copyOf(headers);
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

    private RestProviderResolvedConfig.TokenCache resolveTokenCache(RestProviderProperties.Token token) {
        RestProviderProperties.Cache cache = nonNull(token.getCache(), new RestProviderProperties.Cache());
        int earlyRefreshSeconds = value(token.getEarlyRefreshSeconds(), 30);
        return new RestProviderResolvedConfig.TokenCache(
                Boolean.TRUE.equals(first(cache.getEnabled(), Boolean.TRUE)),
                value(cache.getMode(), "centralized").trim().toLowerCase(Locale.ROOT),
                value(cache.getKeyPrefix(), "provider-token"),
                first(cache.getRefreshSkew(), Duration.ofSeconds(Math.max(0, earlyRefreshSeconds))),
                first(cache.getTtlSkew(), Duration.ofSeconds(5))
        );
    }

    private RestProviderResolvedConfig.TokenLock resolveTokenLock(RestProviderProperties.Token token) {
        RestProviderProperties.Lock lock = nonNull(token.getLock(), new RestProviderProperties.Lock());
        return new RestProviderResolvedConfig.TokenLock(
                Boolean.TRUE.equals(first(lock.getEnabled(), Boolean.TRUE)),
                value(lock.getKeyPrefix(), value(token.getLockName(), "provider-token-refresh-lock")),
                first(lock.getWaitTimeout(), Duration.ofSeconds(3)),
                first(lock.getLeaseTime(), Duration.ofSeconds(10)),
                first(lock.getRetryDelay(), Duration.ofMillis(100))
        );
    }

    private RestProviderResolvedConfig.TokenApply resolveTokenApply(
            RestProviderProperties.Token token,
            RestProviderResolvedConfig.Auth auth
    ) {
        RestProviderProperties.Apply apply = nonNull(token.getApply(), new RestProviderProperties.Apply());
        RestProviderResolvedConfig.TokenApplyLocation location = resolveApplyLocation(apply.getLocation());
        String name = value(apply.getName(), defaultApplyName(location, auth));
        String format = value(apply.getFormat(), defaultApplyFormat(auth));
        return new RestProviderResolvedConfig.TokenApply(location, name, format);
    }

    private RestProviderResolvedConfig.TokenApplyLocation resolveApplyLocation(String value) {
        String location = StringUtils.defaultIfBlank(value, "header").trim().toUpperCase(Locale.ROOT);
        try {
            return RestProviderResolvedConfig.TokenApplyLocation.valueOf(location);
        } catch (Exception ignored) {
            throw new IllegalArgumentException("Unsupported token apply location: " + value + ". Allowed: HEADER, BODY, QUERY");
        }
    }

    private String defaultApplyName(RestProviderResolvedConfig.TokenApplyLocation location, RestProviderResolvedConfig.Auth auth) {
        if (location == RestProviderResolvedConfig.TokenApplyLocation.HEADER) {
            return StringUtils.defaultIfBlank(auth.headerName(), "Authorization");
        }
        return "accessToken";
    }

    private String defaultApplyFormat(RestProviderResolvedConfig.Auth auth) {
        if (auth.type() == RestProviderResolvedConfig.AuthType.API_KEY && StringUtils.isBlank(auth.prefix())) {
            return "{accessToken}";
        }
        if (StringUtils.isNotBlank(auth.prefix())) {
            return auth.prefix() + " {accessToken}";
        }
        if (auth.type() == RestProviderResolvedConfig.AuthType.JWT) {
            return "{tokenType} {accessToken}";
        }
        return "{tokenType} {accessToken}";
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

    private static String value(String value, String fallback) {
        return StringUtils.defaultIfBlank(value, fallback);
    }
}
