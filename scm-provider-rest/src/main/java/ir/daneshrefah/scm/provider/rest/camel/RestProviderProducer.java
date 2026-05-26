package ir.daneshrefah.scm.provider.rest.camel;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.model.operation.Operation;
import ir.daneshrefah.scm.provider.rest.config.RestProviderConfigResolver;
import ir.daneshrefah.scm.provider.rest.config.RestProviderEndpointOverrides;
import ir.daneshrefah.scm.provider.rest.config.RestProviderHeaders;
import ir.daneshrefah.scm.provider.rest.config.RestProviderResolvedConfig;
import ir.daneshrefah.scm.provider.rest.http.RestProviderClientRegistry;
import ir.daneshrefah.scm.provider.rest.log.RestProviderLogSanitizer;
import ir.daneshrefah.scm.provider.rest.metrics.RestProviderMetrics;
import ir.daneshrefah.scm.provider.rest.model.RestProviderRequestEnvelope;
import ir.daneshrefah.scm.provider.rest.model.RestProviderRequestSpec;
import ir.daneshrefah.scm.provider.rest.token.RestProviderTokenManager;
import ir.daneshrefah.scm.provider.rest.trace.RestProviderTraceSupport;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.support.DefaultProducer;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Slf4j
public class RestProviderProducer extends DefaultProducer {
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };

    private static final Set<String> RESERVED_REQUEST_KEYS = Set.of("method", "url", "path", "headers", "query", "body", "auth");
    private static final Set<String> EXCLUDED_INBOUND_HEADERS = Set.of(
            "host",
            "content-length",
            "transfer-encoding",
            "connection",
            "authorization",
            "proxy-authorization",
            "restprovider",
            "restprovidermethod",
            "restproviderurl",
            "restproviderpath",
            "restprovidertimeoutms",
            "scmoperationprovidername",
            "scmoperationprovideruri"
    );

    private final RestProviderEndpoint endpoint;
    private RestProviderConfigResolver configResolver;
    private RestProviderClientRegistry clientRegistry;
    private RestProviderMetrics metrics;
    private RestProviderTraceSupport traceSupport;
    private RestProviderLogSanitizer logSanitizer;
    private RestProviderTokenManager tokenManager;
    private ObjectMapper objectMapper;

    public RestProviderProducer(RestProviderEndpoint endpoint) {
        super(endpoint);
        this.endpoint = endpoint;
    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();
        configResolver = bean(RestProviderConfigResolver.class);
        clientRegistry = bean(RestProviderClientRegistry.class);
        metrics = bean(RestProviderMetrics.class);
        traceSupport = bean(RestProviderTraceSupport.class);
        logSanitizer = bean(RestProviderLogSanitizer.class);
        tokenManager = bean(RestProviderTokenManager.class);
        objectMapper = bean(ObjectMapper.class);
    }

    @Override
    public void process(Exchange exchange) {
        traceSupport.enrichLogMdc(exchange);
        String provider = resolveProvider(exchange);
        RestProviderResolvedConfig config = configResolver.resolve(provider, overrides(exchange));
        RestProviderRequestSpec requestSpec = buildRequestSpec(exchange, config);
        String operationName = resolveOperationName(exchange);

        RestProviderMetrics.CounterSet providerMetrics = metrics.provider(config.provider());
        providerMetrics.submitted();

        logRequest(config, operationName, requestSpec);

        long startedAt = System.nanoTime();
        try {
            ResponseEntity<String> response = traceSupport.clientSpan(
                    exchange,
                    config,
                    requestSpec,
                    () -> clientRegistry.exchange(config, requestSpec)
            );
            long elapsedMs = Duration.ofNanos(System.nanoTime() - startedAt).toMillis();
            providerMetrics.addLatency(elapsedMs);
            classifyResponse(response.getStatusCode().value(), providerMetrics);

            Map<String, Object> result = buildResponseBody(response);
            exchange.getMessage().setHeader(Exchange.HTTP_RESPONSE_CODE, response.getStatusCode().value());
            exchange.getMessage().setBody(result);

            logResponse(config, operationName, requestSpec, response, result, elapsedMs);
        } catch (RuntimeException e) {
            providerMetrics.failed();
            if (isTimeout(e)) {
                providerMetrics.timedOut();
            }
            long elapsedMs = Duration.ofNanos(System.nanoTime() - startedAt).toMillis();
            log.warn(
                    "REST ERROR provider={} operation={} method={} url={} elapsedMs={} message={}",
                    config.provider(),
                    operationName,
                    requestSpec.method(),
                    requestSpec.uri(),
                    elapsedMs,
                    e.getMessage()
            );
            throw e;
        }
    }

    private String resolveProvider(Exchange exchange) {
        String headerProvider = exchange.getMessage().getHeader(RestProviderHeaders.PROVIDER, String.class);
        if (StringUtils.isNotBlank(headerProvider)) {
            return headerProvider;
        }
        String operationProvider = exchange.getMessage().getHeader("scmOperationProviderName", String.class);
        if (StringUtils.isNotBlank(operationProvider)) {
            return operationProvider;
        }
        if (StringUtils.isNotBlank(endpoint.getProvider())) {
            return endpoint.getProvider();
        }
        String remaining = StringUtils.trimToNull(endpoint.getRemaining());
        if (remaining != null && !"request".equalsIgnoreCase(remaining)) {
            return remaining;
        }
        throw new IllegalArgumentException("REST provider is not specified");
    }

    private RestProviderEndpointOverrides overrides(Exchange exchange) {
        Integer timeout = first(
                exchange.getMessage().getHeader(RestProviderHeaders.TIMEOUT_MS, Integer.class),
                endpoint.getTimeoutMs()
        );
        return new RestProviderEndpointOverrides(timeout);
    }

    private RestProviderRequestSpec buildRequestSpec(Exchange exchange, RestProviderResolvedConfig config) {
        Object body = exchange.getMessage().getBody();
        RestProviderRequestEnvelope envelope = toEnvelope(body);

        String methodValue = first(
                exchange.getMessage().getHeader(RestProviderHeaders.METHOD, String.class),
                first(envelope.getMethod(), first(endpoint.getMethod(), config.defaultMethod()))
        );
        HttpMethod method = resolveMethod(methodValue);

        String absoluteUrl = first(
                exchange.getMessage().getHeader(RestProviderHeaders.URL, String.class),
                envelope.getUrl()
        );
        String path = first(
                exchange.getMessage().getHeader(RestProviderHeaders.PATH, String.class),
                envelope.getPath()
        );

        URI uri = resolveUri(config.baseUrl(), absoluteUrl, path, envelope.getQuery());

        Map<String, String> headers = new LinkedHashMap<>();
        headers.putAll(config.defaultHeaders());
        headers.putAll(extractInboundHeaders(exchange.getMessage().getHeaders()));
        headers.putAll(stringMap(envelope.getHeaders()));
        removeRequestAuthHeaders(headers, config.provider());
        if (envelope.getAuth() != null) {
            log.warn("Ignoring request-level auth override for REST provider. provider={}", config.provider());
        }
        applyAuth(headers, config);
        ensureContentType(headers, envelope.getBody());

        Object requestBody = envelope.getBody();
        if (requestBody == null && body != null && !looksLikeEnvelope(body)) {
            requestBody = body;
        }

        return new RestProviderRequestSpec(method, uri, Map.copyOf(headers), requestBody);
    }

    private URI resolveUri(String baseUrl, String absoluteUrl, String path, Map<String, Object> query) {
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

        Map<String, Object> queryMap = query == null ? Map.of() : query;
        queryMap.forEach((key, value) -> appendQuery(builder, key, value));
        return builder.build().encode().toUri();
    }

    private void appendQuery(UriComponentsBuilder builder, String key, Object value) {
        if (StringUtils.isBlank(key) || value == null) {
            return;
        }
        if (value instanceof Collection<?> collection) {
            collection.forEach(item -> builder.queryParam(key, item));
            return;
        }
        builder.queryParam(key, value);
    }

    private void applyAuth(Map<String, String> headers, RestProviderResolvedConfig config) {
        RestProviderResolvedConfig.Auth auth = config.auth();
        if (auth == null) {
            return;
        }
        if (config.token() != null && config.token().enabled() && requiresSharedToken(auth.type())) {
            RestProviderTokenManager.TokenValue tokenValue = tokenManager.resolveToken(config);
            String resolvedPrefix = auth.prefix();
            if ((auth.type() == RestProviderResolvedConfig.AuthType.BEARER
                    || auth.type() == RestProviderResolvedConfig.AuthType.JWT)
                    && StringUtils.isBlank(resolvedPrefix)) {
                resolvedPrefix = tokenValue.tokenType();
            }
            auth = new RestProviderResolvedConfig.Auth(
                    auth.type(),
                    auth.headerName(),
                    resolvedPrefix,
                    tokenValue.accessToken(),
                    auth.username(),
                    auth.password(),
                    auth.basicBase64()
            );
        }
        if (auth.type() == RestProviderResolvedConfig.AuthType.NONE) {
            return;
        }
        if (hasHeader(headers, auth.headerName())) {
            return;
        }
        String headerName = StringUtils.defaultIfBlank(auth.headerName(), HttpHeaders.AUTHORIZATION);
        String value = resolveAuthHeaderValue(auth);
        if (StringUtils.isNotBlank(value)) {
            headers.put(headerName, value);
        }
    }

    private boolean requiresSharedToken(RestProviderResolvedConfig.AuthType type) {
        return type == RestProviderResolvedConfig.AuthType.BEARER
                || type == RestProviderResolvedConfig.AuthType.JWT
                || type == RestProviderResolvedConfig.AuthType.API_KEY;
    }

    private String resolveAuthHeaderValue(RestProviderResolvedConfig.Auth auth) {
        return switch (auth.type()) {
            case BASIC -> basicHeader(auth);
            case BEARER -> prefixed(auth.prefix(), auth.token(), "Bearer");
            case JWT -> prefixed(auth.prefix(), auth.token(), "JWT");
            case API_KEY -> prefixed(auth.prefix(), auth.token(), null);
            case NONE -> null;
        };
    }

    private String basicHeader(RestProviderResolvedConfig.Auth auth) {
        if (StringUtils.isBlank(auth.username()) || StringUtils.isBlank(auth.password())) {
            throw new IllegalArgumentException("REST provider BASIC auth requires username and password");
        }
        String credentials = auth.username() + ":" + auth.password();
        if (auth.basicBase64()) {
            credentials = java.util.Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
        }
        String prefix = StringUtils.defaultIfBlank(auth.prefix(), "Basic");
        return prefix + " " + credentials;
    }

    private String prefixed(String prefix, String token, String defaultPrefix) {
        if (StringUtils.isBlank(token)) {
            throw new IllegalArgumentException("REST provider auth token is empty");
        }
        String resolvedPrefix = first(prefix, defaultPrefix);
        if (StringUtils.isBlank(resolvedPrefix)) {
            return token;
        }
        return resolvedPrefix + " " + token;
    }

    private Map<String, Object> buildResponseBody(ResponseEntity<String> response) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("status", response.getStatusCode().value());
        payload.put("headers", flattenHeaders(response.getHeaders()));
        payload.put("body", parseBodyByContentType(response.getBody(), response.getHeaders()));
        return payload;
    }

    private Object parseBodyByContentType(String body, HttpHeaders headers) {
        if (body == null) {
            return null;
        }
        String contentType = headers.getFirst(HttpHeaders.CONTENT_TYPE);
        if (contentType != null && contentType.toLowerCase(Locale.ROOT).contains(MediaType.APPLICATION_JSON_VALUE)) {
            try {
                return objectMapper.readValue(body, Object.class);
            } catch (Exception ignored) {
                return body;
            }
        }
        return body;
    }

    private Map<String, Object> flattenHeaders(HttpHeaders headers) {
        Map<String, Object> result = new LinkedHashMap<>();
        headers.forEach((key, values) -> {
            if (values == null || values.isEmpty()) {
                return;
            }
            if (values.size() == 1) {
                result.put(key, values.getFirst());
            } else {
                result.put(key, values);
            }
        });
        return result;
    }

    private void classifyResponse(int statusCode, RestProviderMetrics.CounterSet counterSet) {
        if (statusCode >= 200 && statusCode < 300) {
            counterSet.succeeded();
            return;
        }
        counterSet.failed();
        if (statusCode >= 400 && statusCode < 500) {
            counterSet.clientError();
            return;
        }
        if (statusCode >= 500) {
            counterSet.serverError();
        }
    }

    private boolean isTimeout(Throwable error) {
        Throwable current = error;
        while (current != null) {
            String className = current.getClass().getName();
            if (className.contains("Timeout")) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private void logRequest(RestProviderResolvedConfig config, String operationName, RestProviderRequestSpec requestSpec) {
        if (!log.isInfoEnabled()) {
            return;
        }
        log.info(
                "REST SEND provider={} operation={} method={} url={} headers={} body={}",
                config.provider(),
                operationName,
                requestSpec.method(),
                requestSpec.uri(),
                logSanitizer.sanitizeHeaders(requestSpec.headers(), config.security()),
                logSanitizer.sanitizeBody(requestSpec.body(), config.security())
        );
    }

    private void logResponse(
            RestProviderResolvedConfig config,
            String operationName,
            RestProviderRequestSpec requestSpec,
            ResponseEntity<String> response,
            Map<String, Object> responsePayload,
            long elapsedMs
    ) {
        if (!log.isInfoEnabled()) {
            return;
        }
        Object body = responsePayload.get("body");
        Object safeBody = body;
        if (body instanceof String text) {
            safeBody = logSanitizer.sanitizeBody(logSanitizer.parseJsonIfPossible(text), config.security());
        } else {
            safeBody = logSanitizer.sanitizeBody(body, config.security());
        }
        log.info(
                "REST RECEIVE provider={} operation={} method={} url={} status={} elapsedMs={} headers={} body={}",
                config.provider(),
                operationName,
                requestSpec.method(),
                requestSpec.uri(),
                response.getStatusCode().value(),
                elapsedMs,
                logSanitizer.sanitizeHeaders(flattenHeaders(response.getHeaders()), config.security()),
                safeBody
        );
    }

    private RestProviderRequestEnvelope toEnvelope(Object body) {
        if (body == null) {
            return new RestProviderRequestEnvelope();
        }
        if (body instanceof RestProviderRequestEnvelope envelope) {
            return envelope;
        }

        Map<String, Object> map = toMap(body);
        if (map.isEmpty()) {
            RestProviderRequestEnvelope envelope = new RestProviderRequestEnvelope();
            envelope.setBody(body);
            return envelope;
        }
        if (!looksLikeEnvelope(map)) {
            RestProviderRequestEnvelope envelope = new RestProviderRequestEnvelope();
            envelope.setBody(map);
            return envelope;
        }
        RestProviderRequestEnvelope envelope = objectMapper.convertValue(map, RestProviderRequestEnvelope.class);
        if (envelope.getBody() == null && map.containsKey("body")) {
            envelope.setBody(map.get("body"));
        }
        return envelope;
    }

    private boolean looksLikeEnvelope(Object body) {
        if (!(body instanceof Map<?, ?> map)) {
            return false;
        }
        for (Object key : map.keySet()) {
            if (key == null) {
                continue;
            }
            if (RESERVED_REQUEST_KEYS.contains(String.valueOf(key).toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    private Map<String, Object> toMap(Object body) {
        if (body == null) {
            return Map.of();
        }
        if (body instanceof Map<?, ?> map) {
            return objectMapper.convertValue(map, MAP_TYPE);
        }
        if (body instanceof String text) {
            if (StringUtils.isBlank(text)) {
                return Map.of();
            }
            try {
                return objectMapper.readValue(text, MAP_TYPE);
            } catch (Exception e) {
                throw new IllegalArgumentException("REST provider body must be a JSON object or envelope map", e);
            }
        }
        try {
            return objectMapper.convertValue(body, MAP_TYPE);
        } catch (IllegalArgumentException ignored) {
            return Map.of();
        }
    }

    private HttpMethod resolveMethod(String methodValue) {
        String normalized = StringUtils.defaultIfBlank(methodValue, "POST").trim().toUpperCase(Locale.ROOT);
        try {
            return HttpMethod.valueOf(normalized);
        } catch (Exception e) {
            throw new IllegalArgumentException("Unsupported REST method: " + methodValue);
        }
    }

    private Map<String, String> extractInboundHeaders(Map<String, Object> headers) {
        Map<String, String> result = new LinkedHashMap<>();
        headers.forEach((key, value) -> {
            if (StringUtils.isBlank(key) || value == null) {
                return;
            }
            if (StringUtils.startsWithIgnoreCase(key, "Camel")) {
                return;
            }
            if (StringUtils.startsWithIgnoreCase(key, "org.apache.camel")) {
                return;
            }
            if (EXCLUDED_INBOUND_HEADERS.contains(key.toLowerCase(Locale.ROOT))) {
                return;
            }
            result.put(key, String.valueOf(value));
        });
        return result;
    }

    private Map<String, String> stringMap(Map<String, Object> input) {
        if (input == null || input.isEmpty()) {
            return Map.of();
        }
        Map<String, String> result = new LinkedHashMap<>();
        input.forEach((key, value) -> {
            if (StringUtils.isBlank(key) || value == null) {
                return;
            }
            result.put(key, String.valueOf(value));
        });
        return result;
    }

    private void removeRequestAuthHeaders(Map<String, String> headers, String provider) {
        if (headers == null || headers.isEmpty()) {
            return;
        }
        boolean removedAuthorization = removeHeaderIgnoreCase(headers, HttpHeaders.AUTHORIZATION);
        boolean removedProxyAuthorization = removeHeaderIgnoreCase(headers, HttpHeaders.PROXY_AUTHORIZATION);
        if (removedAuthorization || removedProxyAuthorization) {
            log.warn("Ignoring request-level authorization headers for REST provider. provider={}", provider);
        }
    }

    private boolean removeHeaderIgnoreCase(Map<String, String> headers, String headerName) {
        String target = headerName.toLowerCase(Locale.ROOT);
        String keyToRemove = null;
        for (String key : headers.keySet()) {
            if (key != null && key.toLowerCase(Locale.ROOT).equals(target)) {
                keyToRemove = key;
                break;
            }
        }
        if (keyToRemove == null) {
            return false;
        }
        headers.remove(keyToRemove);
        return true;
    }

    private boolean hasHeader(Map<String, String> headers, String headerName) {
        if (StringUtils.isBlank(headerName)) {
            return false;
        }
        return headers.keySet().stream().anyMatch(key -> Objects.equals(key.toLowerCase(Locale.ROOT), headerName.toLowerCase(Locale.ROOT)));
    }

    private void ensureContentType(Map<String, String> headers, Object body) {
        if (body == null) {
            return;
        }
        if (hasHeader(headers, HttpHeaders.CONTENT_TYPE)) {
            return;
        }
        if (body instanceof String) {
            return;
        }
        headers.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
    }

    private String resolveOperationName(Exchange exchange) {
        Operation operation = exchange.getProperty(ir.daneshrefah.scm.common.model.message.Message.OPERATION, Operation.class);
        return operation != null ? operation.getName() : "";
    }

    private <T> T bean(Class<T> type) {
        T bean = getEndpoint().getCamelContext().getRegistry().findSingleByType(type);
        if (bean == null) {
            throw new IllegalStateException("No bean found for " + type.getName());
        }
        return bean;
    }

    private static <T> T first(T value, T fallback) {
        return value != null ? value : fallback;
    }
}
