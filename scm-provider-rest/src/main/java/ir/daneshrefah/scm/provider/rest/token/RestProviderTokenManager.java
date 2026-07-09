package ir.daneshrefah.scm.provider.rest.token;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.MissingNode;
import ir.daneshrefah.scm.cache.starter.connector.spring.TtlAwareCache;
import ir.daneshrefah.scm.cache.starter.utility.lock.LockAcquireFailedException;
import ir.daneshrefah.scm.cache.starter.utility.lock.LockUtility;
import ir.daneshrefah.scm.common.provider.message.ProviderMessageCustomizerContext;
import ir.daneshrefah.scm.provider.rest.config.RestProviderResolvedConfig;
import ir.daneshrefah.scm.provider.rest.customizer.RestAuthUrlProviderMessageCustomizerConfig;
import ir.daneshrefah.scm.provider.rest.exception.RestProviderAuthException;
import ir.daneshrefah.scm.provider.rest.exception.RestProviderAuthFault;
import ir.daneshrefah.scm.provider.rest.http.RestProviderClientRegistry;
import ir.daneshrefah.scm.provider.rest.metrics.RestProviderMetrics;
import ir.daneshrefah.scm.provider.rest.model.RestProviderRequestSpec;
import ir.daneshrefah.scm.provider.rest.trace.RestProviderTraceSupport;
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

@Component
@RequiredArgsConstructor
@Slf4j
public class RestProviderTokenManager implements ProviderAuthTokenProvider {
    private final RestProviderClientRegistry clientRegistry;
    private final ObjectMapper objectMapper;
    private final ObjectProvider<CacheManager> cacheManagerProvider;
    private final ObjectProvider<LockUtility> lockUtilityProvider;
    private final RestProviderMetrics metrics;
    private final RestProviderTraceSupport traceSupport;

    @Override
    public ProviderAuthToken resolveToken(
            RestProviderResolvedConfig providerConfig,
            RestAuthUrlProviderMessageCustomizerConfig authConfig,
            ProviderMessageCustomizerContext context
    ) {
        if (providerConfig == null) {
            throw new RestProviderAuthException(RestProviderAuthFault.PROVIDER_AUTH_FAILED, "",
                    "REST provider configuration is unavailable");
        }
        if (authConfig == null) {
            throw authException(RestProviderAuthFault.PROVIDER_AUTH_FAILED, providerConfig,
                    "REST auth-url configuration is unavailable for provider " + providerConfig.provider());
        }

        String cacheKey = cacheKey(providerConfig, authConfig, context);
        TokenCacheEntry cached = readToken(providerConfig, authConfig, cacheKey);
        long now = System.currentTimeMillis();
        if (isUsable(cached, authConfig, now)) {
            log.debug("REST auth-url token cache hit provider={} service={} operation={} channel={} authProfile={}",
                    providerConfig.provider(), serviceCode(context), operationCode(context), channelCode(context),
                    authConfig.cache().getAuthProfile());
            metrics.provider(providerConfig.provider()).tokenCacheHit(providerConfig, context);
            traceTokenEvent("provider.auth.cache.hit", providerConfig, authConfig, context);
            return new ProviderAuthToken(cached.accessToken(), cached.tokenType());
        }

        log.debug("REST auth-url token cache miss provider={} service={} operation={} channel={} authProfile={}",
                providerConfig.provider(), serviceCode(context), operationCode(context), channelCode(context),
                authConfig.cache().getAuthProfile());
        metrics.provider(providerConfig.provider()).tokenCacheMiss(providerConfig, context);
        traceTokenEvent("provider.auth.cache.miss", providerConfig, authConfig, context);

        TokenCacheEntry refreshed = resolveMissingToken(providerConfig, authConfig, context, cacheKey);
        if (!isUsable(refreshed, authConfig, System.currentTimeMillis())) {
            throw authException(RestProviderAuthFault.PROVIDER_AUTH_TOKEN_UNAVAILABLE, providerConfig,
                    "Could not resolve valid access token for provider " + providerConfig.provider());
        }
        return new ProviderAuthToken(refreshed.accessToken(), refreshed.tokenType());
    }

    private TokenCacheEntry resolveMissingToken(
            RestProviderResolvedConfig providerConfig,
            RestAuthUrlProviderMessageCustomizerConfig authConfig,
            ProviderMessageCustomizerContext context,
            String cacheKey
    ) {
        String lockKey = lockKey(providerConfig, authConfig, context);
        LockUtility lockUtility = lockUtilityProvider.getIfAvailable();
        if (lockUtility == null) {
            throw authException(RestProviderAuthFault.PROVIDER_AUTH_LOCK_TIMEOUT, providerConfig,
                    "Distributed lock utility is unavailable for provider " + providerConfig.provider());
        }

        try {
            return lockUtility.executeWithLock(lockKey, authConfig.lock().waitTimeoutDuration(), () -> {
                metrics.provider(providerConfig.provider()).tokenLockAcquired(providerConfig, context);
                traceTokenEvent("provider.auth.lock.acquired", providerConfig, authConfig, context);
                log.debug("REST auth-url token refresh lock acquired provider={} authProfile={}",
                        providerConfig.provider(), authConfig.cache().getAuthProfile());
                TokenCacheEntry current = readToken(providerConfig, authConfig, cacheKey);
                if (isUsable(current, authConfig, System.currentTimeMillis())) {
                    log.debug("REST auth-url token cache hit after lock provider={} service={} operation={} channel={} authProfile={}",
                            providerConfig.provider(), serviceCode(context), operationCode(context), channelCode(context),
                            authConfig.cache().getAuthProfile());
                    metrics.provider(providerConfig.provider()).tokenCacheHit(providerConfig, context);
                    traceTokenEvent("provider.auth.cache.hit", providerConfig, authConfig, context);
                    return current;
                }
                return refreshToken(providerConfig, authConfig, context, cacheKey);
            });
        } catch (LockAcquireFailedException e) {
            metrics.provider(providerConfig.provider()).tokenLockTimeout(providerConfig, context);
            traceTokenEvent("provider.auth.lock.timeout", providerConfig, authConfig, context);
            log.warn("REST auth-url token refresh lock timeout provider={} waitTimeout={} authProfile={}",
                    providerConfig.provider(), authConfig.lock().waitTimeoutDuration(), authConfig.cache().getAuthProfile());
            TokenCacheEntry polled = pollTokenCache(providerConfig, authConfig, context, cacheKey);
            if (isUsable(polled, authConfig, System.currentTimeMillis())) {
                return polled;
            }
            throw new RestProviderAuthException(
                    RestProviderAuthFault.PROVIDER_AUTH_LOCK_TIMEOUT,
                    providerConfig.provider(),
                    "Could not acquire REST provider token refresh lock for provider " + providerConfig.provider(),
                    e);
        } catch (RestProviderAuthException e) {
            throw e;
        } catch (RuntimeException e) {
            log.error("REST auth-url distributed lock failure provider={} authProfile={}",
                    providerConfig.provider(), authConfig.cache().getAuthProfile(), e);
            throw new RestProviderAuthException(
                    RestProviderAuthFault.PROVIDER_AUTH_LOCK_TIMEOUT,
                    providerConfig.provider(),
                    "REST provider token refresh lock failed for provider " + providerConfig.provider(),
                    e);
        }
    }

    private TokenCacheEntry refreshToken(
            RestProviderResolvedConfig providerConfig,
            RestAuthUrlProviderMessageCustomizerConfig authConfig,
            ProviderMessageCustomizerContext context,
            String cacheKey
    ) {
        try {
            long startedAt = System.nanoTime();
            TokenCacheEntry fetched = fetchToken(providerConfig, authConfig, context);
            metrics.provider(providerConfig.provider()).recordTokenRequestDuration(
                    providerConfig,
                    context,
                    Duration.ofNanos(System.nanoTime() - startedAt));
            putToken(providerConfig, authConfig, context, cacheKey, fetched);
            traceTokenEvent("provider.auth.token.refresh", providerConfig, authConfig, context);
            log.info("REST auth-url token refreshed provider={} expiresInSeconds={} authProfile={}",
                    providerConfig.provider(), fetched.expiresInSeconds(), authConfig.cache().getAuthProfile());
            metrics.provider(providerConfig.provider()).tokenRefresh(providerConfig, context);
            return fetched;
        } catch (RestProviderAuthException e) {
            metrics.provider(providerConfig.provider()).tokenRefreshFailure(providerConfig, context);
            throw e;
        } catch (RuntimeException e) {
            log.error("REST auth-url token refresh failed provider={} authProfile={}",
                    providerConfig.provider(), authConfig.cache().getAuthProfile(), e);
            metrics.provider(providerConfig.provider()).tokenRefreshFailure(providerConfig, context);
            throw new RestProviderAuthException(
                    RestProviderAuthFault.PROVIDER_AUTH_FAILED,
                    providerConfig.provider(),
                    "REST provider token refresh failed for provider " + providerConfig.provider(),
                    e);
        }
    }

    private TokenCacheEntry fetchToken(
            RestProviderResolvedConfig providerConfig,
            RestAuthUrlProviderMessageCustomizerConfig authConfig,
            ProviderMessageCustomizerContext context
    ) {
        traceTokenEvent("provider.auth.request", providerConfig, authConfig, context);
        HttpMethod method = resolveMethod(authConfig.getMethod());
        URI uri = resolveUri(providerConfig.baseUrl(), authConfig.getUrl(), authConfig.getPath(), authConfig.request().getQuery());

        Map<String, String> headers = new LinkedHashMap<>(safeStringMap(authConfig.request().getHeaders()));
        applyAuth(headers, authConfig.request().auth());
        Object body = resolveTokenRequestBody(authConfig, headers);
        RestProviderRequestSpec requestSpec = new RestProviderRequestSpec(method, uri, Map.copyOf(headers), body);

        ResponseEntity<String> response = clientRegistry.exchange(providerConfig, requestSpec);
        int statusCode = response.getStatusCode().value();
        if (statusCode < 200 || statusCode >= 300) {
            throw authException(RestProviderAuthFault.PROVIDER_AUTH_FAILED, providerConfig,
                    "Token endpoint returned non-success status " + statusCode + " for provider " + providerConfig.provider());
        }

        String responseBody = response.getBody();
        if (StringUtils.isBlank(responseBody)) {
            throw authException(RestProviderAuthFault.PROVIDER_AUTH_INVALID_RESPONSE, providerConfig,
                    "Token endpoint returned empty body for provider " + providerConfig.provider());
        }

        JsonNode root;
        try {
            root = objectMapper.readTree(responseBody);
        } catch (Exception e) {
            throw new RestProviderAuthException(
                    RestProviderAuthFault.PROVIDER_AUTH_INVALID_RESPONSE,
                    providerConfig.provider(),
                    "Token endpoint body is not valid JSON for provider " + providerConfig.provider(),
                    e);
        }

        RestAuthUrlProviderMessageCustomizerConfig.Response responseConfig = authConfig.response();
        String accessToken = asText(resolvePath(root, responseConfig.getTokenField()));
        if (StringUtils.isBlank(accessToken)) {
            throw authException(RestProviderAuthFault.PROVIDER_AUTH_INVALID_RESPONSE, providerConfig,
                    "Token endpoint response does not contain configured token field for provider " + providerConfig.provider());
        }

        String tokenType = asText(resolvePath(root, responseConfig.getTokenTypeField()));
        if (StringUtils.isBlank(tokenType)) {
            tokenType = responseConfig.getDefaultTokenType();
        }

        int expiresInSeconds = asInt(resolvePath(root, responseConfig.getExpiresInField()),
                responseConfig.getDefaultExpiresInSeconds() == null ? 300 : responseConfig.getDefaultExpiresInSeconds());
        if (expiresInSeconds <= 0) {
            expiresInSeconds = responseConfig.getDefaultExpiresInSeconds() == null ? 300 : responseConfig.getDefaultExpiresInSeconds();
        }
        long expiresAtEpochMs = System.currentTimeMillis() + (expiresInSeconds * 1000L);
        return new TokenCacheEntry(accessToken, tokenType, expiresAtEpochMs, expiresInSeconds);
    }

    private Object resolveTokenRequestBody(
            RestAuthUrlProviderMessageCustomizerConfig authConfig,
            Map<String, String> headers
    ) {
        Map<String, String> form = safeStringMap(authConfig.request().getForm());
        if (!form.isEmpty()) {
            headers.putIfAbsent(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE);
            return encodeForm(form);
        }
        Map<String, Object> body = safeObjectMap(authConfig.request().getBody());
        if (!body.isEmpty()) {
            headers.putIfAbsent(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
            return body;
        }
        return null;
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

    private void applyAuth(Map<String, String> headers, RestAuthUrlProviderMessageCustomizerConfig.Auth auth) {
        if (auth == null || auth.authType() == RestAuthUrlProviderMessageCustomizerConfig.AuthType.NONE) {
            return;
        }
        if (hasHeader(headers, auth.getHeaderName())) {
            return;
        }
        String headerName = StringUtils.defaultIfBlank(auth.getHeaderName(), HttpHeaders.AUTHORIZATION);
        String value;
        switch (auth.authType()) {
            case BASIC -> {
                if (StringUtils.isBlank(auth.getUsername()) || StringUtils.isBlank(auth.getPassword())) {
                    throw new IllegalArgumentException("Token endpoint BASIC auth requires username and password");
                }
                String credentials = auth.getUsername() + ":" + auth.getPassword();
                if (auth.getBasicBase64() == null || auth.getBasicBase64()) {
                    credentials = java.util.Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
                }
                value = StringUtils.defaultIfBlank(auth.getPrefix(), "Basic") + " " + credentials;
            }
            case BEARER -> value = withPrefix(auth.getPrefix(), auth.getToken(), "Bearer");
            case NONE -> value = null;
            default -> throw new IllegalStateException("Unsupported auth type: " + auth.authType());
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
        safeStringMap(query).forEach((key, value) -> builder.queryParam(key, value));
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

    private boolean isUsable(
            TokenCacheEntry entry,
            RestAuthUrlProviderMessageCustomizerConfig authConfig,
            long nowEpochMs
    ) {
        if (entry == null || StringUtils.isBlank(entry.accessToken())) {
            return false;
        }
        long refreshThresholdMs = Math.max(authConfig.cache().refreshSkewDuration().toMillis(), 1000L);
        return (entry.expiresAtEpochMs() - nowEpochMs) > refreshThresholdMs;
    }

    private String cacheKey(
            RestProviderResolvedConfig providerConfig,
            RestAuthUrlProviderMessageCustomizerConfig authConfig,
            ProviderMessageCustomizerContext context
    ) {
        return String.join(":",
                safeKey(authConfig.cache().getKeyPrefix()),
                safeKey(providerConfig.provider()),
                safeKey(authConfig.cache().getAuthProfile()),
                safeKey(channelCode(context)),
                safeKey(authConfig.cache().getCredentialKey()));
    }

    private String lockKey(
            RestProviderResolvedConfig providerConfig,
            RestAuthUrlProviderMessageCustomizerConfig authConfig,
            ProviderMessageCustomizerContext context
    ) {
        return String.join(":",
                safeKey(authConfig.lock().getKeyPrefix()),
                safeKey(providerConfig.provider()),
                safeKey(authConfig.cache().getAuthProfile()),
                safeKey(channelCode(context)),
                safeKey(authConfig.cache().getCredentialKey()));
    }

    private TokenCacheEntry readToken(
            RestProviderResolvedConfig providerConfig,
            RestAuthUrlProviderMessageCustomizerConfig authConfig,
            String key
    ) {
        Cache cache = tokenCache(providerConfig, authConfig);
        try {
            Cache.ValueWrapper valueWrapper = cache.get(key);
            if (valueWrapper == null || valueWrapper.get() == null) {
                return null;
            }
            return convertToTokenEntry(valueWrapper.get());
        } catch (RuntimeException e) {
            log.error("REST auth-url centralized cache read failed provider={} cacheName={}",
                    providerConfig.provider(), authConfig.cache().getName(), e);
            throw new RestProviderAuthException(
                    RestProviderAuthFault.PROVIDER_AUTH_CACHE_ERROR,
                    providerConfig.provider(),
                    "REST provider token cache read failed for provider " + providerConfig.provider(),
                    e);
        }
    }

    private void putToken(
            RestProviderResolvedConfig providerConfig,
            RestAuthUrlProviderMessageCustomizerConfig authConfig,
            ProviderMessageCustomizerContext context,
            String key,
            TokenCacheEntry entry
    ) {
        Cache cache = tokenCache(providerConfig, authConfig);
        Duration ttl = tokenTtl(authConfig, entry);
        try {
            if (cache instanceof TtlAwareCache ttlAwareCache) {
                ttlAwareCache.put(key, entry, ttl);
            } else {
                cache.put(key, entry);
            }
            metrics.provider(providerConfig.provider()).tokenCachePut(providerConfig, context);
            traceTokenEvent("provider.auth.cache.put", providerConfig, authConfig, context);
        } catch (RuntimeException e) {
            log.error("REST auth-url centralized cache put failed provider={} cacheName={}",
                    providerConfig.provider(), authConfig.cache().getName(), e);
            throw new RestProviderAuthException(
                    RestProviderAuthFault.PROVIDER_AUTH_CACHE_ERROR,
                    providerConfig.provider(),
                    "REST provider token cache put failed for provider " + providerConfig.provider(),
                    e);
        }
    }

    private Cache tokenCache(
            RestProviderResolvedConfig providerConfig,
            RestAuthUrlProviderMessageCustomizerConfig authConfig
    ) {
        CacheManager cacheManager = cacheManagerProvider.getIfAvailable();
        if (cacheManager == null) {
            throw authException(RestProviderAuthFault.PROVIDER_AUTH_CACHE_ERROR, providerConfig,
                    "Centralized token cache manager is unavailable for provider " + providerConfig.provider());
        }
        Cache cache = cacheManager.getCache(authConfig.cache().getName());
        if (cache == null) {
            throw authException(RestProviderAuthFault.PROVIDER_AUTH_CACHE_ERROR, providerConfig,
                    "Centralized token cache is not configured for provider " + providerConfig.provider());
        }
        return cache;
    }

    private Duration tokenTtl(RestAuthUrlProviderMessageCustomizerConfig authConfig, TokenCacheEntry entry) {
        long ttlSeconds = Math.max(1, entry.expiresInSeconds() - authConfig.cache().ttlSkewDuration().toSeconds());
        return Duration.ofSeconds(ttlSeconds);
    }

    private TokenCacheEntry pollTokenCache(
            RestProviderResolvedConfig providerConfig,
            RestAuthUrlProviderMessageCustomizerConfig authConfig,
            ProviderMessageCustomizerContext context,
            String cacheKey
    ) {
        Duration waitTimeout = authConfig.lock().waitTimeoutDuration();
        Duration retryDelay = authConfig.lock().retryDelayDuration();
        long deadline = System.nanoTime() + Math.max(0, waitTimeout.toNanos());
        while (System.nanoTime() <= deadline) {
            TokenCacheEntry cached = readToken(providerConfig, authConfig, cacheKey);
            if (isUsable(cached, authConfig, System.currentTimeMillis())) {
                log.debug("REST auth-url token cache populated while waiting provider={} authProfile={}",
                        providerConfig.provider(), authConfig.cache().getAuthProfile());
                metrics.provider(providerConfig.provider()).tokenCacheHit(providerConfig, context);
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
        if (node.isTextual() || node.isNumber() || node.isBoolean()) {
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

    private Map<String, String> safeStringMap(Map<String, String> input) {
        Map<String, String> result = new LinkedHashMap<>();
        if (input != null) {
            input.forEach((key, value) -> {
                if (StringUtils.isNotBlank(key) && value != null) {
                    result.put(key, value);
                }
            });
        }
        return result;
    }

    private Map<String, Object> safeObjectMap(Map<String, Object> input) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (input != null) {
            input.forEach((key, value) -> {
                if (StringUtils.isNotBlank(key) && value != null) {
                    result.put(key, value);
                }
            });
        }
        return result;
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
            RestProviderResolvedConfig providerConfig,
            String message
    ) {
        return new RestProviderAuthException(fault, providerConfig.provider(), message);
    }

    private void traceTokenEvent(
            String eventName,
            RestProviderResolvedConfig providerConfig,
            RestAuthUrlProviderMessageCustomizerConfig authConfig,
            ProviderMessageCustomizerContext context
    ) {
        if (traceSupport != null) {
            traceSupport.tokenEvent(eventName, providerConfig, authConfig, context);
        }
    }

    record TokenCacheEntry(
            String accessToken,
            String tokenType,
            long expiresAtEpochMs,
            int expiresInSeconds
    ) implements Serializable {
    }
}
