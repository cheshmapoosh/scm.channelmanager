package ir.daneshrefah.scm.core.integration.observability;

import ir.daneshrefah.scm.common.handler.PluginHandler;
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
import ir.daneshrefah.scm.observation.starter.attributes.trace.CommonTraceAttributes;
import ir.daneshrefah.scm.observation.starter.metrics.MetricCounterBuilder;
import ir.daneshrefah.scm.observation.starter.metrics.MetricTimerBuilder;
import ir.daneshrefah.scm.observation.starter.metrics.CommonMetricNames;
import org.apache.camel.Exchange;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class PluginObservationSupport {
    private final ObjectProvider<ScmObservation> observationProvider;
    private final ObjectProvider<ObservationContext> contextProvider;
    private final ScmExchangeMdc exchangeMdc;

    public PluginObservationSupport(
            ObjectProvider<ScmObservation> observationProvider,
            ObjectProvider<ObservationContext> contextProvider,
            ScmExchangeMdc exchangeMdc
    ) {
        this.observationProvider = observationProvider;
        this.contextProvider = contextProvider;
        this.exchangeMdc = exchangeMdc;
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

        long startNanos = System.nanoTime();
        Map<String, String> fields = exchangeMdc.fields(exchange);
        ObservationScope scope = startScope(observation, exchange, detail, handler, layer, fields);
        logStarted(observation, exchange, detail, handler, layer, fields);
        try {
            invocation.run();
            long durationNanos = System.nanoTime() - startNanos;
            scope.attribute(CoreTraceAttributes.PLUGIN_DURATION_MS, durationNanos / 1_000_000L).success();
            recordMetrics(observation, fields, detail, handler, layer, durationNanos, "success", null);
            logSuccess(observation, exchange, detail, handler, layer, fields, durationNanos);
        } catch (Exception exception) {
            long durationNanos = System.nanoTime() - startNanos;
            scope.failure(exception)
                    .attribute(CoreTraceAttributes.PLUGIN_DURATION_MS, durationNanos / 1_000_000L)
                    .attribute(CommonTraceAttributes.ERROR_MESSAGE, RouteLogSupport.failureMessage(exception));
            recordMetrics(observation, fields, detail, handler, layer, durationNanos, "failure", exception);
            logFailure(observation, exchange, detail, handler, layer, fields, durationNanos, exception);
            throw exception;
        } finally {
            scope.close();
        }
    }

    private ObservationScope startScope(
            ScmObservation observation,
            Exchange exchange,
            PluginDetail detail,
            PluginHandler handler,
            String layer,
            Map<String, String> fields
    ) {
        return observation.trace()
                .span("plugin.execute")
                .spanKind("internal")
                .action("plugin.execute")
                .correlationId(fields.get("correlationId"))
                .attribute(CommonTraceAttributes.SCM_GATEWAY_NAME, fields.get("gatewayName"))
                .attribute(CommonTraceAttributes.SCM_CHANNEL_CODE, fields.get("channelCode"))
                .attribute(CoreTraceAttributes.SERVICE_CODE, fields.get("serviceCode"))
                .attribute(CoreTraceAttributes.OPERATION_CODE, fields.get("operationName"))
                .attribute(CoreTraceAttributes.OPERATION_NAME, fields.get("operationName"))
                .attribute(CommonTraceAttributes.SCM_ROUTE_ID, fields.get("routeId"))
                .attribute(CoreTraceAttributes.EXCHANGE_ID, fields.get("exchangeId"))
                .attribute(CoreTraceAttributes.PLUGIN_NAME, detail != null ? detail.getName() : null)
                .attribute(CoreTraceAttributes.PLUGIN_TYPE, handler != null && handler.getType() != null ? handler.getType().name() : null)
                .attribute(CoreTraceAttributes.PLUGIN_PHASE, detail != null && detail.getPhase() != null ? detail.getPhase().name() : null)
                .attribute(CoreTraceAttributes.PLUGIN_LAYER, layer)
                .attribute(CommonTraceAttributes.SCM_PROTOCOL, RouteLogSupport.protocol(exchange))
                .start();
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
                .attribute(CommonLogAttributes.ERROR_MESSAGE, RouteLogSupport.failureMessage(exception))
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
