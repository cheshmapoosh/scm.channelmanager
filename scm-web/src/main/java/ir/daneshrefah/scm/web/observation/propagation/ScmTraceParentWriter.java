package ir.daneshrefah.scm.web.observation.propagation;

import ir.daneshrefah.scm.observation.starter.TraceContext;
import ir.daneshrefah.scm.observation.starter.TraceFlags;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Optional;

@Component
public class ScmTraceParentWriter {
    public static final String TRACEPARENT = "traceparent";
    public static final String TRACEPARENT_VERSION = "00";

    public Optional<String> format(TraceContext context) {
        if (context == null
                || !ScmTraceParent.isValidTraceId(context.traceId())
                || !ScmTraceParent.isValidParentId(context.spanId())) {
            return Optional.empty();
        }
        String traceId = context.traceId().trim().toLowerCase(Locale.ROOT);
        String spanId = context.spanId().trim().toLowerCase(Locale.ROOT);
        String traceFlags = TraceFlags.normalizeOrDefault(context.traceFlags());
        return Optional.of(TRACEPARENT_VERSION + "-" + traceId + "-" + spanId + "-" + traceFlags);
    }

    public void write(HttpHeaders headers, TraceContext context) {
        if (headers == null) {
            return;
        }
        format(context).ifPresent(value -> headers.set(TRACEPARENT, value));
    }
}
