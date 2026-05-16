package ir.daneshrefah.scm.provider.rest.trace;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.provider.rest.config.RestProviderResolvedConfig;
import ir.daneshrefah.scm.provider.rest.model.RestProviderRequestSpec;
import lombok.RequiredArgsConstructor;
import org.apache.camel.Exchange;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
public class RestProviderTraceSupport {
    private final Tracer tracer;

    public ResponseEntity<String> clientSpan(
            Exchange exchange,
            RestProviderResolvedConfig config,
            RestProviderRequestSpec request,
            Supplier<ResponseEntity<String>> action
    ) {
        Span parent = exchange.getProperty(Message.CURRENT_OPEN_TELEMETRY_SPAN, Span.class);
        Span span = tracer.spanBuilder("rest-provider " + config.provider())
                .setSpanKind(SpanKind.CLIENT)
                .setParent(parent != null ? Context.current().with(parent) : Context.current())
                .startSpan();

        try (Scope ignored = span.makeCurrent()) {
            putMdc(span);
            span.setAttribute("scm.provider.name", config.provider());
            span.setAttribute("http.request.method", request.method().name());

            URI uri = request.uri();
            if (uri != null) {
                if (uri.getHost() != null) {
                    span.setAttribute("net.peer.name", uri.getHost());
                }
                if (uri.getPort() > 0) {
                    span.setAttribute("net.peer.port", uri.getPort());
                }
                span.setAttribute("url.path", uri.getPath() == null ? "" : uri.getPath());
            }

            ResponseEntity<String> response = action.get();
            if (response != null) {
                span.setAttribute("http.response.status_code", response.getStatusCode().value());
            }
            return response;
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

    private void putMdc(Span span) {
        if (span != null && span.getSpanContext().isValid()) {
            MDC.put("traceId", span.getSpanContext().getTraceId());
            MDC.put("spanId", span.getSpanContext().getSpanId());
        }
    }
}
