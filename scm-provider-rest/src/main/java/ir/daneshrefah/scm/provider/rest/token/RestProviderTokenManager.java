package ir.daneshrefah.scm.provider.rest.token;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.MissingNode;
import ir.daneshrefah.scm.cache.client.connector.spring.TtlAwareCache;
import ir.daneshrefah.scm.cache.client.utility.lock.LockAcquireFailedException;
import ir.daneshrefah.scm.cache.client.utility.lock.LockUtility;
import ir.daneshrefah.scm.common.provider.message.ProviderMessageCustomizerContext;
import ir.daneshrefah.scm.provider.rest.config.RestProviderResolvedConfig;
import ir.daneshrefah.scm.provider.rest.exception.RestProviderAuthException;
import ir.daneshrefah.scm.provider.rest.exception.RestProviderAuthFault;
import ir.daneshrefah.scm.provider.rest.http.RestProviderClientRegistry;
import ir.daneshrefah.scm.provider.rest.metrics.RestProviderMetrics;
import ir.daneshrefah.scm.provider.rest.model.RestProviderRequestSpec;
import io.opentelemetry.api.trace.Span;
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

@Component
@RequiredArgsConstructor
@Slf4j
public class RestProviderTokenManager implements ProviderAuthTokenProvider {
    private final RestProviderClientRegistry clientRegistry;
    private final ObjectMapper objectMapper;
    private final ObjectProvider<CacheManager> cacheManagerProvider;
    private final ObjectProvider<LockUtility> lockUtilityProvider;
    private final RestProviderMetrics metrics;

    private final ConcurrentMap<String, TokenCacheEntry> localTokenCache = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, ReentrantLock> localLocks = new ConcurrentHashMap<>();

    public TokenValue resolveToken(RestProviderResolvedConfig config) {
        ProviderAuthToken token = resolveToken(config, null);
        return new TokenValue(token.accessToken(), token.tokenType());
    }

    @Override
    public ProviderAuthToken resolveToken(RestProviderResolvedConfig config, ProviderMessageCustomizerContext context) {
        RestProviderResolvedConfig.Token tokenConfig = config.token();
        if (tokenConfig == null || !tokenConfig.enabled()) {
            throw authException(RestProviderAuthFault.PROVIDER_AUTH_FAILED, config,
                    "Token flow is not enabled for provider " + config.provider());
        }

        String cacheKey = cacheKey(config, context);
        TokenCacheEntry cached = readToken(config, cacheKey);
        long now = System.currentTimeMillis();
        if (isUsable(cached, tokenConfig, now)) {
            log.debug("REST provider token cache hit provider={} service={} operation={} channel={} authProfile={}",
                    config.provider(), serviceCode(context), operationCode(context), channelCode(context), tokenConfig.authProfile());
            metrics.provider(config.provider()).tokenCacheHit();
            traceTokenEvent("provider.auth.cache.hit", config, context);
            return new ProviderAuthToken(cached.accessToken(), cached.tokenType());
        }
        log.debug("REST provider token cache miss provider={} service={} operation={} channel={} authProfile={}",
                config.provider(), serviceCode(context), operationCode(context), channelCode(context), tokenConfig.authProfile());
        metrics.provider(config.provider()).tokenCacheMiss();
        traceTokenEvent("provider.auth.cache.miss", config, context);

        TokenCacheEntry refreshed = resolveMissingToken(config, context, cacheKey);

        if (!isUsable(refreshed, tokenConfig, System.currentTimeMillis())) {
            throw authException(RestProviderAuthFault.PROVIDER_AUTH_TOKEN_UNAVAILABLE, config,
                    "Could not resolve valid access token for provider " + config.provider());
        }
        return new ProviderAuthToken(refreshed.accessToken(), refreshed.tokenType());
    }

    private TokenCacheEntry resolveMissingToken(
            RestProviderResolvedConfig config,
            ProviderMessageCustomizerContext context,
            String cacheKey
    ) {
        RestProviderResolvedConfig.Token tokenConfig = config.token();
        if (tokenConfig.lock() == null || !tokenConfig.lock().enabled()) {
            log.warn("REST provider token refresh is running without distributed lock provider={} authProfile={}",
                    config.provider(), tokenConfig.authProfile());
            return refreshToken(config, context, cacheKey);
        }

        String lockKey = lockKey(config, context);
        LockUtility lockUtility = lockUtilityProvider.getIfAvailable();
        if (lockUtility == null) {
            if (!centralizedTokenCache(config)) {
                return executeLocalSingleFlight(lockKey, () -> {
                    TokenCacheEntry current = readToken(config, cacheKey);
                    if (isUsable(current, tokenConfig, System.currentTimeMillis())) {
                        metrics.provider(config.provider()).tokenCacheHit();
                        return current;
                    }
                    return refreshToken(config, context, cacheKey);
                });
            }
            throw authException(RestProviderAuthFault.PROVIDER_AUTH_LOCK_TIMEOUT, config,
                    "Distributed lock utility is unavailable for provider " + config.provider());
        }

        try {
            return lockUtility.executeWithLock(lockKey, tokenConfig.lock().waitTimeout(), () -> {
                metrics.provider(config.provider()).tokenLockAcquired();
                traceTokenEvent("provider.auth.lock.acquired", config, context);
                log.debug("REST provider token refresh lock acquired provider={} lockKey={} authProfile={}",
                        config.provider(), lockKey, tokenConfig.authProfile());
                TokenCacheEntry current = readToken(config, cacheKey);
                long currentNow = System.currentTimeMillis();
                if (isUsable(current, tokenConfig, currentNow)) {
                    log.debug("REST provider token cache hit after lock provider={} service={} operation={} channel={} authProfile={}",
                            config.provider(), serviceCode(context), operationCode(context), channelCode(context), tokenConfig.authProfile());
                    metrics.provider(config.provider()).tokenCacheHit();
                    return current;
                }
                return refreshToken(config, context, cacheKey);
            });
        } catch (LockAcquireFailedException e) {
            metrics.provider(config.provider()).tokenLockTimeout();
            traceTokenEvent("provider.auth.lock.timeout", config, context);
            log.warn("REST provider token refresh lock timeout provider={} lockKey={} waitTimeout={} authProfile={}",
                    config.provider(), lockKey, tokenConfig.lock().waitTimeout(), tokenConfig.authProfile());
            TokenCacheEntry polled = pollTokenCache(config, cacheKey);
            if (isUsable(polled, tokenConfig, System.currentTimeMillis())) {
                return polled;
            }
            throw new RestProviderAuthException(
                    RestProviderAuthFault.PROVIDER_AUTH_LOCK_TIMEOUT,
                    config.provider(),
                    "Could not acquire REST provider token refresh lock for provider " + config.provider(),
                    e);
        } catch (RestProviderAuthException e) {
            throw e;
        } catch (RuntimeException e) {
            log.error("REST provider token distributed lock failure provider={} authProfile={}",
                    config.provider(), tokenConfig.authProfile(), e);
            throw new RestProviderAuthException(
                    RestProviderAuthFault.PROVIDER_AUTH_LOCK_TIMEOUT,
                    config.provider(),
                    "REST provider token refresh lock failed for provider " + config.provider(),
                    e);
        }
    }

    private TokenCacheEntry refreshToken(
            RestProviderResolvedConfig config,
            ProviderMessageCustomizerContext context,
            String cacheKey
    ) {
        try {
            long startedAt = System.nanoTime();
            TokenCacheEntry fetched = fetchToken(config, context);
            metrics.provider(config.provider()).addTokenRequestLatency(Duration.ofNanos(System.nanoTime() - startedAt).toMillis());
            putToken(config, context, cacheKey, fetched);
            traceTokenEvent("provider.auth.token.refresh", config, context);
            log.info("REST provider token refreshed provider={} expiresInSeconds={} authProfile={}",
                    config.provider(), fetched.expiresInSeconds(), config.token().authProfile());
            metrics.provider(config.provider()).tokenRefresh();
            return fetched;
        } catch (RestProviderAuthException e) {
            metrics.provider(config.provider()).tokenRefreshFailure();
            throw e;
        } catch (RuntimeException e) {
            log.error("REST provider token refresh failed provider={} authProfile={}",
                    config.provider(), config.token().authProfile(), e);
            metrics.provider(config.provider()).tokenRefreshFailure();
            throw new RestProviderAuthException(
                    RestProviderAuthFault.PROVIDER_AUTH_FAILED,
                    config.provider(),
                    "REST provider token refresh failed for provider " + config.provider(),
                    e);
        }
    }

    private TokenCacheEntry fetchToken(RestProviderResolvedConfig config, ProviderMessageCustomizerContext context) {
        RestProviderResolvedConfig.Token tokenConfig = config.token();
        traceTokenEvent("provider.auth.request", config, context);
        HttpMethod method = resolveMethod(tokenConfig.method());
        URI uri = resolveUri(config.baseUrl(), tokenConfig.url(), tokenConfig.path(), tokenConfig.query());

        Map<String, String> headers = new LinkedHashMap<>(tokenConfig.headers());
        applyAuth(headers, tokenConfig.auth());

        Object body = resolveTokenRequestBody(tokenConfig, headers);
        RestProviderRequestSpec requestSpec = new RestProviderRequestSpec(method, uri, Map.copyOf(headers), body, true);

        ResponseEntity<String> response = clientRegistry.exchange(config, requestSpec);
        int statusCode = response.getStatusCode().value();
        if (statusCode < 200 || statusCode >= 300) {
            throw authException(RestProviderAuthFault.PROVIDER_AUTH_FAILED, config,
                    "Token endpoint returned non-success status " + statusCode + " for provider " + config.provider());
        }

        String responseBody = response.getBody();
        if (StringUtils.isBlank(responseBody)) {
            throw authException(RestProviderAuthFault.PROVIDER_AUTH_INVALID_RESPONSE, config,
                    "Token endpoint returned empty body for provider " + config.provider());
        }

        JsonNode root;
        try {
            root = objectMapper.readTree(responseBody);
        } catch (Exception e) {
            throw new RestProviderAuthException(
                    RestProviderAuthFault.PROVIDER_AUTH_INVALID_RESPONSE,
                    config.provider(),
                    "Token endpoint body is not valid JSON for provider " + config.provider(),
                    e);
        }

        String accessToken = asText(resolvePath(root, tokenConfig.responseTokenField()));
        if (StringUtils.isBlank(accessToken)) {
            throw authException(RestProviderAuthFault.PROVIDER_AUTH_INVALID_RESPONSE, config,
                    "Token endpoint response does not contain configured token field for provider " + config.provider());
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
        long refreshThresholdMs = tokenConfig.cache() != null && tokenConfig.cache().refreshSkew() != null
                ? tokenConfig.cache().refreshSkew().toMillis()
                : tokenConfig.earlyRefreshSeconds() * 1000L;
        return (entry.expiresAtEpochMs() - nowEpochMs) > Math.max(refreshThresholdMs, 1000L);
    }

    private String cacheKey(RestProviderResolvedConfig config, ProviderMessageCustomizerContext context) {
        RestProviderResolvedConfig.Token token = config.token();
        String prefix = token.cache() == null ? "provider-token" : StringUtils.defaultIfBlank(token.cache().keyPrefix(), "provider-token");
        return String.join(":",
                prefix,
                safeKey(config.provider()),
                safeKey(token.authProfile()),
                safeKey(channelCode(context)),
                safeKey(token.credentialKey()));
    }

    private String lockKey(RestProviderResolvedConfig config, ProviderMessageCustomizerContext context) {
        RestProviderResolvedConfig.Token token = config.token();
        String prefix = token.lock() == null ? "provider-token-refresh-lock" : StringUtils.defaultIfBlank(token.lock().keyPrefix(), "provider-token-refresh-lock");
        return String.join(":",
                prefix,
                safeKey(config.provider()),
                safeKey(token.authProfile()),
                safeKey(channelCode(context)),
                safeKey(token.credentialKey()));
    }

    private TokenCacheEntry readToken(RestProviderResolvedConfig config, String key) {
        Cache cache = tokenCache(config);
        if (cache == null) {
            return localTokenCacheEnabled(config) ? localTokenCache.get(localKey(config.token().cacheName(), key)) : null;
        }
        try {
            Cache.ValueWrapper valueWrapper = cache.get(key);
            if (valueWrapper == null || valueWrapper.get() == null) {
                return null;
            }
            return convertToTokenEntry(valueWrapper.get());
        } catch (RuntimeException e) {
            log.error("REST provider token centralized cache read failed provider={} cacheName={}",
                    config.provider(), config.token().cacheName(), e);
            throw new RestProviderAuthException(
                    RestProviderAuthFault.PROVIDER_AUTH_CACHE_ERROR,
                    config.provider(),
                    "REST provider token cache read failed for provider " + config.provider(),
                    e);
        }
    }

    private void putToken(
            RestProviderResolvedConfig config,
            ProviderMessageCustomizerContext context,
            String key,
            TokenCacheEntry entry
    ) {
        Cache cache = tokenCache(config);
        if (cache == null) {
            if (localTokenCacheEnabled(config)) {
                localTokenCache.put(localKey(config.token().cacheName(), key), entry);
                metrics.provider(config.provider()).tokenCachePut();
                traceTokenEvent("provider.auth.cache.put", config, context);
            }
            return;
        }
        Duration ttl = tokenTtl(config, entry);
        try {
            if (cache instanceof TtlAwareCache ttlAwareCache) {
                ttlAwareCache.put(key, entry, ttl);
                metrics.provider(config.provider()).tokenCachePut();
                traceTokenEvent("provider.auth.cache.put", config, context);
                return;
            }
            cache.put(key, entry);
            metrics.provider(config.provider()).tokenCachePut();
            traceTokenEvent("provider.auth.cache.put", config, context);
        } catch (RuntimeException e) {
            log.error("REST provider token centralized cache put failed provider={} cacheName={}",
                    config.provider(), config.token().cacheName(), e);
            throw new RestProviderAuthException(
                    RestProviderAuthFault.PROVIDER_AUTH_CACHE_ERROR,
                    config.provider(),
                    "REST provider token cache put failed for provider " + config.provider(),
                    e);
        }
    }

    private Cache tokenCache(RestProviderResolvedConfig config) {
        RestProviderResolvedConfig.Token token = config.token();
        if (token.cache() != null && !token.cache().enabled()) {
            return null;
        }
        CacheManager cacheManager = cacheManagerProvider.getIfAvailable();
        if (cacheManager == null) {
            if (centralizedTokenCache(config)) {
                throw authException(RestProviderAuthFault.PROVIDER_AUTH_CACHE_ERROR, config,
                        "Centralized token cache manager is unavailable for provider " + config.provider());
            }
            return null;
        }
        Cache cache = cacheManager.getCache(token.cacheName());
        if (cache == null && centralizedTokenCache(config)) {
            throw authException(RestProviderAuthFault.PROVIDER_AUTH_CACHE_ERROR, config,
                    "Centralized token cache is not configured for provider " + config.provider());
        }
        return cache;
    }

    private boolean centralizedTokenCache(RestProviderResolvedConfig config) {
        RestProviderResolvedConfig.TokenCache cache = config.token().cache();
        return cache == null || !"local".equalsIgnoreCase(cache.mode());
    }

    private boolean localTokenCacheEnabled(RestProviderResolvedConfig config) {
        RestProviderResolvedConfig.TokenCache cache = config.token().cache();
        return cache != null && cache.enabled() && "local".equalsIgnoreCase(cache.mode());
    }

    private Duration tokenTtl(RestProviderResolvedConfig config, TokenCacheEntry entry) {
        Duration ttlSkew = config.token().cache() == null ? Duration.ZERO : config.token().cache().ttlSkew();
        long ttlSeconds = Math.max(1, entry.expiresInSeconds() - (ttlSkew == null ? 0 : ttlSkew.toSeconds()));
        return Duration.ofSeconds(ttlSeconds);
    }

    private TokenCacheEntry pollTokenCache(RestProviderResolvedConfig config, String cacheKey) {
        RestProviderResolvedConfig.Token token = config.token();
        Duration waitTimeout = token.lock().waitTimeout() == null ? Duration.ZERO : token.lock().waitTimeout();
        Duration retryDelay = token.lock().retryDelay();
        long deadline = System.nanoTime() + Math.max(0, waitTimeout.toNanos());
        while (System.nanoTime() <= deadline) {
            TokenCacheEntry cached = readToken(config, cacheKey);
            if (isUsable(cached, token, System.currentTimeMillis())) {
                log.debug("REST provider token cache populated while waiting provider={} authProfile={}",
                        config.provider(), token.authProfile());
                metrics.provider(config.provider()).tokenCacheHit();
                return cached;
            }
            sleep(retryDelay);
        }
        return null;
    }

    private void sleep(Duration retryDelay) {
        long millis = retryDelay == null ? 100L : Math.max(1L, retryDelay.toMillis());
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for REST provider token cache", e);
        }
    }

    private TokenCacheEntry executeLocalSingleFlight(String lockKey, java.util.function.Supplier<TokenCacheEntry> action) {
        ReentrantLock localLock = localLocks.computeIfAbsent(lockKey, ignored -> new ReentrantLock());
        localLock.lock();
        try {
            return action.get();
        } finally {
            localLock.unlock();
        }
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

    private String safeKey(String value) {
        String normalized = StringUtils.defaultIfBlank(value, "default").trim();
        return normalized.replace(':', '_');
    }

    private String channelCode(ProviderMessageCustomizerContext context) {
        return context == null ? "default" : StringUtils.defaultIfBlank(context.channelCode(), "default");
    }

    private String serviceCode(ProviderMessageCustomizerContext context) {
        return context == null ? "" : StringUtils.defaultString(context.serviceCode());
    }

    private String operationCode(ProviderMessageCustomizerContext context) {
        return context == null ? "" : StringUtils.defaultString(context.operationCode());
    }

    private RestProviderAuthException authException(
            RestProviderAuthFault fault,
            RestProviderResolvedConfig config,
            String message
    ) {
        return new RestProviderAuthException(fault, config.provider(), message);
    }

    private void traceTokenEvent(String eventName, RestProviderResolvedConfig config, ProviderMessageCustomizerContext context) {
        Span span = Span.current();
        if (span == null || !span.getSpanContext().isValid()) {
            return;
        }
        span.addEvent(eventName);
        span.setAttribute("scm.provider.name", config.provider());
        span.setAttribute("scm.provider.service_code", serviceCode(context));
        span.setAttribute("scm.provider.operation_code", operationCode(context));
        span.setAttribute("scm.provider.channel_code", channelCode(context));
        span.setAttribute("scm.provider.transport_type", "rest");
        span.setAttribute("scm.provider.auth.profile", StringUtils.defaultString(config.token().authProfile()));
        if ("provider.auth.cache.hit".equals(eventName)) {
            span.setAttribute("scm.provider.auth.cache_hit", true);
        } else if ("provider.auth.cache.miss".equals(eventName)) {
            span.setAttribute("scm.provider.auth.cache_hit", false);
        } else if ("provider.auth.lock.acquired".equals(eventName)) {
            span.setAttribute("scm.provider.auth.lock_acquired", true);
        } else if ("provider.auth.lock.timeout".equals(eventName)) {
            span.setAttribute("scm.provider.auth.lock_acquired", false);
        } else if ("provider.auth.token.refresh".equals(eventName)) {
            span.setAttribute("scm.provider.auth.token_refreshed", true);
        }
    }

    public record TokenValue(String accessToken, String tokenType) {
    }

    record TokenCacheEntry(
            String accessToken,
            String tokenType,
            long expiresAtEpochMs,
            int expiresInSeconds
    ) implements Serializable {
    }
}
