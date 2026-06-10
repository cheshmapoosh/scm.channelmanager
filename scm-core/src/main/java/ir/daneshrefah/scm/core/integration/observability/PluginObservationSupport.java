package ir.daneshrefah.scm.core.integration.observability;

import ir.daneshrefah.scm.common.handler.PluginHandler;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.plugin.PluginDetail;
import ir.daneshrefah.scm.observation.ObservationContext;
import ir.daneshrefah.scm.observation.ObservationScope;
import ir.daneshrefah.scm.observation.ScmObservation;
import ir.daneshrefah.scm.observation.attributes.ScmCommonAttributes;
import ir.daneshrefah.scm.observation.attributes.ScmErrorAttributes;
import ir.daneshrefah.scm.observation.attributes.ScmGatewayAttributes;
import ir.daneshrefah.scm.observation.attributes.ScmMetricAttributes;
import ir.daneshrefah.scm.observation.attributes.ScmOperationAttributes;
import ir.daneshrefah.scm.observation.attributes.ScmServiceAttributes;
import ir.daneshrefah.scm.observation.metrics.MetricCounterBuilder;
import ir.daneshrefah.scm.observation.metrics.MetricTimerBuilder;
import ir.daneshrefah.scm.observation.metrics.ScmMetricNames;
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
            scope.attribute("plugin.duration_ms", durationNanos / 1_000_000L).success();
            recordMetrics(observation, fields, detail, handler, layer, durationNanos, "success", null);
            logSuccess(observation, exchange, detail, handler, layer, fields, durationNanos);
        } catch (Exception exception) {
            long durationNanos = System.nanoTime() - startNanos;
            scope.failure(exception)
                    .attribute("plugin.duration_ms", durationNanos / 1_000_000L)
                    .attribute(ScmErrorAttributes.MESSAGE, RouteLogSupport.failureMessage(exception));
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
                .attribute(ScmCommonAttributes.GATEWAY_NAME, fields.get("gatewayName"))
                .attribute(ScmCommonAttributes.CHANNEL_CODE, fields.get("channelCode"))
                .attribute(ScmServiceAttributes.CODE, fields.get("serviceCode"))
                .attribute(ScmOperationAttributes.CODE, fields.get("operationName"))
                .attribute(ScmOperationAttributes.NAME, fields.get("operationName"))
                .attribute(ScmGatewayAttributes.ROUTE_ID, fields.get("routeId"))
                .attribute("scm.exchange.id", fields.get("exchangeId"))
                .attribute("plugin.name", detail != null ? detail.getName() : null)
                .attribute("plugin.type", handler != null && handler.getType() != null ? handler.getType().name() : null)
                .attribute("plugin.phase", detail != null && detail.getPhase() != null ? detail.getPhase().name() : null)
                .attribute("plugin.layer", layer)
                .attribute("scm.protocol", RouteLogSupport.protocol(exchange))
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
                .attribute("plugin.duration_ms", durationNanos / 1_000_000L)
                .write();
    }

    private void logFailure(ScmObservation observation, Exchange exchange, PluginDetail detail, PluginHandler handler, String layer, Map<String, String> fields, long durationNanos, Exception exception) {
        baseLog(observation, exchange, detail, handler, layer, fields)
                .warn("Plugin execution failed")
                .outcome("failure")
                .attribute("plugin.duration_ms", durationNanos / 1_000_000L)
                .attribute(ScmErrorAttributes.TYPE, exception.getClass().getName())
                .attribute(ScmErrorAttributes.MESSAGE, RouteLogSupport.failureMessage(exception))
                .write();
    }

    private ir.daneshrefah.scm.observation.LogObservationBuilder baseLog(
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
                .attribute(ScmCommonAttributes.GATEWAY_NAME, fields.get("gatewayName"))
                .attribute(ScmCommonAttributes.CHANNEL_CODE, fields.get("channelCode"))
                .attribute(ScmServiceAttributes.CODE, fields.get("serviceCode"))
                .attribute(ScmOperationAttributes.CODE, fields.get("operationName"))
                .attribute(ScmOperationAttributes.NAME, fields.get("operationName"))
                .attribute(ScmGatewayAttributes.ROUTE_ID, fields.get("routeId"))
                .attribute("scm.exchange.id", fields.get("exchangeId"))
                .attribute("plugin.name", detail != null ? detail.getName() : null)
                .attribute("plugin.type", handler != null && handler.getType() != null ? handler.getType().name() : null)
                .attribute("plugin.phase", detail != null && detail.getPhase() != null ? detail.getPhase().name() : null)
                .attribute("plugin.layer", layer)
                .attribute("scm.protocol", RouteLogSupport.protocol(exchange));
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
                .counter(ScmMetricNames.PLUGIN_EXECUTIONS);
        tagCommon(counter, context, fields, detail, handler, layer, outcome, exception);
        counter.increment();

        var timer = observation.metric()
                .timer(ScmMetricNames.PLUGIN_DURATION);
        tagCommon(timer, context, fields, detail, handler, layer, outcome, exception);
        timer.record(durationNanos, TimeUnit.NANOSECONDS);

        if (exception != null) {
            var faults = observation.metric().counter(ScmMetricNames.FAULTS);
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
        builder.tag(ScmMetricAttributes.APP_NAME, context != null ? context.appName() : null)
                .tag(ScmMetricAttributes.APP_PROFILE, context != null ? context.appProfile() : null)
                .tag(ScmMetricAttributes.APP_LABEL, context != null ? context.appLabel() : null)
                .tag(ScmMetricAttributes.PLATFORM, context != null ? context.platform() : null)
                .tag(ScmMetricAttributes.GATEWAY_NAME, fields.get("gatewayName"))
                .tag(ScmMetricAttributes.CHANNEL_CODE, fields.get("channelCode"))
                .tag(ScmMetricAttributes.SERVICE_CODE, fields.get("serviceCode"))
                .tag("operation_name", fields.get("operationName"))
                .tag("plugin_name", detail != null ? detail.getName() : null)
                .tag("plugin_type", handler != null && handler.getType() != null ? handler.getType().name() : null)
                .tag("plugin_phase", detail != null && detail.getPhase() != null ? detail.getPhase().name() : null)
                .tag("plugin_layer", layer)
                .tag(ScmMetricAttributes.OUTCOME, outcome)
                .tag(ScmMetricAttributes.ERROR_CODE, exception != null ? exception.getClass().getSimpleName() : null);
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
        builder.tag(ScmMetricAttributes.APP_NAME, context != null ? context.appName() : null)
                .tag(ScmMetricAttributes.APP_PROFILE, context != null ? context.appProfile() : null)
                .tag(ScmMetricAttributes.APP_LABEL, context != null ? context.appLabel() : null)
                .tag(ScmMetricAttributes.PLATFORM, context != null ? context.platform() : null)
                .tag(ScmMetricAttributes.GATEWAY_NAME, fields.get("gatewayName"))
                .tag(ScmMetricAttributes.CHANNEL_CODE, fields.get("channelCode"))
                .tag(ScmMetricAttributes.SERVICE_CODE, fields.get("serviceCode"))
                .tag("operation_name", fields.get("operationName"))
                .tag("plugin_name", detail != null ? detail.getName() : null)
                .tag("plugin_type", handler != null && handler.getType() != null ? handler.getType().name() : null)
                .tag("plugin_phase", detail != null && detail.getPhase() != null ? detail.getPhase().name() : null)
                .tag("plugin_layer", layer)
                .tag(ScmMetricAttributes.OUTCOME, outcome)
                .tag(ScmMetricAttributes.ERROR_CODE, exception != null ? exception.getClass().getSimpleName() : null);
    }

    @FunctionalInterface
    public interface PluginInvocation {
        void run() throws Exception;
    }
}
