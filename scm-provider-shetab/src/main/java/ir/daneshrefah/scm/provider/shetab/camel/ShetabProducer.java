package ir.daneshrefah.scm.provider.shetab.camel;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.model.operation.Operation;
import ir.daneshrefah.scm.provider.shetab.config.ShetabConfigResolver;
import ir.daneshrefah.scm.provider.shetab.config.ShetabEndpointOverrides;
import ir.daneshrefah.scm.provider.shetab.config.ShetabHeaders;
import ir.daneshrefah.scm.provider.shetab.config.ShetabResolvedConfig;
import ir.daneshrefah.scm.provider.shetab.iso.ShetabIsoMapConverter;
import ir.daneshrefah.scm.provider.shetab.metrics.ShetabProviderMetrics;
import ir.daneshrefah.scm.provider.shetab.ratelimit.ShetabRateLimiter;
import ir.daneshrefah.scm.provider.shetab.security.ShetabMessageSecurityProcessor;
import ir.daneshrefah.scm.provider.shetab.tcp.ShetabClientRegistry;
import ir.daneshrefah.scm.provider.shetab.trace.ShetabTraceSupport;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.support.DefaultProducer;
import org.apache.commons.lang3.StringUtils;
import org.jpos.iso.ISOMsg;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ShetabProducer extends DefaultProducer {
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };

    private final ShetabEndpoint endpoint;
    private ShetabConfigResolver configResolver;
    private ShetabIsoMapConverter isoMapConverter;
    private ShetabClientRegistry clientRegistry;
    private ShetabRateLimiter rateLimiter;
    private ShetabProviderMetrics metrics;
    private ShetabMessageSecurityProcessor securityProcessor;
    private ShetabTraceSupport traceSupport;
    private ObjectMapper objectMapper;

    public ShetabProducer(ShetabEndpoint endpoint) {
        super(endpoint);
        this.endpoint = endpoint;
    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();
        configResolver = bean(ShetabConfigResolver.class);
        isoMapConverter = bean(ShetabIsoMapConverter.class);
        clientRegistry = bean(ShetabClientRegistry.class);
        rateLimiter = bean(ShetabRateLimiter.class);
        metrics = bean(ShetabProviderMetrics.class);
        securityProcessor = bean(ShetabMessageSecurityProcessor.class);
        traceSupport = bean(ShetabTraceSupport.class);
        objectMapper = bean(ObjectMapper.class);
    }

    @Override
    public void process(Exchange exchange) {
        traceSupport.enrichLogMdc(exchange);
        String provider = resolveProvider(exchange);
        ShetabResolvedConfig config = configResolver.resolve(provider, overrides(exchange));
        Operation operation = exchange.getProperty(ir.daneshrefah.scm.common.model.message.Message.OPERATION, Operation.class);
        String operationName = operation != null ? operation.getName() : "";
        ShetabProviderMetrics.CounterSet providerMetrics = metrics.provider(config.provider());

        Map<String, Object> requestMap = bodyAsMap(exchange.getMessage().getBody());
        ISOMsg request = isoMapConverter.toIsoMsg(requestMap);
        securityProcessor.protectRequest(config, requestMap, request);

        log.info("Shetab provider start provider={} operation={} mti={}", config.provider(), operationName, requestMap.get("mti"));
        if (log.isDebugEnabled()) {
            log.debug("Shetab provider request provider={} operation={} body={}",
                    config.provider(), operationName, maskSensitive(requestMap));
        }
        long startedAt = System.nanoTime();
        try {
            ISOMsg response = traceSupport.clientSpan(exchange, config, request, () -> {
                rateLimiter.acquire(config, operationName);
                return clientRegistry.request(config, request);
            });
            securityProcessor.verifyResponse(config, response);
            Map<String, Object> responseMap = isoMapConverter.toMap(response);
            providerMetrics.succeeded();
            providerMetrics.addLatency(TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAt));
            if (log.isDebugEnabled()) {
                log.debug("Shetab provider response provider={} operation={} body={}",
                        config.provider(), operationName, maskSensitive(responseMap));
            }
            exchange.getMessage().setBody(responseMap);
            log.info("Shetab provider done provider={} operation={} elapsedMs={}",
                    config.provider(), operationName, TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAt));
        } catch (RuntimeException e) {
            log.error("Shetab provider error provider={} operation={} elapsedMs={} message={}",
                    config.provider(), operationName, TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAt), e.getMessage(), e);
            throw e;
        }
    }

    private String resolveProvider(Exchange exchange) {
        String headerProvider = exchange.getMessage().getHeader(ShetabHeaders.PROVIDER, String.class);
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
        throw new IllegalArgumentException("Shetab provider is not specified");
    }

    private ShetabEndpointOverrides overrides(Exchange exchange) {
        return new ShetabEndpointOverrides(
                first(exchange.getMessage().getHeader(ShetabHeaders.TIMEOUT_MS, Integer.class), endpoint.getTimeoutMs()),
                first(exchange.getMessage().getHeader(ShetabHeaders.RATE_LIMIT_ENABLED, Boolean.class), endpoint.getRateLimitEnabled()),
                first(exchange.getMessage().getHeader(ShetabHeaders.RATE_LIMIT_BUCKET, String.class), endpoint.getRateLimitBucket()),
                first(exchange.getMessage().getHeader(ShetabHeaders.RATE_LIMIT_KEY, String.class), endpoint.getRateLimitKey())
        );
    }

    private Map<String, Object> bodyAsMap(Object body) {
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
                throw new IllegalArgumentException("Shetab body must be a JSON object", e);
            }
        }
        return objectMapper.convertValue(body, MAP_TYPE);
    }

    private Object maskSensitive(Object value) {
        return maskSensitive(null, value);
    }

    @SuppressWarnings("unchecked")
    private Object maskSensitive(String key, Object value) {
        if (isSensitiveKey(key)) {
            return "***";
        }
        if (value instanceof Map<?, ?> map) {
            Map<String, Object> masked = new LinkedHashMap<>();
            map.forEach((entryKey, entryValue) ->
                    masked.put(String.valueOf(entryKey), maskSensitive(String.valueOf(entryKey), entryValue)));
            return masked;
        }
        if (value instanceof Collection<?> collection) {
            return collection.stream().map(item -> maskSensitive(null, item)).toList();
        }
        return value;
    }

    private boolean isSensitiveKey(String key) {
        if (key == null) {
            return false;
        }
        String normalized = key.replace("-", "").replace("_", "").toLowerCase();
        return "52".equals(key)
                || "14".equals(key)
                || "48".equals(key)
                || "128".equals(key)
                || normalized.contains("pin")
                || normalized.contains("mac")
                || normalized.contains("cvv")
                || normalized.contains("expiry")
                || normalized.contains("expire");
    }

    private <T> T first(T value, T fallback) {
        return value != null ? value : fallback;
    }

    private <T> T bean(Class<T> type) {
        T bean = getEndpoint().getCamelContext().getRegistry().findSingleByType(type);
        if (bean == null) {
            throw new IllegalStateException("No bean found for " + type.getName());
        }
        return bean;
    }
}
