package ir.daneshrefah.scm.provider.shetab.camel;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.model.gateway.Service;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.operation.Operation;
import ir.daneshrefah.scm.common.provider.message.ProviderExchange;
import ir.daneshrefah.scm.common.provider.message.ProviderMessageCustomizerContext;
import ir.daneshrefah.scm.common.provider.message.ProviderMessageCustomizerPipeline;
import ir.daneshrefah.scm.common.provider.message.ProviderRequest;
import ir.daneshrefah.scm.common.provider.message.ProviderResponse;
import ir.daneshrefah.scm.provider.shetab.config.ShetabConfigResolver;
import ir.daneshrefah.scm.provider.shetab.config.ShetabEndpointOverrides;
import ir.daneshrefah.scm.provider.shetab.config.ShetabHeaders;
import ir.daneshrefah.scm.provider.shetab.config.ShetabResolvedConfig;
import ir.daneshrefah.scm.provider.shetab.iso.ShetabIsoMapConverter;
import ir.daneshrefah.scm.provider.shetab.metrics.ShetabProviderMetrics;
import ir.daneshrefah.scm.provider.shetab.ratelimit.ShetabRateLimiter;
import ir.daneshrefah.scm.provider.shetab.tcp.ShetabClientRegistry;
import ir.daneshrefah.scm.provider.shetab.trace.ShetabTraceSupport;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.support.DefaultProducer;
import org.apache.commons.lang3.StringUtils;
import org.jpos.iso.ISOMsg;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
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
        traceSupport = bean(ShetabTraceSupport.class);
        objectMapper = bean(ObjectMapper.class);
    }

    @Override
    public void process(Exchange exchange) {
        traceSupport.enrichLogMdc(exchange);
        String provider = resolveProvider(exchange);
        String operationName = resolveOperationName(exchange);
        ShetabResolvedConfig config;
        try {
            config = configResolver.resolve(provider, overrides(exchange));
        } catch (RuntimeException e) {
            logProviderResolutionFailed(exchange, provider, operationName, e);
            throw e;
        }
        ShetabProviderMetrics.CounterSet providerMetrics = metrics.provider(config.provider());

        Map<String, Object> requestMap = bodyAsMap(exchange.getMessage().getBody());
        ISOMsg request = isoMapConverter.toIsoMsg(requestMap);
        ProviderRequest providerRequest = new ProviderRequest("ISO8583", null, Map.of(), requestMap);
        providerRequest.nativeRequest(request);
        ProviderMessageCustomizerContext customizerContext = customizerContext(exchange, config, operationName);
        ProviderExchange providerExchange = new ProviderExchange(providerRequest, customizerContext);
        ProviderMessageCustomizerPipeline customizerPipeline = config.messageCustomizerPipeline();
        logConfiguredCustomizers(customizerContext, customizerPipeline);
        executeCustomizers(exchange, providerExchange, customizerPipeline, true);

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
            ProviderResponse providerResponse = new ProviderResponse();
            providerResponse.nativeResponse(response);
            providerExchange.response(providerResponse);
            executeCustomizers(exchange, providerExchange, customizerPipeline, false);
            Map<String, Object> responseMap = isoMapConverter.toMap(response);
            providerResponse.body(responseMap);
            providerMetrics.succeeded();
            Duration elapsed = Duration.ofNanos(System.nanoTime() - startedAt);
            providerMetrics.addLatency(elapsed.toMillis());
            providerMetrics.recordProviderRequestDuration(config, customizerContext, elapsed, "success");
            if (log.isDebugEnabled()) {
                log.debug("Shetab provider response provider={} operation={} body={}",
                        config.provider(), operationName, maskSensitive(responseMap));
            }
            exchange.getMessage().setBody(responseMap);
            log.info("Shetab provider done provider={} operation={} elapsedMs={}",
                    config.provider(), operationName, TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAt));
        } catch (RuntimeException e) {
            providerMetrics.recordProviderRequestError(config, customizerContext);
            log.error("Shetab provider error provider={} operation={} elapsedMs={} message={}",
                    config.provider(), operationName, TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAt), e.getMessage(), e);
            throw e;
        }
    }


    private ProviderMessageCustomizerContext customizerContext(Exchange exchange, ShetabResolvedConfig config, String operationName) {
        return new ProviderMessageCustomizerContext(
                config.provider(),
                config.providerType(),
                serviceCode(exchange),
                operationName,
                channelCode(exchange),
                "shetab",
                config.providerConfig(),
                config,
                correlationId(exchange),
                traceId(exchange)
        );
    }

    private void logConfiguredCustomizers(ProviderMessageCustomizerContext context, ProviderMessageCustomizerPipeline pipeline) {
        if (!log.isDebugEnabled()) {
            return;
        }
        List<String> customizers = pipeline == null ? List.of() : pipeline.entries().stream()
                .map(entry -> entry.type() + "#" + entry.order())
                .toList();
        log.debug("Shetab provider customizers configured provider={} type={} service={} operation={} channel={} transport={} customizers={}",
                context.providerCode(), context.providerType(), context.serviceCode(), context.operationCode(),
                context.channelCode(), context.transportType(), customizers);
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
                metrics.provider(providerExchange.context().providerCode()).customizerExecution(
                        providerExchange.context(), entry.type(), beforeSend ? "beforeSend" : "afterReceive");
            } catch (RuntimeException e) {
                metrics.provider(providerExchange.context().providerCode()).customizerError(
                        providerExchange.context(), entry.type(), beforeSend ? "beforeSend" : "afterReceive");
                log.error("Shetab provider customizer error provider={} type={} service={} operation={} channel={} customizer={} phase={} message={}",
                        providerExchange.context().providerCode(), providerExchange.context().providerType(),
                        providerExchange.context().serviceCode(), providerExchange.context().operationCode(),
                        providerExchange.context().channelCode(), entry.type(), beforeSend ? "beforeSend" : "afterReceive",
                        e.getMessage(), e);
                throw e;
            }
        }
    }

    private String resolveProvider(Exchange exchange) {
        String headerProvider = exchange.getMessage().getHeader(ShetabHeaders.PROVIDER, String.class);
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
        throw new IllegalArgumentException("Shetab provider is not specified");
    }

    private void logProviderResolutionFailed(Exchange exchange,
                                             String provider,
                                             String operationName,
                                             RuntimeException exception) {
        log.warn("event=PROVIDER_RESOLUTION_FAILED providerUri={} providerName={} providerType={} transportType={} availableProviderCodes={} operationName={} serviceCode={} gatewayName={} outcome=failed failureType={} failureMessage={}",
                providerUri(exchange, provider),
                normalizedProviderName(provider),
                "shetab",
                "shetab",
                configResolver.availableProviderCodes(),
                operationName,
                serviceCode(exchange),
                gatewayName(exchange),
                exception.getClass().getSimpleName(),
                safeMessage(exception),
                exception);
    }

    private String normalizedProviderName(String provider) {
        try {
            return configResolver.providerName(provider);
        } catch (RuntimeException ignored) {
            return provider;
        }
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
        return exception.getMessage()
                .replace('\r', ' ')
                .replace('\n', ' ')
                .trim();
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

    private String gatewayName(Exchange exchange) {
        String gatewayName = exchange.getProperty(Message.GATEWAY_NAME, String.class);
        if (StringUtils.isNotBlank(gatewayName)) {
            return gatewayName;
        }
        return StringUtils.defaultString(exchange.getMessage().getHeader("gatewayName", String.class));
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
        return StringUtils.defaultString(exchange.getMessage().getHeader("X-SCM-Correlation-ID", String.class));
    }

    private String traceId(Exchange exchange) {
        String traceId = exchange.getProperty(Message.TRACE_ID, String.class);
        if (StringUtils.isNotBlank(traceId)) {
            return traceId;
        }
        return traceSupport.currentTraceIds().getOrDefault("traceId", "");
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
