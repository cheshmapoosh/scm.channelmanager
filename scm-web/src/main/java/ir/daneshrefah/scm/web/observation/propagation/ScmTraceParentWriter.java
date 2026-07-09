package ir.daneshrefah.scm.web.observation.propagation;

import ir.daneshrefah.scm.observation.starter.TraceContext;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Optional;

@Component
public class ScmTraceParentWriter {
    public static final String TRACEPARENT = "traceparent";
    public static final String TRACEPARENT_VERSION = "00";
    public static final String SAMPLED_FLAGS = "01";

    public Optional<String> format(TraceContext context) {
        if (context == null
                || !ScmTraceParent.isValidTraceId(context.traceId())
                || !ScmTraceParent.isValidParentId(context.spanId())) {
            return Optional.empty();
        }
        String traceId = context.traceId().trim().toLowerCase(Locale.ROOT);
        String spanId = context.spanId().trim().toLowerCase(Locale.ROOT);
        return Optional.of(TRACEPARENT_VERSION + "-" + traceId + "-" + spanId + "-" + SAMPLED_FLAGS);
    }

    public void write(HttpHeaders headers, TraceContext context) {
        if (headers == null) {
            return;
        }
        format(context).ifPresent(value -> headers.set(TRACEPARENT, value));
    }
}
