package ir.daneshrefah.scm.core.integration.observability;

import ir.daneshrefah.scm.common.handler.PluginHandler;
import ir.daneshrefah.scm.common.exception.ErrorCodeAwareException;
import ir.daneshrefah.scm.common.model.plugin.PluginDetail;
import ir.daneshrefah.scm.core.integration.observability.attributes.CoreLogAttributes;
import ir.daneshrefah.scm.core.integration.observability.attributes.CoreMetricTags;
import ir.daneshrefah.scm.core.integration.observability.attributes.CoreTraceAttributes;
import ir.daneshrefah.scm.observation.starter.ObservationContext;
import ir.daneshrefah.scm.observation.starter.ObservationScope;
import ir.daneshrefah.scm.observation.starter.ScmObservation;
import ir.daneshrefah.scm.observation.starter.LogObservationBuilder;
import ir.daneshrefah.scm.observation.starter.attributes.log.CommonLogAttributes;
import ir.daneshrefah.scm.observation.starter.attributes.metric.CommonMetricTags;
import ir.daneshrefah.scm.observation.starter.metrics.MetricCounterBuilder;
import ir.daneshrefah.scm.observation.starter.metrics.MetricTimerBuilder;
import ir.daneshrefah.scm.observation.starter.metrics.CommonMetricNames;
import org.apache.camel.Exchange;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class PluginObservationSupport {
    private final ObjectProvider<ScmObservation> observationProvider;
    private final ObjectProvider<ObservationContext> contextProvider;
    private final ScmExchangeMdc exchangeMdc;
    private final CoreObservationTraceSupport observationTraceSupport;

    public PluginObservationSupport(
            ObjectProvider<ScmObservation> observationProvider,
            ObjectProvider<ObservationContext> contextProvider,
            ScmExchangeMdc exchangeMdc,
            CoreObservationTraceSupport observationTraceSupport
    ) {
        this.observationProvider = observationProvider;
        this.contextProvider = contextProvider;
        this.exchangeMdc = exchangeMdc;
        this.observationTraceSupport = observationTraceSupport;
    }

    public void execute(
            Exchange exchange,
            PluginDetail detail,
            PluginHandler handler,
            String layer,
            PluginInvocation invocation
    ) throws Exception {
        ScmObservation observation = observationProvider.getIfAvailable();
        if (observation == null) {
            invocation.run();
            return;
        }

        Map<String, String> fields = exchangeMdc.fields(exchange);
        logStarted(observation, exchange, detail, handler, layer, fields);

        Exception invocationFailure = null;
        long startNanos = System.nanoTime();
        try {
            invocation.run();
        } catch (Exception exception) {
            invocationFailure = exception;
        }
        long durationNanos = elapsedNanos(startNanos);
        recordPluginEvent(exchange, detail, handler, layer, durationNanos, invocationFailure);

        if (invocationFailure == null) {
            recordMetrics(observation, fields, detail, handler, layer, durationNanos, "success", null);
            logSuccess(observation, exchange, detail, handler, layer, fields, durationNanos);
            return;
        }

        recordMetrics(observation, fields, detail, handler, layer, durationNanos, "failure", invocationFailure);
        logFailure(observation, exchange, detail, handler, layer, fields, durationNanos, invocationFailure);
        throw invocationFailure;
    }

    private void recordPluginEvent(
            Exchange exchange,
            PluginDetail detail,
            PluginHandler handler,
            String layer,
            long durationNanos,
            Exception exception
    ) {
        ObservationScope scope = observationTraceSupport.activeScope(exchange, layer);
        if (scope == null) {
            return;
        }
        Map<String, Object> attributes = new LinkedHashMap<>();
        put(attributes, CoreTraceAttributes.PLUGIN_NAME.name(), detail == null ? null : detail.getName());
        put(attributes, CoreTraceAttributes.PLUGIN_TYPE.name(),
                handler == null || handler.getType() == null ? null : handler.getType().name());
        put(attributes, CoreTraceAttributes.PLUGIN_PHASE.name(),
                detail == null || detail.getPhase() == null ? null : detail.getPhase().name());
        put(attributes, CoreTraceAttributes.PLUGIN_LAYER.name(), layer);
        put(attributes, CoreTraceAttributes.PLUGIN_DURATION_MS.name(), durationNanos / 1_000_000L);
        put(attributes, "event.outcome", exception == null ? "success" : "failure");
        if (exception != null) {
            put(attributes, "error.type", exception.getClass().getSimpleName());
            put(attributes, "error.code", safeErrorCode(exception));
        }
        try {
            scope.event("plugin.execute", attributes);
        } catch (RuntimeException ignored) {
            // A trace event must never change plugin execution behavior.
        }
    }

    private void put(Map<String, Object> attributes, String name, Object value) {
        if (name != null && value != null) {
            attributes.put(name, value);
        }
    }

    private String safeErrorCode(Exception exception) {
        if (exception instanceof ErrorCodeAwareException aware) {
            return String.valueOf(aware.getErrorCode());
        }
        return exception == null ? null : exception.getClass().getSimpleName();
    }

    private long elapsedNanos(long startNanos) {
        return Math.max(0L, System.nanoTime() - startNanos);
    }

    private void logStarted(ScmObservation observation, Exchange exchange, PluginDetail detail, PluginHandler handler, String layer, Map<String, String> fields) {
        baseLog(observation, exchange, detail, handler, layer, fields)
                .info("Plugin execution started")
                .outcome("started")
                .write();
    }

    private void logSuccess(ScmObservation observation, Exchange exchange, PluginDetail detail, PluginHandler handler, String layer, Map<String, String> fields, long durationNanos) {
        baseLog(observation, exchange, detail, handler, layer, fields)
                .info("Plugin execution completed")
                .outcome("success")
                .attribute(CoreLogAttributes.PLUGIN_DURATION_MS, durationNanos / 1_000_000L)
                .write();
    }

    private void logFailure(ScmObservation observation, Exchange exchange, PluginDetail detail, PluginHandler handler, String layer, Map<String, String> fields, long durationNanos, Exception exception) {
        baseLog(observation, exchange, detail, handler, layer, fields)
                .warn("Plugin execution failed")
                .outcome("failure")
                .attribute(CoreLogAttributes.PLUGIN_DURATION_MS, durationNanos / 1_000_000L)
                .attribute(CommonLogAttributes.ERROR_TYPE, exception.getClass().getName())
                .attribute(CommonLogAttributes.ERROR_CODE, safeErrorCode(exception))
                .write();
    }

    private LogObservationBuilder baseLog(
            ScmObservation observation,
            Exchange exchange,
            PluginDetail detail,
            PluginHandler handler,
            String layer,
            Map<String, String> fields
    ) {
        return observation.log()
                .loggerName(PluginObservationSupport.class)
                .correlationId(fields.get("correlationId"))
                .attribute(CoreLogAttributes.GATEWAY_NAME, fields.get("gatewayName"))
                .attribute(CommonLogAttributes.SCM_CHANNEL_CODE, fields.get("channelCode"))
                .attribute(CoreLogAttributes.SERVICE_CODE, fields.get("serviceCode"))
                .attribute(CoreLogAttributes.OPERATION_CODE, fields.get("operationName"))
                .attribute(CoreLogAttributes.OPERATION_NAME, fields.get("operationName"))
                .attribute(CoreLogAttributes.ROUTE_ID, fields.get("routeId"))
                .attribute(CoreLogAttributes.EXCHANGE_ID, fields.get("exchangeId"))
                .attribute(CoreLogAttributes.PLUGIN_NAME, detail != null ? detail.getName() : null)
                .attribute(CoreLogAttributes.PLUGIN_TYPE, handler != null && handler.getType() != null ? handler.getType().name() : null)
                .attribute(CoreLogAttributes.PLUGIN_PHASE, detail != null && detail.getPhase() != null ? detail.getPhase().name() : null)
                .attribute(CoreLogAttributes.PLUGIN_LAYER, layer)
                .attribute(CoreLogAttributes.PROTOCOL, RouteLogSupport.protocol(exchange));
    }

    private void recordMetrics(
            ScmObservation observation,
            Map<String, String> fields,
            PluginDetail detail,
            PluginHandler handler,
            String layer,
            long durationNanos,
            String outcome,
            Exception exception
    ) {
        ObservationContext context = contextProvider.getIfAvailable();
        var counter = observation.metric()
                .counter(CoreMetricNames.PLUGIN_EXECUTIONS);
        tagCommon(counter, context, fields, detail, handler, layer, outcome, exception);
        counter.increment();

        var timer = observation.metric()
                .timer(CoreMetricNames.PLUGIN_DURATION);
        tagCommon(timer, context, fields, detail, handler, layer, outcome, exception);
        timer.record(durationNanos, TimeUnit.NANOSECONDS);

        if (exception != null) {
            var faults = observation.metric().counter(CommonMetricNames.FAULTS);
            tagCommon(faults, context, fields, detail, handler, layer, outcome, exception);
            faults.increment();
        }
    }

    private void tagCommon(
            MetricCounterBuilder builder,
            ObservationContext context,
            Map<String, String> fields,
            PluginDetail detail,
            PluginHandler handler,
            String layer,
            String outcome,
            Exception exception
    ) {
        builder.tag(CoreMetricTags.APP_NAME, context != null ? context.appName() : null)
                .tag(CoreMetricTags.APP_PROFILE, context != null ? context.appProfile() : null)
                .tag(CoreMetricTags.GATEWAY_NAME, fields.get("gatewayName"))
                .tag(CommonMetricTags.CHANNEL_CODE, fields.get("channelCode"))
                .tag(CoreMetricTags.SERVICE_CODE, fields.get("serviceCode"))
                .tag("operation_name", fields.get("operationName"))
                .tag("plugin_name", detail != null ? detail.getName() : null)
                .tag("plugin_type", handler != null && handler.getType() != null ? handler.getType().name() : null)
                .tag("plugin_phase", detail != null && detail.getPhase() != null ? detail.getPhase().name() : null)
                .tag("plugin_layer", layer)
                .tag(CommonMetricTags.OUTCOME, outcome)
                .tag(CommonMetricTags.ERROR_CODE, exception != null ? exception.getClass().getSimpleName() : null);
    }

    private void tagCommon(
            MetricTimerBuilder builder,
            ObservationContext context,
            Map<String, String> fields,
            PluginDetail detail,
            PluginHandler handler,
            String layer,
            String outcome,
            Exception exception
    ) {
        builder.tag(CoreMetricTags.APP_NAME, context != null ? context.appName() : null)
                .tag(CoreMetricTags.APP_PROFILE, context != null ? context.appProfile() : null)
                .tag(CoreMetricTags.GATEWAY_NAME, fields.get("gatewayName"))
                .tag(CommonMetricTags.CHANNEL_CODE, fields.get("channelCode"))
                .tag(CoreMetricTags.SERVICE_CODE, fields.get("serviceCode"))
                .tag("operation_name", fields.get("operationName"))
                .tag("plugin_name", detail != null ? detail.getName() : null)
                .tag("plugin_type", handler != null && handler.getType() != null ? handler.getType().name() : null)
                .tag("plugin_phase", detail != null && detail.getPhase() != null ? detail.getPhase().name() : null)
                .tag("plugin_layer", layer)
                .tag(CommonMetricTags.OUTCOME, outcome)
                .tag(CommonMetricTags.ERROR_CODE, exception != null ? exception.getClass().getSimpleName() : null);
    }

    @FunctionalInterface
    public interface PluginInvocation {
        void run() throws Exception;
    }
}
