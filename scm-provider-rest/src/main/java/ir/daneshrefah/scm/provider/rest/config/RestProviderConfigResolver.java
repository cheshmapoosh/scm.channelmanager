package ir.daneshrefah.scm.provider.rest.config;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class RestProviderConfigResolver {
    private final RestProviderProperties properties;

    public RestProviderResolvedConfig resolve(String provider, RestProviderEndpointOverrides overrides) {
        String providerName = normalizeProviderName(provider);
        if (providerName == null) {
            throw new IllegalArgumentException("REST provider is required");
        }

        RestProviderProperties.Instance instance = findProvider(providerName);
        RestProviderProperties.Instance defaults = nonNull(properties.getDefaults(), new RestProviderProperties.Instance());

        String baseUrl = StringUtils.trimToNull(first(instance.getBaseUrl(), defaults.getBaseUrl()));
        if (baseUrl == null) {
            throw new IllegalArgumentException("REST provider " + providerName + " must define baseUrl");
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

        Map<String, String> headers = mergeHeaders(defaults.getHeaders(), instance.getHeaders());
        RestProviderProperties.Auth mergedTokenAuth = nonNull(mergedToken.getAuth(), new RestProviderProperties.Auth());

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
                new RestProviderResolvedConfig.Proxy(
                        StringUtils.trimToNull(mergedProxy.getHost()),
                        mergedProxy.getPort(),
                        StringUtils.trimToNull(mergedProxy.getUsername()),
                        StringUtils.trimToNull(mergedProxy.getPassword())
                ),
                new RestProviderResolvedConfig.Auth(
                        resolveAuthType(mergedAuth.getType()),
                        StringUtils.defaultIfBlank(mergedAuth.getHeaderName(), "Authorization").trim(),
                        StringUtils.trimToNull(mergedAuth.getPrefix()),
                        StringUtils.trimToNull(mergedAuth.getToken()),
                        StringUtils.trimToNull(mergedAuth.getUsername()),
                        StringUtils.trimToNull(mergedAuth.getPassword()),
                        Boolean.TRUE.equals(first(mergedAuth.getBasicBase64(), Boolean.TRUE))
                ),
                new RestProviderResolvedConfig.Security(
                        listOf(mergedSecurity.getSensitiveHeaders()),
                        listOf(mergedSecurity.getSensitiveBodyKeys()),
                        value(mergedSecurity.getMaxBodyLogLength(), 400)
                ),
                new RestProviderResolvedConfig.Token(
                        Boolean.TRUE.equals(first(mergedToken.getEnabled(), Boolean.FALSE)),
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
                        value(mergedToken.getDefaultTokenType(), "Bearer")
                )
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

    private RestProviderProperties.Token mergeToken(RestProviderProperties.Token defaults, RestProviderProperties.Token instance) {
        RestProviderProperties.Token fallback = nonNull(defaults, new RestProviderProperties.Token());
        RestProviderProperties.Token item = nonNull(instance, new RestProviderProperties.Token());
        RestProviderProperties.Token result = new RestProviderProperties.Token();
        result.setEnabled(first(item.getEnabled(), fallback.getEnabled()));
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
        return result;
    }

    private Map<String, Object> mergeBody(Map<String, Object> defaults, Map<String, Object> instance) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.putAll(nonNull(defaults, Map.of()));
        body.putAll(nonNull(instance, Map.of()));
        body.entrySet().removeIf(entry -> StringUtils.isBlank(entry.getKey()) || entry.getValue() == null);
        return Map.copyOf(body);
    }

    private Map<String, String> mergeHeaders(Map<String, String> defaults, Map<String, String> instance) {
        Map<String, String> headers = new LinkedHashMap<>();
        headers.putAll(nonNull(defaults, Map.of()));
        headers.putAll(nonNull(instance, Map.of()));
        headers.entrySet().removeIf(entry -> StringUtils.isBlank(entry.getKey()) || entry.getValue() == null);
        return Map.copyOf(headers);
    }

    private static <T> T first(T value, T fallback) {
        return Objects.nonNull(value) ? value : fallback;
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
