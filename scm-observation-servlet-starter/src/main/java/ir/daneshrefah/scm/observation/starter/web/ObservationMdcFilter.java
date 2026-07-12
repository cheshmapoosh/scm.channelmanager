package ir.daneshrefah.scm.observation.starter.web;

import ir.daneshrefah.scm.observation.starter.ObservationContext;
import ir.daneshrefah.scm.observation.starter.ObservationIds;
import ir.daneshrefah.scm.observation.starter.gateway.GatewayObservationContext;
import ir.daneshrefah.scm.observation.starter.logging.ScmMdcKeys;
import ir.daneshrefah.scm.observation.starter.policy.ObservationSignal;
import ir.daneshrefah.scm.observation.starter.policy.ObservationSignalPolicy;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ObservationMdcFilter extends OncePerRequestFilter {
    private static final String CORRELATION_HEADER = "X-Correlation-ID";
    private static final String SCM_APP_NAME = "scmAppName";
    private static final String SCM_APP_PROFILE = "scmAppProfile";
    private static final List<String> OWNED_MDC_KEYS = List.of(
            ScmMdcKeys.CORRELATION_ID,
            ScmMdcKeys.CORRELATION_TYPE,
            ScmMdcKeys.TRACE_ID,
            ScmMdcKeys.SPAN_ID,
            SCM_APP_NAME,
            SCM_APP_PROFILE
    );

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
        MdcSnapshot snapshot = MdcSnapshot.capture(OWNED_MDC_KEYS);
        try {
            if (!gatewayOwnsCorrelation(request)) {
                String correlationId = correlationId(request);
                MDC.put(ScmMdcKeys.CORRELATION_ID, correlationId);
            }
            putIfPresent(SCM_APP_NAME, context.appName());
            putIfPresent(SCM_APP_PROFILE, context.appProfile());
            filterChain.doFilter(request, response);
        } finally {
            snapshot.restore();
        }
    }

    private boolean gatewayOwnsCorrelation(HttpServletRequest request) {
        return request != null && request.getAttribute(GatewayObservationContext.REQUEST_ATTRIBUTE) instanceof GatewayObservationContext;
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

    private record MdcSnapshot(Map<String, String> values) {
        private static MdcSnapshot capture(List<String> keys) {
            Map<String, String> values = new LinkedHashMap<>();
            for (String key : keys) {
                values.put(key, MDC.get(key));
            }
            return new MdcSnapshot(values);
        }

        private void restore() {
            values.forEach((key, previousValue) -> {
                if (previousValue == null) {
                    MDC.remove(key);
                } else {
                    MDC.put(key, previousValue);
                }
            });
        }
    }
}
