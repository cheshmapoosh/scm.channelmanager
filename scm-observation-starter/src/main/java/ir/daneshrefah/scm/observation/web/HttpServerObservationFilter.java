package ir.daneshrefah.scm.observation.web;

import ir.daneshrefah.scm.observation.CorrelationType;
import ir.daneshrefah.scm.observation.ObservationIds;
import ir.daneshrefah.scm.observation.ObservationScope;
import ir.daneshrefah.scm.observation.ScmObservation;
import ir.daneshrefah.scm.observation.TraceContext;
import ir.daneshrefah.scm.observation.TraceContextHolder;
import ir.daneshrefah.scm.observation.attributes.trace.CommonTraceAttributes;
import ir.daneshrefah.scm.observation.policy.ObservationSignal;
import ir.daneshrefah.scm.observation.policy.ObservationSignalPolicy;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class HttpServerObservationFilter extends OncePerRequestFilter {
    public static final String ERROR_ATTRIBUTE = HttpServerObservationFilter.class.getName() + ".error";
    private static final String OBSERVED_ATTRIBUTE = HttpServerObservationFilter.class.getName() + ".observed";
    private static final String CORRELATION_HEADER = "X-Correlation-ID";
    private static final String FORWARDED_FOR_HEADER = "X-Forwarded-For";
    private static final String REAL_IP_HEADER = "X-Real-IP";
    private static final String SPRING_ROUTE_ATTRIBUTE = "org.springframework.web.servlet.HandlerMapping.bestMatchingPattern";

    private final ScmObservation observation;
    private final ObservationSignalPolicy signalPolicy;
    private final String spanName;

    public HttpServerObservationFilter(
            ScmObservation observation,
            ObservationSignalPolicy signalPolicy,
            String spanName
    ) {
        this.observation = observation;
        this.signalPolicy = signalPolicy;
        this.spanName = textOrDefault(spanName, "http.server.request");
    }

    public static void recordException(ServletRequest request, Throwable throwable) {
        if (request != null && throwable != null) {
            request.setAttribute(ERROR_ATTRIBUTE, throwable);
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return request != null && Boolean.TRUE.equals(request.getAttribute(OBSERVED_ATTRIBUTE));
    }

    @Override
    protected boolean shouldNotFilterAsyncDispatch() {
        return true;
    }

    @Override
    protected boolean shouldNotFilterErrorDispatch() {
        return true;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        if (observation == null || signalPolicy == null || !signalPolicy.isEnabled(ObservationSignal.TRACE)) {
            filterChain.doFilter(request, response);
            return;
        }

        request.setAttribute(OBSERVED_ATTRIBUTE, Boolean.TRUE);
        ObservationScope scope = observation.trace()
                .source(HttpServerObservationFilter.class)
                .span(spanName)
                .spanKind("server")
                .action(spanName)
                .correlationId(correlationId(request))
                .correlationType(CorrelationType.REQUEST.value())
                .traceId(ObservationIds.traceId())
                .parentSpanId("")
                .attribute(CommonTraceAttributes.HTTP_METHOD, request.getMethod())
                .attribute(CommonTraceAttributes.URL_PATH, path(request))
                .attribute(CommonTraceAttributes.CLIENT_IP, clientIp(request))
                .start();

        MdcSnapshot mdcSnapshot = putTraceMdc();
        Throwable thrown = null;
        try {
            filterChain.doFilter(request, response);
        } catch (Throwable ex) {
            thrown = ex;
            recordException(request, ex);
            throw ex;
        } finally {
            try {
                int status = status(response);
                Throwable error = firstThrowable(thrown, request.getAttribute(ERROR_ATTRIBUTE));
                scope.attribute(CommonTraceAttributes.HTTP_ROUTE, route(request))
                        .attribute(CommonTraceAttributes.HTTP_STATUS_CODE, status);
                if (error != null) {
                    scope.failure(error);
                } else if (status >= 400) {
                    scope.failure();
                } else {
                    scope.success();
                }
            } finally {
                try {
                    scope.close();
                } finally {
                    mdcSnapshot.restore();
                    request.removeAttribute(OBSERVED_ATTRIBUTE);
                }
            }
        }
    }

    private MdcSnapshot putTraceMdc() {
        MdcSnapshot snapshot = new MdcSnapshot(
                MDC.get("correlationId"),
                MDC.get("correlationType"),
                MDC.get("traceId"),
                MDC.get("spanId")
        );
        TraceContext context = TraceContextHolder.current();
        if (context != null) {
            putIfPresent("correlationId", context.correlationId());
            putIfPresent("correlationType", context.correlationType());
            putIfPresent("traceId", context.traceId());
            putIfPresent("spanId", context.spanId());
        }
        return snapshot;
    }

    private String correlationId(HttpServletRequest request) {
        String header = request == null ? null : request.getHeader(CORRELATION_HEADER);
        if (header != null && !header.isBlank()) {
            return header.trim();
        }
        String mdcCorrelationId = MDC.get("correlationId");
        return mdcCorrelationId == null || mdcCorrelationId.isBlank()
                ? ObservationIds.correlationId()
                : mdcCorrelationId.trim();
    }

    private String path(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String uri = request.getRequestURI();
        return uri == null || uri.isBlank() ? request.getServletPath() : uri;
    }

    private String route(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        Object route = request.getAttribute(SPRING_ROUTE_ATTRIBUTE);
        return route == null ? null : String.valueOf(route);
    }

    private String clientIp(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String forwardedFor = request.getHeader(FORWARDED_FOR_HEADER);
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            int comma = forwardedFor.indexOf(',');
            return comma >= 0 ? forwardedFor.substring(0, comma).trim() : forwardedFor.trim();
        }
        String realIp = request.getHeader(REAL_IP_HEADER);
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }
        return request.getRemoteAddr();
    }

    private int status(HttpServletResponse response) {
        return response == null || response.getStatus() <= 0 ? HttpServletResponse.SC_OK : response.getStatus();
    }

    private Throwable firstThrowable(Throwable thrown, Object recorded) {
        if (thrown != null) {
            return thrown;
        }
        return recorded instanceof Throwable throwable ? throwable : null;
    }

    private void putIfPresent(String key, String value) {
        if (value != null && !value.isBlank()) {
            MDC.put(key, value.trim());
        }
    }

    private String textOrDefault(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value.trim();
    }

    private record MdcSnapshot(String correlationId, String correlationType, String traceId, String spanId) {
        private void restore() {
            restore("correlationId", correlationId);
            restore("correlationType", correlationType);
            restore("traceId", traceId);
            restore("spanId", spanId);
        }

        private void restore(String key, String previousValue) {
            if (previousValue == null) {
                MDC.remove(key);
            } else {
                MDC.put(key, previousValue);
            }
        }
    }
}
