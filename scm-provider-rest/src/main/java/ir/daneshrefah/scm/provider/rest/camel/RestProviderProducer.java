package ir.daneshrefah.scm.provider.rest.camel;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.opentelemetry.api.trace.Span;
import ir.daneshrefah.scm.common.model.gateway.Service;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.operation.Operation;
import ir.daneshrefah.scm.common.provider.message.ProviderExchange;
import ir.daneshrefah.scm.common.provider.message.ProviderMessageCustomizerContext;
import ir.daneshrefah.scm.common.provider.message.ProviderMessageCustomizerPipeline;
import ir.daneshrefah.scm.common.provider.message.ProviderRequest;
import ir.daneshrefah.scm.common.provider.message.ProviderResponse;
import ir.daneshrefah.scm.provider.rest.config.RestProviderConfigResolver;
import ir.daneshrefah.scm.provider.rest.config.RestProviderEndpointOverrides;
import ir.daneshrefah.scm.provider.rest.config.RestProviderHeaders;
import ir.daneshrefah.scm.provider.rest.config.RestProviderResolvedConfig;
import ir.daneshrefah.scm.provider.rest.http.RestProviderClientRegistry;
import ir.daneshrefah.scm.provider.rest.log.RestProviderLogSanitizer;
import ir.daneshrefah.scm.provider.rest.metrics.RestProviderMetrics;
import ir.daneshrefah.scm.provider.rest.model.RestProviderRequestEnvelope;
import ir.daneshrefah.scm.provider.rest.model.RestProviderRequestSpec;
import ir.daneshrefah.scm.provider.rest.ratelimit.RestProviderRateLimiter;
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
import java.time.Duration;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
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
            "restproviderratelimitenabled",
            "restproviderratelimitbucket",
            "restproviderratelimitkey",
            "scmoperationprovidername",
            "scmoperationprovideruri"
    );

    private final RestProviderEndpoint endpoint;
    private RestProviderConfigResolver configResolver;
    private RestProviderClientRegistry clientRegistry;
    private RestProviderMetrics metrics;
    private RestProviderRateLimiter rateLimiter;
    private RestProviderTraceSupport traceSupport;
    private RestProviderLogSanitizer logSanitizer;
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
        rateLimiter = bean(RestProviderRateLimiter.class);
        traceSupport = bean(RestProviderTraceSupport.class);
        logSanitizer = bean(RestProviderLogSanitizer.class);
        objectMapper = bean(ObjectMapper.class);
    }

    @Override
    public void process(Exchange exchange) {
        traceSupport.enrichLogMdc(exchange);
        String provider = resolveProvider(exchange);
        RestProviderResolvedConfig config = configResolver.resolve(provider, overrides(exchange));
        String operationName = resolveOperationName(exchange);
        ProviderRequest providerRequest = buildProviderRequest(exchange, config);
        ProviderMessageCustomizerContext customizerContext = customizerContext(exchange, config, operationName);
        ProviderExchange providerExchange = new ProviderExchange(providerRequest, customizerContext);
        ProviderMessageCustomizerPipeline customizerPipeline = config.messageCustomizerPipeline();
        logConfiguredCustomizers(customizerContext, customizerPipeline);

        RestProviderMetrics.CounterSet providerMetrics = metrics.provider(config.provider());
        providerMetrics.submitted();
        long startedAt = System.nanoTime();
        try {
            executeCustomizers(exchange, providerExchange, customizerPipeline, true);
            rateLimiter.acquire(config, operationName);
            RestProviderRequestSpec requestSpec = toRestRequestSpec(providerExchange, config);
            logRequest(config, operationName, requestSpec);
            ResponseEntity<String> response = traceSupport.clientSpan(
                    exchange,
                    config,
                    requestSpec,
                    () -> clientRegistry.exchange(config, requestSpec)
            );
            Duration elapsed = Duration.ofNanos(System.nanoTime() - startedAt);
            long elapsedMs = elapsed.toMillis();
            providerMetrics.addLatency(elapsedMs);
            classifyResponse(response.getStatusCode().value(), providerMetrics);
            providerMetrics.recordProviderRequestDuration(config, customizerContext, elapsed,
                    response.getStatusCode().is2xxSuccessful() ? "success" : "error");

            Map<String, Object> result = buildResponseBody(response);
            ProviderResponse providerResponse = new ProviderResponse(response.getStatusCode().value(), flattenHeaders(response.getHeaders()), result);
            providerResponse.nativeResponse(response);
            providerExchange.response(providerResponse);
            executeCustomizers(exchange, providerExchange, customizerPipeline, false);

            exchange.getMessage().setHeader(Exchange.HTTP_RESPONSE_CODE, response.getStatusCode().value());
            Object finalBody = providerExchange.response() != null ? providerExchange.response().body() : result;
            exchange.getMessage().setBody(finalBody);

            logResponse(config, operationName, requestSpec, response, finalBody, elapsedMs);
        } catch (RuntimeException e) {
            providerMetrics.failed();
            if (isTimeout(e)) {
                providerMetrics.timedOut();
            }
            providerMetrics.recordProviderRequestError(config, customizerContext);
            long elapsedMs = Duration.ofNanos(System.nanoTime() - startedAt).toMillis();
            log.warn(
                    "REST ERROR provider={} operation={} method={} url={} elapsedMs={} message={}",
                    config.provider(),
                    operationName,
                    providerExchange.request().method(),
                    providerExchange.request().uri(),
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
        return new RestProviderEndpointOverrides(
                timeout,
                first(exchange.getMessage().getHeader(RestProviderHeaders.RATE_LIMIT_ENABLED, Boolean.class), endpoint.getRateLimitEnabled()),
                first(exchange.getMessage().getHeader(RestProviderHeaders.RATE_LIMIT_BUCKET, String.class), endpoint.getRateLimitBucket()),
                first(exchange.getMessage().getHeader(RestProviderHeaders.RATE_LIMIT_KEY, String.class), endpoint.getRateLimitKey())
        );
    }

    private ProviderRequest buildProviderRequest(Exchange exchange, RestProviderResolvedConfig config) {
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
        ensureContentType(headers, envelope.getBody());

        Object requestBody = envelope.getBody();
        if (requestBody == null && body != null && !looksLikeEnvelope(body)) {
            requestBody = body;
        }

        return new ProviderRequest(method.name(), uri, headers, requestBody);
    }

    private RestProviderRequestSpec toRestRequestSpec(ProviderExchange providerExchange, RestProviderResolvedConfig config) {
        ProviderRequest request = providerExchange.request();
        URI uri = appendCustomizerQueryParameters(request.uri(), request.queryParameters());
        HttpMethod method = resolveMethod(request.method());
        return new RestProviderRequestSpec(method, uri, Map.copyOf(request.headers()), request.body());
    }

    private URI appendCustomizerQueryParameters(URI uri, Map<String, Object> queryParameters) {
        if (queryParameters == null || queryParameters.isEmpty()) {
            return uri;
        }
        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(uri);
        queryParameters.forEach((key, value) -> appendQuery(builder, key, value));
        return builder.build().encode().toUri();
    }

    private ProviderMessageCustomizerContext customizerContext(
            Exchange exchange,
            RestProviderResolvedConfig config,
            String operationName
    ) {
        return new ProviderMessageCustomizerContext(
                config.provider(),
                config.providerType(),
                serviceCode(exchange),
                operationName,
                channelCode(exchange),
                "rest",
                config.providerConfig(),
                config,
                correlationId(exchange),
                traceId(exchange)
        );
    }

    private void logConfiguredCustomizers(
            ProviderMessageCustomizerContext context,
            ProviderMessageCustomizerPipeline pipeline
    ) {
        if (!log.isDebugEnabled()) {
            return;
        }
        List<String> customizerNames = pipeline == null ? List.of() : pipeline.entries().stream()
                .map(entry -> entry.type() + "#" + entry.order())
                .toList();
        log.debug("REST provider customizers configured provider={} type={} service={} operation={} channel={} transport={} customizers={}",
                context.providerCode(),
                context.providerType(),
                context.serviceCode(),
                context.operationCode(),
                context.channelCode(),
                context.transportType(),
                customizerNames);
    }

    private void executeCustomizers(
            Exchange camelExchange,
            ProviderExchange providerExchange,
            ProviderMessageCustomizerPipeline pipeline,
            boolean beforeSend
    ) {
        if (pipeline == null || pipeline.isEmpty()) {
            return;
        }
        for (ProviderMessageCustomizerPipeline.Entry entry : pipeline.entries()) {
            try {
                traceSupport.customizerSpan(camelExchange, providerExchange.context(), entry.type(),
                        beforeSend ? "beforeSend" : "afterReceive",
                        () -> {
                            if (beforeSend) {
                                entry.customizer().beforeSend(providerExchange);
                            } else {
                                entry.customizer().afterReceive(providerExchange);
                            }
                        });
                metrics.provider(providerExchange.context().providerCode()).customizerExecution(providerExchange.context(), entry.type(), beforeSend ? "beforeSend" : "afterReceive");
            } catch (RuntimeException e) {
                metrics.provider(providerExchange.context().providerCode()).customizerError(providerExchange.context(), entry.type(), beforeSend ? "beforeSend" : "afterReceive");
                log.error("REST provider customizer error provider={} type={} service={} operation={} channel={} customizer={} phase={} message={}",
                        providerExchange.context().providerCode(),
                        providerExchange.context().providerType(),
                        providerExchange.context().serviceCode(),
                        providerExchange.context().operationCode(),
                        providerExchange.context().channelCode(),
                        entry.type(),
                        beforeSend ? "beforeSend" : "afterReceive",
                        e.getMessage(),
                        e);
                throw e;
            }
        }
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
            Object responsePayload,
            long elapsedMs
    ) {
        if (!log.isInfoEnabled()) {
            return;
        }
        Object body = responsePayload instanceof Map<?, ?> map ? map.get("body") : responsePayload;
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
        String operationName = exchange.getProperty(Message.OPERATION_NAME, String.class);
        if (StringUtils.isNotBlank(operationName)) {
            return operationName;
        }
        Operation operation = exchange.getProperty(Message.OPERATION, Operation.class);
        return operation != null ? operation.getName() : "";
    }

    private String serviceCode(Exchange exchange) {
        Service service = exchange.getProperty(Message.SERVICE, Service.class);
        if (service != null && StringUtils.isNotBlank(service.getCode())) {
            return service.getCode();
        }
        return StringUtils.defaultString(exchange.getMessage().getHeader("serviceCode", String.class));
    }

    private String channelCode(Exchange exchange) {
        String channelCode = exchange.getProperty(Message.CHANNEL_CODE, String.class);
        if (StringUtils.isNotBlank(channelCode)) {
            return channelCode;
        }
        return StringUtils.defaultString(exchange.getMessage().getHeader("channelCode", String.class));
    }

    private String correlationId(Exchange exchange) {
        String correlationId = exchange.getProperty(Message.CORRELATION_ID, String.class);
        if (StringUtils.isNotBlank(correlationId)) {
            return correlationId;
        }
        correlationId = exchange.getMessage().getHeader("X-Correlation-Id", String.class);
        if (StringUtils.isNotBlank(correlationId)) {
            return correlationId;
        }
        correlationId = exchange.getMessage().getHeader("X-SCM-Correlation-ID", String.class);
        return StringUtils.defaultString(correlationId);
    }

    private String traceId(Exchange exchange) {
        String traceId = exchange.getProperty(Message.TRACE_ID, String.class);
        if (StringUtils.isNotBlank(traceId)) {
            return traceId;
        }
        Span span = exchange.getProperty(Message.CURRENT_OPEN_TELEMETRY_SPAN, Span.class);
        if (span == null) {
            span = Span.current();
        }
        if (span != null && span.getSpanContext().isValid()) {
            return span.getSpanContext().getTraceId();
        }
        return "";
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
