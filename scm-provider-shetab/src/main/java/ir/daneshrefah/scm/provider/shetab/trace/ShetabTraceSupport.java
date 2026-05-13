package ir.daneshrefah.scm.provider.shetab.trace;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.provider.shetab.config.ShetabResolvedConfig;
import lombok.RequiredArgsConstructor;
import org.apache.camel.Exchange;
import org.jpos.iso.ISOMsg;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
public class ShetabTraceSupport {
    private final Tracer tracer;

    public <T> T clientSpan(Exchange exchange, ShetabResolvedConfig config, ISOMsg request, Supplier<T> action) {
        Span parent = exchange.getProperty(Message.CURRENT_OPEN_TELEMETRY_SPAN, Span.class);
        Span span = tracer.spanBuilder("shetab " + config.provider())
                .setSpanKind(SpanKind.CLIENT)
                .setParent(parent != null ? Context.current().with(parent) : Context.current())
                .startSpan();

        try (Scope ignored = span.makeCurrent()) {
            putMdc(span);
            span.setAttribute("scm.provider.name", config.provider());
            span.setAttribute("net.peer.name", config.host());
            span.setAttribute("net.peer.port", config.port());
            span.setAttribute("shetab.iso.mti", safeMti(request));
            span.setAttribute("shetab.iso.stan", safeField(request, 11));
            span.setAttribute("shetab.iso.rrn", safeField(request, 37));
            return action.get();
        } catch (RuntimeException e) {
            span.recordException(e);
            throw e;
        } finally {
            span.end();
            MDC.remove("traceId");
            MDC.remove("spanId");
        }
    }

    public void enrichLogMdc(Exchange exchange) {
        Span span = exchange.getProperty(Message.CURRENT_OPEN_TELEMETRY_SPAN, Span.class);
        if (span == null) {
            span = Span.current();
        }
        putMdc(span);
    }

    public Map<String, String> currentTraceIds() {
        Span span = Span.current();
        if (span.getSpanContext().isValid()) {
            return Map.of(
                    "traceId", span.getSpanContext().getTraceId(),
                    "spanId", span.getSpanContext().getSpanId()
            );
        }
        return Map.of("traceId", "", "spanId", "");
    }

    private void putMdc(Span span) {
        if (span != null && span.getSpanContext().isValid()) {
            MDC.put("traceId", span.getSpanContext().getTraceId());
            MDC.put("spanId", span.getSpanContext().getSpanId());
        }
    }

    private String safeMti(ISOMsg msg) {
        try {
            return msg != null && msg.hasMTI() ? msg.getMTI() : "";
        } catch (Exception e) {
            return "";
        }
    }

    private String safeField(ISOMsg msg, int field) {
        try {
            return msg != null ? msg.getString(field) : "";
        } catch (Exception e) {
            return "";
        }
    }
}
