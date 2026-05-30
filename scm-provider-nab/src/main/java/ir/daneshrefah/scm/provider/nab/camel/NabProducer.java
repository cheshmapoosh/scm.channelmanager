package ir.daneshrefah.scm.provider.nab.camel;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.provider.nab.application.NabProviderService;
import ir.daneshrefah.scm.provider.nab.config.NabConfigResolver;
import ir.daneshrefah.scm.provider.nab.config.NabEndpointOverrides;
import ir.daneshrefah.scm.provider.nab.config.NabHeaders;
import ir.daneshrefah.scm.provider.nab.config.NabResolvedConfig;
import ir.daneshrefah.scm.provider.nab.metrics.NabProviderMetrics;
import ir.daneshrefah.scm.provider.nab.ratelimit.NabRateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.support.DefaultProducer;
import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.TimeUnit;

@Slf4j
public class NabProducer extends DefaultProducer {
    private final NabEndpoint endpoint;
    private NabConfigResolver configResolver;
    private NabProviderService providerService;
    private NabRateLimiter rateLimiter;
    private NabProviderMetrics metrics;
    private ObjectMapper objectMapper;

    public NabProducer(NabEndpoint endpoint) {
        super(endpoint);
        this.endpoint = endpoint;
    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();
        configResolver = bean(NabConfigResolver.class);
        providerService = bean(NabProviderService.class);
        rateLimiter = bean(NabRateLimiter.class);
        metrics = bean(NabProviderMetrics.class);
        objectMapper = bean(ObjectMapper.class);
    }

    @Override
    public void process(Exchange exchange) {
        String provider = resolveProvider(exchange);
        NabResolvedConfig config = configResolver.resolve(provider, overrides(exchange));
        JsonNode input = bodyAsJsonNode(exchange.getMessage().getBody());
        String operation = operationName(input);
        NabProviderMetrics.CounterSet providerMetrics = metrics.provider(config.provider());
        long startedAt = System.nanoTime();

        log.info("NAB provider call started provider={}", config.provider());
        if (log.isDebugEnabled()) {
            log.debug("NAB provider input provider={} body={}", config.provider(), input);
        }
        providerMetrics.submitted();
        try {
            rateLimiter.acquire(config, operation);
            JsonNode output = providerService.execute(input, config);
            if (log.isDebugEnabled()) {
                log.debug("NAB provider output provider={} body={}", config.provider(), output);
            }
            exchange.getMessage().setBody(output);
            providerMetrics.succeeded();
            log.info("NAB provider call finished provider={} operation={}", config.provider(), operation);
        } catch (RuntimeException e) {
            if (isTimedOut(e)) {
                providerMetrics.timedOut();
            } else {
                providerMetrics.failed();
            }
            log.error("NAB provider call failed provider={} operation={}", config.provider(), operation, e);
            throw e;
        } finally {
            providerMetrics.addLatency(TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAt));
        }
    }

    private String resolveProvider(Exchange exchange) {
        String headerProvider = exchange.getMessage().getHeader(NabHeaders.PROVIDER, String.class);
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
        throw new IllegalArgumentException("NAB provider is not specified");
    }

    private NabEndpointOverrides overrides(Exchange exchange) {
        Integer timeout = first(exchange.getMessage().getHeader(NabHeaders.TIMEOUT_MS, Integer.class), endpoint.getTimeoutMs());
        String charset = first(exchange.getMessage().getHeader(NabHeaders.CHARSET, String.class), endpoint.getCharset());
        Boolean rateLimitEnabled = first(exchange.getMessage().getHeader(NabHeaders.RATE_LIMIT_ENABLED, Boolean.class), endpoint.getRateLimitEnabled());
        String rateLimitBucket = first(exchange.getMessage().getHeader(NabHeaders.RATE_LIMIT_BUCKET, String.class), endpoint.getRateLimitBucket());
        String rateLimitKey = first(exchange.getMessage().getHeader(NabHeaders.RATE_LIMIT_KEY, String.class), endpoint.getRateLimitKey());
        return new NabEndpointOverrides(timeout, charset, rateLimitEnabled, rateLimitBucket, rateLimitKey);
    }

    private String operationName(JsonNode input) {
        if (input == null || !input.isObject()) {
            return "default";
        }
        String code = StringUtils.trimToEmpty(input.path("command").path("code").asText());
        String protocol = StringUtils.trimToEmpty(input.path("command").path("protocol").asText());
        if (code.isEmpty() && protocol.isEmpty()) {
            return "default";
        }
        return protocol + ":" + code;
    }

    private boolean isTimedOut(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            String message = current.getMessage();
            if (message != null && message.toLowerCase().contains("timed out")) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private JsonNode bodyAsJsonNode(Object body) {
        if (body instanceof JsonNode jsonNode) {
            return jsonNode;
        }
        if (body instanceof String text) {
            try {
                return objectMapper.readTree(text);
            } catch (Exception e) {
                throw new IllegalArgumentException("NAB provider body must be a JSON object string", e);
            }
        }
        return objectMapper.valueToTree(body);
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
