package ir.daneshrefah.scm.provider.nab.camel;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.model.gateway.Service;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.operation.Operation;
import ir.daneshrefah.scm.observation.starter.ObservationScope;
import ir.daneshrefah.scm.observation.starter.ScmObservation;
import ir.daneshrefah.scm.observation.starter.attributes.metric.CommonMetricTags;
import ir.daneshrefah.scm.observation.starter.attributes.trace.CommonTraceAttributes;
import ir.daneshrefah.scm.provider.nab.observation.attributes.NabMetricTags;
import ir.daneshrefah.scm.provider.nab.observation.attributes.NabTraceAttributes;
import ir.daneshrefah.scm.provider.nab.observation.NabMetricNames;
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
    private static final String PROVIDER_NAME = "NAB";
    private static final String OUTCOME_SUCCESS = "success";
    private static final String OUTCOME_FAILURE = "failure";

    private final NabEndpoint endpoint;
    private NabConfigResolver configResolver;
    private NabProviderService providerService;
    private NabRateLimiter rateLimiter;
    private NabProviderMetrics metrics;
    private ObjectMapper objectMapper;
    private ScmObservation observation;

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
    }

    @Override
    public void process(Exchange exchange) {
        String provider = resolveProvider(exchange);
        NabResolvedConfig config;
        try {
            config = configResolver.resolve(provider, overrides(exchange));
        } catch (RuntimeException e) {
            logProviderResolutionFailed(exchange, provider, resolveOperationName(exchange), e);
            throw e;
        }
        JsonNode input = bodyAsJsonNode(exchange.getMessage().getBody());
        String operation = operationName(input);
        NabProviderMetrics.CounterSet providerMetrics = metrics.provider(config.provider());
        long startedAt = System.nanoTime();
        ObservationScope observationScope = startObservation(exchange, config, input, operation);

        log.info("NAB provider call started provider={}", config.provider());
        providerMetrics.submitted();
        try {
            rateLimiter.acquire(config, operation);
            JsonNode output = providerService.execute(input, config);
            exchange.getMessage().setBody(output);
            providerMetrics.succeeded();
            long durationMs = elapsedMillis(startedAt);
            markObservationSuccess(observationScope, config, output, durationMs);
            recordObservationMetrics(config, operationCode(input), OUTCOME_SUCCESS, durationMs, null);
            log.info("NAB provider call finished provider={} operation={}", config.provider(), operation);
        } catch (RuntimeException e) {
            if (isTimedOut(e)) {
                providerMetrics.timedOut();
            } else {
                providerMetrics.failed();
            }
            long durationMs = elapsedMillis(startedAt);
            markObservationFailure(observationScope, config, e, durationMs);
            recordObservationMetrics(config, operationCode(input), OUTCOME_FAILURE, durationMs, errorCode(e));
            log.error("NAB provider call failed provider={} operation={}", config.provider(), operation, e);
            throw e;
        } finally {
            providerMetrics.addLatency(elapsedMillis(startedAt));
            observationScope.close();
        }
    }

    private ObservationScope startObservation(Exchange exchange, NabResolvedConfig config, JsonNode input, String operation) {
        var traceBuilder = observation.trace()
                .span("operation.call")
                .spanKind("client")
                .action("operation.call")
                .attribute(NabTraceAttributes.PROVIDER_CODE, config.provider())
                .attribute(NabTraceAttributes.PROVIDER_NAME, PROVIDER_NAME)
                .attribute(NabTraceAttributes.PROVIDER_TYPE, PROVIDER_TYPE)
                .attribute(NabTraceAttributes.OPERATION_CODE, operationCode(input))
                .attribute(NabTraceAttributes.OPERATION_NAME, operation);
        String correlationId = correlationId(exchange);
        if (StringUtils.isNotBlank(correlationId)) {
            traceBuilder.correlationId(correlationId);
        }
        return traceBuilder.start();
    }

    private void markObservationSuccess(ObservationScope scope, NabResolvedConfig config, JsonNode output, long durationMs) {
        scope.attribute(NabTraceAttributes.PROVIDER_CODE, config.provider())
                .attribute(NabTraceAttributes.PROVIDER_NAME, PROVIDER_NAME)
                .attribute(NabTraceAttributes.PROVIDER_TYPE, PROVIDER_TYPE)
                .attribute(NabTraceAttributes.PROVIDER_STATUS, OUTCOME_SUCCESS)
                .attribute(NabTraceAttributes.PROVIDER_DURATION_MS, durationMs)
                .attribute(NabTraceAttributes.PROVIDER_RESPONSE_CODE, responseCode(output))
                .success();
    }

    private void markObservationFailure(ObservationScope scope, NabResolvedConfig config, RuntimeException exception, long durationMs) {
        scope.attribute(NabTraceAttributes.PROVIDER_CODE, config.provider())
                .attribute(NabTraceAttributes.PROVIDER_NAME, PROVIDER_NAME)
                .attribute(NabTraceAttributes.PROVIDER_TYPE, PROVIDER_TYPE)
                .attribute(NabTraceAttributes.PROVIDER_STATUS, OUTCOME_FAILURE)
                .attribute(NabTraceAttributes.PROVIDER_DURATION_MS, durationMs)
                .attribute(NabTraceAttributes.PROVIDER_ERROR_CODE, errorCode(exception))
                .attribute(NabTraceAttributes.PROVIDER_ERROR_MESSAGE, safeMessage(exception))
                .failure(exception)
                .attribute(CommonTraceAttributes.ERROR_MESSAGE, safeMessage(exception));
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
                                             String provider,
                                             String operationName,
                                             RuntimeException exception) {
        log.warn("event=PROVIDER_RESOLUTION_FAILED providerUri={} scheme={} providerCode={} availableProviderCodes={} operationName={} serviceCode={} gatewayName={} outcome=failed failureType={} failureMessage={}",
                providerUri(exchange, provider),
                scheme(provider),
                providerCode(provider),
                configResolver.availableProviderCodes(),
                operationName,
                serviceCode(exchange),
                gatewayName(exchange),
                exception.getClass().getSimpleName(),
                safeMessage(exception),
                exception);
    }

    private String providerCode(String provider) {
        try {
            return configResolver.providerName(provider);
        } catch (RuntimeException ignored) {
            String cleaned = StringUtils.trimToNull(provider);
            int separator = cleaned != null ? cleaned.indexOf(':') : -1;
            return separator >= 0 ? StringUtils.trimToEmpty(cleaned.substring(separator + 1)) : cleaned;
        }
    }

    private String scheme(String provider) {
        String cleaned = StringUtils.trimToNull(provider);
        int separator = cleaned != null ? cleaned.indexOf(':') : -1;
        return separator >= 0 ? StringUtils.trimToEmpty(cleaned.substring(0, separator)) : NabConfigResolver.COMPONENT_SCHEME;
    }

    private String providerUri(Exchange exchange, String provider) {
        String operationProviderUri = exchange.getMessage().getHeader("scmOperationProviderUri", String.class);
        if (StringUtils.isNotBlank(operationProviderUri)) {
            return operationProviderUri;
        }
        String endpointUri = StringUtils.trimToNull(endpoint.getEndpointUri());
        return endpointUri != null ? endpointUri : provider;
    }

    private String safeMessage(Throwable exception) {
        if (exception == null || exception.getMessage() == null) {
            return null;
        }
        String message = exception.getMessage()
                .replace('\r', ' ')
                .replace('\n', ' ')
                .replaceAll("(?i)(password|token|authorization|pin|cvv2?|pan|account|payload|message)\\s*[:=]\\s*\\S+", "$1=***")
                .trim();
        return message.length() > 300 ? message.substring(0, 300) : message;
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

    private String responseCode(JsonNode output) {
        if (output == null) {
            return null;
        }
        JsonNode status = output.path("status");
        if (!status.isObject()) {
            return null;
        }
        String code = status.path("code").asText(null);
        return StringUtils.trimToNull(code);
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

    private String correlationId(Exchange exchange) {
        String value = exchange.getProperty(Message.CORRELATION_ID, String.class);
        if (StringUtils.isNotBlank(value)) {
            return value;
        }
        value = exchange.getMessage().getHeader(Message.CORRELATION_ID, String.class);
        if (StringUtils.isNotBlank(value)) {
            return value;
        }
        value = exchange.getMessage().getHeader("X-Correlation-Id", String.class);
        if (StringUtils.isNotBlank(value)) {
            return value;
        }
        value = exchange.getMessage().getHeader("X-SCM-Correlation-ID", String.class);
        if (StringUtils.isNotBlank(value)) {
            return value;
        }
        value = exchange.getMessage().getHeader("X-SCM-Client-Correlation-ID", String.class);
        if (StringUtils.isNotBlank(value)) {
            return value;
        }
        return exchange.getMessage().getHeader("correlationId", String.class);
    }

    private long elapsedMillis(long startedAt) {
        return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAt);
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
