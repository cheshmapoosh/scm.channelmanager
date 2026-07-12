package ir.daneshrefah.scm.provider.nab.camel;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.model.gateway.Service;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.operation.Operation;
import ir.daneshrefah.scm.observation.starter.ScmObservation;
import ir.daneshrefah.scm.observation.starter.attributes.metric.CommonMetricTags;
import ir.daneshrefah.scm.observation.starter.provider.ProviderBusinessOutcome;
import ir.daneshrefah.scm.provider.nab.observation.attributes.NabMetricTags;
import ir.daneshrefah.scm.provider.nab.observation.NabMetricNames;
import ir.daneshrefah.scm.provider.nab.observation.NabProviderTraceSupport;
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
    private static final String PROVIDER_TYPE = "tcp";
    private static final String OUTCOME_FAILURE = "failure";

    private final NabEndpoint endpoint;
    private NabConfigResolver configResolver;
    private NabProviderService providerService;
    private NabRateLimiter rateLimiter;
    private NabProviderMetrics metrics;
    private ObjectMapper objectMapper;
    private ScmObservation observation;
    private NabProviderTraceSupport traceSupport;

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
        observation = bean(ScmObservation.class);
        traceSupport = bean(NabProviderTraceSupport.class);
    }

    @Override
    public void process(Exchange exchange) {
        String provider = resolveProvider(exchange);
        NabResolvedConfig config;
        try {
            config = configResolver.resolve(provider, overrides(exchange));
        } catch (RuntimeException e) {
            logProviderResolutionFailed(exchange, resolveOperationName(exchange), e);
            throw e;
        }
        JsonNode input = bodyAsJsonNode(exchange.getMessage().getBody());
        String operation = operationName(input);
        NabProviderMetrics.CounterSet providerMetrics = metrics.provider(config.provider());
        long startedAt = System.nanoTime();

        log.info("NAB provider call started provider={}", config.provider());
        providerMetrics.submitted();
        try {
            rateLimiter.acquire(config, operation);
            NabProviderTraceSupport.ProviderAttempt attempt = traceSupport.startAttempt(exchange, config, input, operation);
            JsonNode output = null;
            RuntimeException providerFailure = null;
            ProviderBusinessOutcome outcome = null;
            try {
                output = providerService.execute(input, config);
                outcome = traceSupport.providerOutcome(output, null);
                setProviderOutcome(exchange, outcome);
            } catch (RuntimeException exception) {
                providerFailure = exception;
                outcome = traceSupport.providerOutcome(output, exception);
                setProviderOutcome(exchange, outcome);
                throw exception;
            } finally {
                ProviderBusinessOutcome finalOutcome = outcome == null
                        ? traceSupport.providerOutcome(output, providerFailure)
                        : outcome;
                traceSupport.finishAttempt(attempt, exchange, config, output, finalOutcome, providerFailure);
            }
            exchange.getMessage().setBody(output);
            if (outcome != null && outcome.success()) {
                providerMetrics.succeeded();
            } else {
                providerMetrics.failed();
            }
            long durationMs = elapsedMillis(startedAt);
            recordObservationMetrics(config, operationCode(input),
                    outcome == null ? OUTCOME_FAILURE : outcome.eventOutcome(),
                    durationMs,
                    outcome == null ? null : outcome.errorCode());
            log.info("NAB provider call finished provider={} operation={} outcome={} responseCode={} errorCode={} errorType={}",
                    config.provider(), operation,
                    outcome == null ? OUTCOME_FAILURE : outcome.eventOutcome(),
                    outcome == null ? null : outcome.responseCode(),
                    outcome == null ? null : outcome.errorCode(),
                    outcome == null ? null : outcome.errorType());
        } catch (RuntimeException e) {
            setProviderOutcomeIfAbsent(exchange, ProviderBusinessOutcome.technicalFailure(null, e));
            ProviderBusinessOutcome failureOutcome = exchange.getProperty(
                    ProviderBusinessOutcome.EXCHANGE_PROPERTY,
                    ProviderBusinessOutcome.class
            );
            if (isTimedOut(e)) {
                providerMetrics.timedOut();
            } else {
                providerMetrics.failed();
            }
            long durationMs = elapsedMillis(startedAt);
            recordObservationMetrics(
                    config,
                    operationCode(input),
                    failureOutcome == null ? OUTCOME_FAILURE : failureOutcome.eventOutcome(),
                    durationMs,
                    failureOutcome == null ? errorCode(e) : failureOutcome.errorCode()
            );
            log.error("NAB provider call failed provider={} operation={} outcome={} errorCode={} errorType={} failureType={} failureCode={}",
                    config.provider(), operation,
                    failureOutcome == null ? OUTCOME_FAILURE : failureOutcome.eventOutcome(),
                    failureOutcome == null ? null : failureOutcome.errorCode(),
                    failureOutcome == null ? null : failureOutcome.errorType(),
                    e.getClass().getSimpleName(), errorCode(e));
            throw e;
        } finally {
            providerMetrics.addLatency(elapsedMillis(startedAt));
        }
    }

    private void recordObservationMetrics(
            NabResolvedConfig config,
            String operationCode,
            String outcome,
            long durationMs,
            String errorCode
    ) {
        observation.metric()
                .counter(NabMetricNames.PROVIDER_CALLS)
                .tag(NabMetricTags.PROVIDER_CODE, config.provider())
                .tag(NabMetricTags.PROVIDER_TYPE, PROVIDER_TYPE)
                .tag(CommonMetricTags.OPERATION_CODE, operationCode)
                .tag(CommonMetricTags.OUTCOME, outcome)
                .tag(CommonMetricTags.ERROR_CODE, errorCode)
                .increment();

        observation.metric()
                .timer(NabMetricNames.PROVIDER_DURATION)
                .tag(NabMetricTags.PROVIDER_CODE, config.provider())
                .tag(NabMetricTags.PROVIDER_TYPE, PROVIDER_TYPE)
                .tag(CommonMetricTags.OPERATION_CODE, operationCode)
                .tag(CommonMetricTags.OUTCOME, outcome)
                .tag(CommonMetricTags.ERROR_CODE, errorCode)
                .record(durationMs, TimeUnit.MILLISECONDS);
    }

    private String resolveProvider(Exchange exchange) {
        String headerProvider = exchange.getMessage().getHeader(NabHeaders.PROVIDER, String.class);
        if (StringUtils.isNotBlank(headerProvider)) {
            return headerProvider;
        }
        String operationProviderUri = exchange.getMessage().getHeader("scmOperationProviderUri", String.class);
        if (StringUtils.isNotBlank(operationProviderUri)) {
            return operationProviderUri;
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

    private void logProviderResolutionFailed(Exchange exchange,
                                             String operationName,
                                             RuntimeException exception) {
        log.warn("event=PROVIDER_RESOLUTION_FAILED providerType=NAB availableProviderCodes={} operationName={} serviceCode={} gatewayName={} outcome=failed failureType={}",
                configResolver.availableProviderCodes(),
                operationName,
                serviceCode(exchange),
                gatewayName(exchange),
                exception.getClass().getSimpleName());
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

    private String operationCode(JsonNode input) {
        if (input == null || !input.isObject()) {
            return "default";
        }
        return StringUtils.defaultIfBlank(StringUtils.trimToNull(input.path("command").path("code").asText()), "default");
    }

    private String errorCode(Throwable throwable) {
        if (isTimedOut(throwable)) {
            return "timeout";
        }
        return throwable == null ? null : throwable.getClass().getSimpleName();
    }

    private String resolveOperationName(Exchange exchange) {
        String operationName = exchange.getProperty(Message.OPERATION_NAME, String.class);
        if (StringUtils.isNotBlank(operationName)) {
            return operationName;
        }
        Operation operation = exchange.getProperty(Message.OPERATION, Operation.class);
        return operation != null ? operation.getName() : "";
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

    private void setProviderOutcome(Exchange exchange, ProviderBusinessOutcome outcome) {
        if (exchange != null && outcome != null) {
            exchange.setProperty(ProviderBusinessOutcome.EXCHANGE_PROPERTY, outcome);
        }
    }

    private void setProviderOutcomeIfAbsent(Exchange exchange, ProviderBusinessOutcome outcome) {
        if (exchange != null
                && outcome != null
                && exchange.getProperty(ProviderBusinessOutcome.EXCHANGE_PROPERTY, ProviderBusinessOutcome.class) == null) {
            exchange.setProperty(ProviderBusinessOutcome.EXCHANGE_PROPERTY, outcome);
        }
    }

    private String serviceCode(Exchange exchange) {
        Service service = exchange.getProperty(Message.SERVICE, Service.class);
        if (service != null && StringUtils.isNotBlank(service.getCode())) {
            return service.getCode();
        }
        return StringUtils.defaultString(exchange.getMessage().getHeader("serviceCode", String.class));
    }

    private String gatewayName(Exchange exchange) {
        String gatewayName = exchange.getProperty(Message.GATEWAY_NAME, String.class);
        if (StringUtils.isNotBlank(gatewayName)) {
            return gatewayName;
        }
        return StringUtils.defaultString(exchange.getMessage().getHeader("gatewayName", String.class));
    }

    private long elapsedMillis(long startedAt) {
        return TimeUnit.NANOSECONDS.toMillis(Math.max(0L, System.nanoTime() - startedAt));
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
