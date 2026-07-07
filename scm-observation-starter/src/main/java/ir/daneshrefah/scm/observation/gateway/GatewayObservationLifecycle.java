package ir.daneshrefah.scm.observation.gateway;

import ir.daneshrefah.scm.observation.ObservationContext;
import ir.daneshrefah.scm.observation.ObservationIds;
import ir.daneshrefah.scm.observation.ObservationScope;
import ir.daneshrefah.scm.observation.ScmObservation;
import ir.daneshrefah.scm.observation.attributes.metric.CommonMetricTags;
import ir.daneshrefah.scm.observation.attributes.trace.CommonTraceAttributes;
import ir.daneshrefah.scm.observation.metrics.CommonMetricNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class GatewayObservationLifecycle {
    private static final Logger log = LoggerFactory.getLogger(GatewayObservationLifecycle.class);
    private static final String GATEWAY_RECEIVE = "gateway.receive";
    private static final String REQUEST_COMPLETED = "request.completed";
    private static final String REQUEST_FAILED = "request.failed";
    private static final String OUTCOME_SUCCESS = "success";
    private static final String OUTCOME_FAILURE = "failure";
    private static final String DEFAULT_VALUE = "default";
    private static final Set<String> MISSING_CHANNEL_CODES = Set.of(
            "null",
            "blank",
            "unknown",
            "default",
            "none",
            "n/a",
            "n-a"
    );

    private final ScmObservation observation;
    private final ObservationContext observationContext;

    public GatewayObservationLifecycle(ScmObservation observation, ObservationContext observationContext) {
        this.observation = observation;
        this.observationContext = observationContext;
    }

    public GatewayObservationScope start(GatewayObservationRequest request) {
        GatewayObservationRequest safeRequest = request == null ? GatewayObservationRequest.builder().build() : request;
        GatewayObservationContext context = context(safeRequest);
        ObservationScope traceScope = null;
        try {
            traceScope = observation.trace()
                    .source(GatewayObservationLifecycle.class)
                    .span(GATEWAY_RECEIVE)
                    .spanKind(spanKind(context.protocol()))
                    .correlationId(context.correlationId())
                    .traceId(context.traceId())
                    .spanId(context.gatewaySpanId())
                    .attribute("scm.gateway.name", context.gatewayName())
                    .attribute("scm.channel.code", businessChannelCode(context.channelCode()))
                    .attribute("scm.protocol", context.protocol().value())
                    .attribute("scm.request.name", context.requestName())
                    .attribute("scm.message.id", context.messageId())
                    .attribute("scm.route.id", safeRequest.routeId())
                    .attribute("client.address", safeRequest.clientAddress())
                    .attributes(safeRequest.attributes())
                    .start();
        } catch (RuntimeException ex) {
            safeObservationFailure("start", ex);
        }
        return new GatewayObservationScope(this, safeRequest, context, traceScope, System.nanoTime());
    }

    void finish(
            GatewayObservationRequest request,
            GatewayObservationContext context,
            ObservationScope traceScope,
            long startNanos,
            GatewayObservationResult result
    ) {
        GatewayObservationResult safeResult = result == null ? GatewayObservationResult.success() : result;
        long durationMs = durationMillis(startNanos);
        try {
            finishTrace(traceScope, safeResult, durationMs);
        } catch (RuntimeException ex) {
            safeObservationFailure("trace", ex);
        }
        try {
            recordMetrics(context, safeResult, durationMs);
        } catch (RuntimeException ex) {
            safeObservationFailure("metric", ex);
        }
    }

    private GatewayObservationContext context(GatewayObservationRequest request) {
        GatewayProtocol protocol = request.protocol() == null ? GatewayProtocol.UNKNOWN : request.protocol();
        return new GatewayObservationContext(
                textOrGenerate(request.correlationId(), ObservationIds.correlationId()),
                textOrGenerate(request.traceId(), ObservationIds.traceId()),
                textOrGenerate(request.spanId(), ObservationIds.spanId()),
                textOrDefault(request.gatewayName(), observationContext.gatewayName()),
                textOrDefault(request.channelCode(), observationContext.channelCode()),
                protocol,
                textOrDefault(request.requestName(), protocol.value()),
                textOrNull(request.messageId())
        );
    }

    private void finishTrace(ObservationScope traceScope, GatewayObservationResult result, long durationMs) {
        if (traceScope == null) {
            return;
        }
        putResultAttributes(traceScope, result);
        if (OUTCOME_FAILURE.equals(outcome(result))) {
            traceScope.failure();
        } else {
            traceScope.success();
        }
        traceScope.close();
    }

    private void recordMetrics(GatewayObservationContext context, GatewayObservationResult result, long durationMs) {
        String outcome = outcome(result);
        observation.metric()
                .counter("scm.gateway.requests")
                .tag("app_name", observationContext.appName())
                .tag("app_profile", observationContext.appProfile())
                .tag("app_label", observationContext.appLabel())
                .tag("platform", observationContext.platform())
                .tag(CommonMetricTags.CHANNEL_CODE, context.channelCode())
                .tag("gateway_name", context.gatewayName())
                .tag("protocol", context.protocol().value())
                .tag("request_name", context.requestName())
                .tag(CommonMetricTags.OUTCOME, outcome)
                .increment();

        observation.metric()
                .timer(CommonMetricNames.REQUEST_DURATION)
                .tag("app_name", observationContext.appName())
                .tag("app_profile", observationContext.appProfile())
                .tag("app_label", observationContext.appLabel())
                .tag("platform", observationContext.platform())
                .tag(CommonMetricTags.CHANNEL_CODE, context.channelCode())
                .tag("gateway_name", context.gatewayName())
                .tag("protocol", context.protocol().value())
                .tag("request_name", context.requestName())
                .tag(CommonMetricTags.OUTCOME, outcome)
                .record(durationMs, TimeUnit.MILLISECONDS);

        if (OUTCOME_FAILURE.equals(outcome)) {
            observation.metric()
                    .counter(CommonMetricNames.FAULTS)
                    .tag("app_name", observationContext.appName())
                    .tag("app_profile", observationContext.appProfile())
                    .tag("app_label", observationContext.appLabel())
                    .tag("platform", observationContext.platform())
                    .tag(CommonMetricTags.CHANNEL_CODE, context.channelCode())
                    .tag("gateway_name", context.gatewayName())
                    .tag("protocol", context.protocol().value())
                    .tag("request_name", context.requestName())
                    .tag(CommonMetricTags.OUTCOME, outcome)
                    .tag(CommonMetricTags.ERROR_CODE, result.errorCode())
                    .increment();
        }
    }

    private void putResultAttributes(ObservationScope traceScope, GatewayObservationResult result) {
        traceScope.attributes(result.attributes());
        traceScope.attribute(CommonTraceAttributes.ERROR_CODE, result.errorCode());
        traceScope.attribute(CommonTraceAttributes.ERROR_TYPE, result.errorType());
        traceScope.attribute(CommonTraceAttributes.ERROR_MESSAGE, result.errorMessage());
    }

    private String spanKind(GatewayProtocol protocol) {
        return protocol == GatewayProtocol.MQ || protocol == GatewayProtocol.JMS ? "consumer" : "server";
    }

    private String outcome(GatewayObservationResult result) {
        String outcome = result.outcome();
        return outcome == null || outcome.isBlank() ? OUTCOME_SUCCESS : outcome.trim();
    }

    private long durationMillis(long startNanos) {
        return TimeUnit.NANOSECONDS.toMillis(Math.max(0L, System.nanoTime() - startNanos));
    }

    private String textOrDefault(String value, String fallback) {
        String candidate = textOrNull(value);
        if (candidate != null) {
            return candidate;
        }
        candidate = textOrNull(fallback);
        return candidate == null ? DEFAULT_VALUE : candidate;
    }

    private String textOrGenerate(String value, String generatedValue) {
        String candidate = textOrNull(value);
        return candidate == null ? generatedValue : candidate;
    }

    private String textOrNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String businessChannelCode(String value) {
        String candidate = textOrNull(value);
        if (candidate == null) {
            return null;
        }
        String normalized = candidate.toLowerCase(Locale.ROOT);
        return MISSING_CHANNEL_CODES.contains(normalized) ? null : normalized;
    }

    private void safeObservationFailure(String phase, RuntimeException ex) {
        log.warn("event=GATEWAY_OBSERVATION_FAILED phase={} outcome=failed errorType={} errorMessage={}",
                phase,
                ex.getClass().getName(),
                ex.getMessage());
    }

}
