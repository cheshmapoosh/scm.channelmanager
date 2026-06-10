package ir.daneshrefah.scm.observation.web;

import ir.daneshrefah.scm.observation.ObservationContext;
import ir.daneshrefah.scm.observation.ObservationIds;
import ir.daneshrefah.scm.observation.policy.ObservationSignal;
import ir.daneshrefah.scm.observation.policy.ObservationSignalPolicy;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class ObservationMdcFilter extends OncePerRequestFilter {
    private static final String CORRELATION_HEADER = "X-Correlation-ID";

    private final ObservationSignalPolicy signalPolicy;
    private final ObservationContext context;

    public ObservationMdcFilter(ObservationSignalPolicy signalPolicy, ObservationContext context) {
        this.signalPolicy = signalPolicy;
        this.context = context;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        if (signalPolicy == null || !signalPolicy.isEnabled(ObservationSignal.LOG)) {
            filterChain.doFilter(request, response);
            return;
        }
        String previousCorrelationId = MDC.get("correlationId");
        String previousTraceId = MDC.get("traceId");
        String previousSpanId = MDC.get("spanId");
        try {
            String correlationId = correlationId(request);
            MDC.put("correlationId", correlationId);
            putIfPresent("scmAppName", context.appName());
            putIfPresent("scmAppProfile", context.appProfile());
            filterChain.doFilter(request, response);
        } finally {
            restore("correlationId", previousCorrelationId);
            restore("traceId", previousTraceId);
            restore("spanId", previousSpanId);
            MDC.remove("scmAppName");
            MDC.remove("scmAppProfile");
        }
    }

    private String correlationId(HttpServletRequest request) {
        String header = request == null ? null : request.getHeader(CORRELATION_HEADER);
        return header == null || header.isBlank() ? ObservationIds.correlationId() : header.trim();
    }

    private void putIfPresent(String key, String value) {
        if (value != null && !value.isBlank()) {
            MDC.put(key, value.trim());
        }
    }

    private void restore(String key, String previousValue) {
        if (previousValue == null) {
            MDC.remove(key);
        } else {
            MDC.put(key, previousValue);
        }
    }
}
