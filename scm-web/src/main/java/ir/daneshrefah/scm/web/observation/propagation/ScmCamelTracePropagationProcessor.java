package ir.daneshrefah.scm.web.observation.propagation;

import ir.daneshrefah.scm.core.integration.observability.CoreObservationTraceSupport;
import ir.daneshrefah.scm.observation.starter.TraceContext;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ScmCamelTracePropagationProcessor implements Processor {
    private static final List<String> REMOVED_PROPAGATION_HEADERS = List.of(
            ScmTraceParentWriter.TRACEPARENT,
            "tracestate",
            "X-Correlation-ID",
            "X-Correlation-Id",
            "X-SCM-Correlation-ID",
            "X-SCM-Trace-ID",
            "X-SCM-Span-ID",
            "X-SCM-Parent-Span-ID"
    );

    private final ScmTraceParentWriter traceParentWriter;

    public ScmCamelTracePropagationProcessor(ScmTraceParentWriter traceParentWriter) {
        this.traceParentWriter = traceParentWriter;
    }

    @Override
    public void process(Exchange exchange) {
        if (exchange == null) {
            return;
        }
        Message message = exchange.getMessage();
        if (message == null) {
            return;
        }
        REMOVED_PROPAGATION_HEADERS.forEach(message::removeHeader);
        traceParentWriter.format(activeContext(exchange))
                .ifPresent(value -> message.setHeader(ScmTraceParentWriter.TRACEPARENT, value));
    }

    private TraceContext activeContext(Exchange exchange) {
        TraceContext operation = exchange.getProperty(
                CoreObservationTraceSupport.OPERATION_CONTEXT_PROPERTY,
                TraceContext.class
        );
        if (operation != null) {
            return operation;
        }
        TraceContext service = exchange.getProperty(
                CoreObservationTraceSupport.SERVICE_CONTEXT_PROPERTY,
                TraceContext.class
        );
        if (service != null) {
            return service;
        }
        return exchange.getProperty(
                CoreObservationTraceSupport.GATEWAY_CONTEXT_PROPERTY,
                TraceContext.class
        );
    }
}
