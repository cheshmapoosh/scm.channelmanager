package ir.daneshrefah.scm.provider.rest.token;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.MissingNode;
import ir.daneshrefah.scm.cache.client.connector.spring.TtlAwareCache;
import ir.daneshrefah.scm.cache.client.utility.lock.LockUtility;
import ir.daneshrefah.scm.provider.rest.config.RestProviderResolvedConfig;
import ir.daneshrefah.scm.provider.rest.http.RestProviderClientRegistry;
import ir.daneshrefah.scm.provider.rest.metrics.RestProviderMetrics;
import ir.daneshrefah.scm.provider.rest.model.RestProviderRequestSpec;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.Serializable;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
@Slf4j
public class RestProviderTokenManager {
    private final RestProviderClientRegistry clientRegistry;
    private final ObjectMapper objectMapper;
    private final ObjectProvider<CacheManager> cacheManagerProvider;
    private final ObjectProvider<LockUtility> lockUtilityProvider;
    private final RestProviderMetrics metrics;

    private final ConcurrentMap<String, TokenCacheEntry> localTokenCache = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, ReentrantLock> localLocks = new ConcurrentHashMap<>();

    public TokenValue resolveToken(RestProviderResolvedConfig config) {
        RestProviderResolvedConfig.Token tokenConfig = config.token();
        if (tokenConfig == null || !tokenConfig.enabled()) {
            throw new IllegalStateException("Token flow is not enabled for provider " + config.provider());
        }

        String cacheKey = cacheKey(config);
        TokenCacheEntry cached = readToken(tokenConfig.cacheName(), cacheKey);
        long now = System.currentTimeMillis();
        if (isUsable(cached, tokenConfig, now)) {
            log.debug("REST provider token cache hit provider={}", config.provider());
            metrics.provider(config.provider()).tokenCacheHit();
            return new TokenValue(cached.accessToken(), cached.tokenType());
        }

        String lockName = tokenConfig.lockName() + "::" + config.provider();
        TokenCacheEntry refreshed = executeSingleFlight(lockName, () -> {
            TokenCacheEntry current = readToken(tokenConfig.cacheName(), cacheKey);
            long currentNow = System.currentTimeMillis();
            if (isUsable(current, tokenConfig, currentNow)) {
                log.debug("REST provider token cache hit after lock provider={}", config.provider());
                metrics.provider(config.provider()).tokenCacheHit();
                return current;
            }
            try {
                TokenCacheEntry fetched = fetchToken(config);
                putToken(tokenConfig.cacheName(), cacheKey, fetched);
                log.info("REST provider token refreshed provider={} expiresInSeconds={}", config.provider(), fetched.expiresInSeconds());
                metrics.provider(config.provider()).tokenRefresh();
                return fetched;
            } catch (RuntimeException e) {
                log.error("REST provider token refresh failed provider={}", config.provider(), e);
                metrics.provider(config.provider()).tokenRefreshFailure();
                throw e;
            }
        });

        if (!isUsable(refreshed, tokenConfig, System.currentTimeMillis())) {
            throw new IllegalStateException("Could not resolve valid access token for provider " + config.provider());
        }
        return new TokenValue(refreshed.accessToken(), refreshed.tokenType());
    }

    private TokenCacheEntry fetchToken(RestProviderResolvedConfig config) {
        RestProviderResolvedConfig.Token tokenConfig = config.token();
        HttpMethod method = resolveMethod(tokenConfig.method());
        URI uri = resolveUri(config.baseUrl(), tokenConfig.url(), tokenConfig.path(), tokenConfig.query());

        Map<String, String> headers = new LinkedHashMap<>(tokenConfig.headers());
        applyAuth(headers, tokenConfig.auth());

        Object body = resolveTokenRequestBody(tokenConfig, headers);
        RestProviderRequestSpec requestSpec = new RestProviderRequestSpec(method, uri, Map.copyOf(headers), body);

        ResponseEntity<String> response = clientRegistry.exchange(config, requestSpec);
        int statusCode = response.getStatusCode().value();
        if (statusCode < 200 || statusCode >= 300) {
            throw new IllegalStateException("Token endpoint returned non-success status " + statusCode + " for provider " + config.provider());
        }

        String responseBody = response.getBody();
        if (StringUtils.isBlank(responseBody)) {
            throw new IllegalStateException("Token endpoint returned empty body for provider " + config.provider());
        }

        JsonNode root;
        try {
            root = objectMapper.readTree(responseBody);
        } catch (Exception e) {
            throw new IllegalStateException("Token endpoint body is not valid JSON for provider " + config.provider(), e);
        }

        String accessToken = asText(resolvePath(root, tokenConfig.responseTokenField()));
        if (StringUtils.isBlank(accessToken)) {
            throw new IllegalStateException("Token endpoint response does not contain token field '" + tokenConfig.responseTokenField() +
                    "' for provider " + config.provider());
        }

        String tokenType = asText(resolvePath(root, tokenConfig.responseTokenTypeField()));
        if (StringUtils.isBlank(tokenType)) {
            tokenType = tokenConfig.defaultTokenType();
        }

        int expiresInSeconds = asInt(resolvePath(root, tokenConfig.responseExpiresInField()), tokenConfig.defaultExpiresInSeconds());
        if (expiresInSeconds <= 0) {
            expiresInSeconds = tokenConfig.defaultExpiresInSeconds();
        }
        long expiresAtEpochMs = System.currentTimeMillis() + (expiresInSeconds * 1000L);
        return new TokenCacheEntry(accessToken, tokenType, expiresAtEpochMs, expiresInSeconds);
    }

    private Object resolveTokenRequestBody(RestProviderResolvedConfig.Token tokenConfig, Map<String, String> headers) {
        if (tokenConfig.form() != null && !tokenConfig.form().isEmpty()) {
            headers.putIfAbsent(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE);
            return encodeForm(tokenConfig.form());
        }
        Object body = tokenConfig.body();
        if (hasBody(body)) {
            headers.putIfAbsent(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
            return body;
        }
        return null;
    }

    private boolean hasBody(Object body) {
        if (body == null) {
            return false;
        }
        if (body instanceof String text) {
            return StringUtils.isNotBlank(text);
        }
        if (body instanceof Map<?, ?> map) {
            return !map.isEmpty();
        }
        if (body instanceof Iterable<?> iterable) {
            return iterable.iterator().hasNext();
        }
        if (body.getClass().isArray()) {
            return java.lang.reflect.Array.getLength(body) > 0;
        }
        return true;
    }

    private String encodeForm(Map<String, String> form) {
        StringBuilder body = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> entry : form.entrySet()) {
            if (StringUtils.isBlank(entry.getKey()) || entry.getValue() == null) {
                continue;
            }
            if (!first) {
                body.append('&');
            }
            first = false;
            body.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8));
            body.append('=');
            body.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
        }
        return body.toString();
    }

    private void applyAuth(Map<String, String> headers, RestProviderResolvedConfig.Auth auth) {
        if (auth == null || auth.type() == null || auth.type() == RestProviderResolvedConfig.AuthType.NONE) {
            return;
        }
        if (hasHeader(headers, auth.headerName())) {
            return;
        }

        String headerName = StringUtils.defaultIfBlank(auth.headerName(), HttpHeaders.AUTHORIZATION);
        String value;
        switch (auth.type()) {
            case BASIC -> {
                if (StringUtils.isBlank(auth.username()) || StringUtils.isBlank(auth.password())) {
                    throw new IllegalArgumentException("Token endpoint BASIC auth requires username and password");
                }
                String credentials = auth.username() + ":" + auth.password();
                if (auth.basicBase64()) {
                    credentials = java.util.Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
                }
                String prefix = StringUtils.defaultIfBlank(auth.prefix(), "Basic");
                value = prefix + " " + credentials;
            }
            case BEARER -> value = withPrefix(auth.prefix(), auth.token(), "Bearer");
            case JWT -> value = withPrefix(auth.prefix(), auth.token(), "JWT");
            case API_KEY -> value = withPrefix(auth.prefix(), auth.token(), null);
            case NONE -> value = null;
            default -> throw new IllegalStateException("Unsupported auth type: " + auth.type());
        }
        if (StringUtils.isNotBlank(value)) {
            headers.put(headerName, value);
        }
    }

    private boolean hasHeader(Map<String, String> headers, String headerName) {
        if (StringUtils.isBlank(headerName)) {
            return false;
        }
        String target = headerName.toLowerCase(Locale.ROOT);
        return headers.keySet().stream().anyMatch(key -> key != null && key.toLowerCase(Locale.ROOT).equals(target));
    }

    private String withPrefix(String prefix, String token, String defaultPrefix) {
        if (StringUtils.isBlank(token)) {
            throw new IllegalArgumentException("Token endpoint auth token is empty");
        }
        String resolvedPrefix = StringUtils.defaultIfBlank(prefix, defaultPrefix);
        if (StringUtils.isBlank(resolvedPrefix)) {
            return token;
        }
        return resolvedPrefix + " " + token;
    }

    private URI resolveUri(String baseUrl, String absoluteUrl, String path, Map<String, String> query) {
        UriComponentsBuilder builder;
        String resolvedAbsolute = StringUtils.trimToNull(absoluteUrl);
        if (resolvedAbsolute != null) {
            builder = UriComponentsBuilder.fromUriString(resolvedAbsolute);
        } else {
            builder = UriComponentsBuilder.fromUriString(baseUrl);
            String normalizedPath = StringUtils.trimToNull(path);
            if (normalizedPath != null) {
                if (!normalizedPath.startsWith("/")) {
                    normalizedPath = "/" + normalizedPath;
                }
                builder.path(normalizedPath);
            }
        }
        Map<String, String> queryMap = query == null ? Map.of() : query;
        queryMap.forEach((key, value) -> {
            if (StringUtils.isBlank(key) || value == null) {
                return;
            }
            builder.queryParam(key, value);
        });
        return builder.build().encode().toUri();
    }

    private HttpMethod resolveMethod(String method) {
        String resolved = StringUtils.defaultIfBlank(method, "POST").trim().toUpperCase(Locale.ROOT);
        try {
            return HttpMethod.valueOf(resolved);
        } catch (Exception e) {
            throw new IllegalArgumentException("Unsupported token endpoint method: " + method, e);
        }
    }

    private boolean isUsable(TokenCacheEntry entry, RestProviderResolvedConfig.Token tokenConfig, long nowEpochMs) {
        if (entry == null || StringUtils.isBlank(entry.accessToken())) {
            return false;
        }
        long refreshThresholdMs = tokenConfig.earlyRefreshSeconds() * 1000L;
        return (entry.expiresAtEpochMs() - nowEpochMs) > Math.max(refreshThresholdMs, 1000L);
    }

    private String cacheKey(RestProviderResolvedConfig config) {
        RestProviderResolvedConfig.Token token = config.token();
        String baseKey = StringUtils.defaultIfBlank(token.cacheKey(), "access-token");
        return config.provider() + "::" + baseKey;
    }

    private TokenCacheEntry readToken(String cacheName, String key) {
        Cache cache = tokenCache(cacheName);
        if (cache == null) {
            return localTokenCache.get(localKey(cacheName, key));
        }
        Cache.ValueWrapper valueWrapper = cache.get(key);
        if (valueWrapper == null || valueWrapper.get() == null) {
            return null;
        }
        return convertToTokenEntry(valueWrapper.get());
    }

    private void putToken(String cacheName, String key, TokenCacheEntry entry) {
        Cache cache = tokenCache(cacheName);
        if (cache == null) {
            localTokenCache.put(localKey(cacheName, key), entry);
            return;
        }
        Duration ttl = Duration.ofSeconds(Math.max(1, entry.expiresInSeconds()));
        if (cache instanceof TtlAwareCache ttlAwareCache) {
            ttlAwareCache.put(key, entry, ttl);
            return;
        }
        cache.put(key, entry);
    }

    private Cache tokenCache(String cacheName) {
        CacheManager cacheManager = cacheManagerProvider.getIfAvailable();
        if (cacheManager == null) {
            return null;
        }
        return cacheManager.getCache(cacheName);
    }

    private TokenCacheEntry convertToTokenEntry(Object source) {
        if (source instanceof TokenCacheEntry entry) {
            return entry;
        }
        try {
            return objectMapper.convertValue(source, TokenCacheEntry.class);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private <T> T executeSingleFlight(String lockName, Supplier<T> action) {
        LockUtility lockUtility = lockUtilityProvider.getIfAvailable();
        if (lockUtility != null) {
            return lockUtility.executeWithLock(lockName, true, action::get);
        }

        ReentrantLock localLock = localLocks.computeIfAbsent(lockName, ignored -> new ReentrantLock());
        localLock.lock();
        try {
            return action.get();
        } finally {
            localLock.unlock();
        }
    }

    private JsonNode resolvePath(JsonNode source, String path) {
        if (source == null || StringUtils.isBlank(path)) {
            return source;
        }
        JsonNode current = source;
        for (String rawSegment : path.split("\\.")) {
            String segment = rawSegment.trim();
            if (segment.isEmpty()) {
                continue;
            }
            if (current == null || current.isMissingNode() || current.isNull()) {
                return MissingNode.getInstance();
            }
            current = current.path(segment);
        }
        return current;
    }

    private String asText(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return null;
        }
        if (node.isTextual()) {
            return node.asText();
        }
        if (node.isNumber() || node.isBoolean()) {
            return node.asText();
        }
        return null;
    }

    private int asInt(JsonNode node, int fallback) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return fallback;
        }
        if (node.isInt() || node.isLong() || node.isShort()) {
            return node.asInt();
        }
        if (node.isTextual()) {
            try {
                return Integer.parseInt(node.asText().trim());
            } catch (Exception ignored) {
                return fallback;
            }
        }
        return fallback;
    }

    private String localKey(String cacheName, String key) {
        return cacheName + "::" + key;
    }

    public record TokenValue(String accessToken, String tokenType) {
    }

    private record TokenCacheEntry(
            String accessToken,
            String tokenType,
            long expiresAtEpochMs,
            int expiresInSeconds
    ) implements Serializable {
    }
}
