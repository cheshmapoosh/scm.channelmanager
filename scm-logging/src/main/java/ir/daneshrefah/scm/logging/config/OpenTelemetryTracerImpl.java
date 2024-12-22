package ir.daneshrefah.scm.logging.config;

import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.logging.utils.TraceLogUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.*;
import org.apache.camel.opentelemetry.OpenTelemetryTracer;
import org.apache.camel.spi.*;
import org.apache.camel.support.EventNotifierSupport;
import org.apache.camel.support.RoutePolicySupport;
import org.apache.camel.support.service.ServiceHelper;
import org.apache.camel.tracing.ActiveSpanManager;
import org.apache.camel.tracing.InjectAdapter;
import org.apache.camel.tracing.SpanAdapter;
import org.apache.camel.tracing.SpanDecorator;
import org.apache.camel.util.ObjectHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Configurer
@Primary
@Component
@Slf4j
public class OpenTelemetryTracerImpl extends OpenTelemetryTracer {
    public static final String EXTRACT_PATTERN = "direct";
    private final TracingLogListener logListener = new TracingLogListener();
    private final TracingEventNotifier eventNotifier = new TracingEventNotifier();
    @Autowired
    private TraceLogUtils traceLogUtils;

    @Override
    public boolean isEncoding() {
        return super.isEncoding();
    }

    @Override
    public RoutePolicy createRoutePolicy(CamelContext camelContext, String routeId, NamedNode route) {
        init(camelContext);
        return new TracingRoutePolicy();
    }

    @Override
    protected void doInit() {
        CamelContext camelContext = getCamelContext();
        ObjectHelper.notNull(camelContext, "CamelContext", this);

        camelContext.getManagementStrategy().addEventNotifier(eventNotifier);
        if (!camelContext.getRoutePolicyFactories().contains(this)) {
            camelContext.addRoutePolicyFactory(this);
        }
        camelContext.getCamelContextExtension().addLogListener(logListener);

        InterceptStrategy tracingStrategy = getTracingStrategy();
        if (tracingStrategy != null) {
            camelContext.getCamelContextExtension().addInterceptStrategy(tracingStrategy);
        }

        initTracer();
        initContextPropagators();
        ServiceHelper.startService(eventNotifier);
    }

    private final class TracingRoutePolicy extends RoutePolicySupport {

        @Override
        public void onExchangeBegin(Route route, Exchange exchange) {
            try {
                if (isExcluded(exchange, route.getEndpoint())) {
                    return;
                }
                SpanDecorator sd = getSpanDecorator(route.getEndpoint());
                SpanAdapter parent = ActiveSpanManager.getSpan(exchange);
                SpanAdapter span;
                span = startExchangeBeginSpan(exchange, sd, sd.getOperationName(exchange, route.getEndpoint()),
                        sd.getReceiverSpanKind(), parent);
                sd.pre(span, exchange, route.getEndpoint());
                ActiveSpanManager.activate(exchange, span);
                if (log.isTraceEnabled()) {
                    log.trace("Tracing: start server span={}", span);
                }
            } catch (Exception t) {
                // This exception is ignored
                log.warn("Tracing: Failed to capture tracing data. This exception is ignored.", t);
            }
        }

        @Override
        public void onExchangeDone(Route route, Exchange exchange) {
            try {
                if (isExcluded(exchange, route.getEndpoint())) {
                    return;
                }
                SpanAdapter span = ActiveSpanManager.getSpan(exchange);
                if (span != null) {
                    if (log.isTraceEnabled()) {
                        log.trace("Tracing: finish server span={}", span);
                    }
                    SpanDecorator sd = getSpanDecorator(route.getEndpoint());
                    sd.post(span, exchange, route.getEndpoint());
                    finishSpan(span);
                    ActiveSpanManager.deactivate(exchange);
                } else {
                    log.warn("Tracing: could not find managed span for exchange: {}", exchange);
                }
            } catch (Exception t) {
                //  This exception is ignored
                log.warn("Tracing: Failed to capture tracing data. This exception is ignored.", t);
            }
        }
    }

    private final class TracingEventNotifier extends EventNotifierSupport {

        public TracingEventNotifier() {
            // ignore these
            setIgnoreCamelContextEvents(true);
            setIgnoreCamelContextInitEvents(true);
            setIgnoreRouteEvents(true);
            // we need also async processing started events
            setIgnoreExchangeAsyncProcessingStartedEvents(false);
        }

        @Override
        public void notify(CamelEvent event) throws Exception {
            try {
                if (event instanceof CamelEvent.ExchangeSendingEvent) {
                    CamelEvent.ExchangeSendingEvent ese = (CamelEvent.ExchangeSendingEvent) event;

                    SpanDecorator sd = getSpanDecorator(ese.getEndpoint());
                    if (shouldExclude(sd, ese.getExchange(), ese.getEndpoint())) {
                        return;
                    }
                    SpanAdapter parent = ActiveSpanManager.getSpan(ese.getExchange());
                    InjectAdapter injectAdapter = sd.getInjectAdapter(ese.getExchange().getIn().getHeaders(), encoding);
                    SpanAdapter span = startSendingEventSpan(sd.getOperationName(ese.getExchange(), ese.getEndpoint()),
                            sd.getInitiatorSpanKind(), parent, ese.getExchange(), injectAdapter);
                    Exchange exchange = ese.getExchange();
                    if (exchange != null) {
                        traceLogUtils.recordMessageTrace(exchange, span);
                    }
                    sd.pre(span, ese.getExchange(), ese.getEndpoint());
                    inject(span, injectAdapter);
                    ActiveSpanManager.activate(ese.getExchange(), span);
                    if (log.isTraceEnabled()) {
                        log.trace("Tracing: start client span: {}", span);
                    }
                } else if (event instanceof CamelEvent.ExchangeSentEvent) {
                    CamelEvent.ExchangeSentEvent ese = (CamelEvent.ExchangeSentEvent) event;
                    SpanDecorator sd = getSpanDecorator(ese.getEndpoint());
                    if (shouldExclude(sd, ese.getExchange(), ese.getEndpoint())) {
                        return;
                    }

                    SpanAdapter span = ActiveSpanManager.getSpan(ese.getExchange());
                    if (span != null) {
                        if (log.isTraceEnabled()) {
                            log.trace("Tracing: stop client span: {}", span);
                        }
                        sd.post(span, ese.getExchange(), ese.getEndpoint());
                        ActiveSpanManager.deactivate(ese.getExchange());
                        finishSpan(span);
                    } else {
                        log.warn("Tracing: could not find managed span for exchange: {}", ese.getExchange());
                    }
                } else if (event instanceof CamelEvent.ExchangeAsyncProcessingStartedEvent) {
                    CamelEvent.ExchangeAsyncProcessingStartedEvent eap = (CamelEvent.ExchangeAsyncProcessingStartedEvent) event;

                    // no need to filter scopes here. It's ok to close a scope multiple times and
                    // implementations check if scope being disposed is current
                    // and should not do anything if scopes don't match.
                    ActiveSpanManager.endScope(eap.getExchange());
                }
            } catch (Exception t) {
                // This exception is ignored
                log.warn("Tracing: Failed to capture tracing data. This exception is ignored.", t);
            }
        }

        private boolean shouldExclude(SpanDecorator sd, Exchange exchange, Endpoint endpoint) {
            return !sd.newSpan()
                   || isExcluded(exchange, endpoint);
        }
    }

    private boolean isExcluded(Exchange exchange, Endpoint endpoint) {
        String url = endpoint.getEndpointUri();
        return url != null && url.startsWith(EXTRACT_PATTERN);
    }

    private static final class TracingLogListener implements LogListener {

        @Override
        public String onLog(Exchange exchange, CamelLogger camelLogger, String message) {
            try {
                SpanAdapter span = ActiveSpanManager.getSpan(exchange);
                if (span != null) {
                    Map<String, String> fields = new HashMap<>();
                    fields.put("message", message);
                    span.log(fields);
                }
            } catch (Exception t) {
                // This exception is ignored
                log.warn("Tracing: Failed to capture tracing data. This exception is ignored.", t);
            }
            return message;
        }
    }
}
