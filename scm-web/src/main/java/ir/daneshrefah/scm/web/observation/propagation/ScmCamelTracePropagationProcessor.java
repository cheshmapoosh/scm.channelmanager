package ir.daneshrefah.scm.web.observation.propagation;

import ir.daneshrefah.scm.observation.starter.TraceContextHolder;
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
        traceParentWriter.format(TraceContextHolder.current())
                .ifPresent(value -> message.setHeader(ScmTraceParentWriter.TRACEPARENT, value));
    }
}
