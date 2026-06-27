package ir.daneshrefah.scm.observation.web;

import ir.daneshrefah.scm.observation.ObservationScope;
import ir.daneshrefah.scm.observation.ScmObservation;
import ir.daneshrefah.scm.observation.attributes.audit.ChangeEntityAuditAttributes;
import ir.daneshrefah.scm.observation.attributes.audit.ServiceExecuteAuditAttributes;
import ir.daneshrefah.scm.observation.attributes.metric.CommonMetricTags;
import ir.daneshrefah.scm.observation.attributes.trace.CommonTraceAttributes;
import ir.daneshrefah.scm.observation.metrics.CommonMetricNames;
import ir.daneshrefah.scm.observation.policy.ObservationSignal;
import ir.daneshrefah.scm.observation.policy.ObservationSignalPolicy;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import java.security.Principal;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class ObservationWebMvcInterceptor implements HandlerInterceptor {
    private static final String TRACE_SCOPE_ATTRIBUTE = ObservationWebMvcInterceptor.class.getName() + ".traceScope";
    private static final String START_NANOS_ATTRIBUTE = ObservationWebMvcInterceptor.class.getName() + ".startNanos";
    private static final String ACTOR_HEADER = "X-Actor-Username";

    private final ScmObservation observation;
    private final ObservationSignalPolicy signalPolicy;

    public ObservationWebMvcInterceptor(
            ScmObservation observation,
            ObservationSignalPolicy signalPolicy
    ) {
        this.observation = observation;
        this.signalPolicy = signalPolicy;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        request.setAttribute(START_NANOS_ATTRIBUTE, System.nanoTime());
        if (enabled(ObservationSignal.TRACE)) {
            ObservationScope scope = observation.trace()
                    .source(sourceClass(handler))
                    .span(operationName(request))
                    .spanKind("server")
                    .operation(operationName(request))
                    .correlationId(correlationId())
                    .attribute("http.method", request.getMethod())
                    .attribute("http.route", normalizedRoute(request))
                    .attribute("url.path", safePath(request))
                    .attribute("http.query.present", request.getQueryString() != null)
                    .start();
            request.setAttribute(TRACE_SCOPE_ATTRIBUTE, scope);
        }
        return true;
    }

    @Override
    public void afterCompletion(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            Exception exception
    ) {
        long durationMs = durationMs(request);
        String outcome = outcome(response, exception);
        finishTrace(request, response, outcome, exception, durationMs);
        writeAudit(request, response, handler, outcome, exception, durationMs);
        recordMetrics(request, response, outcome, durationMs);
    }

    private void finishTrace(
            HttpServletRequest request,
            HttpServletResponse response,
            String outcome,
            Exception exception,
            long durationMs
    ) {
        Object scopeAttribute = request.getAttribute(TRACE_SCOPE_ATTRIBUTE);
        if (!(scopeAttribute instanceof ObservationScope scope)) {
            return;
        }
        scope.outcome(outcome)
                .attribute("http.status_code", response.getStatus())
                .attribute("scm.operation.duration_ms", durationMs);
        if (exception != null) {
            scope.failure(exception).attribute(CommonTraceAttributes.ERROR_CODE, String.valueOf(response.getStatus()));
        }
        scope.close();
    }

    private void writeAudit(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            String outcome,
            Exception exception,
            long durationMs
    ) {
        if (!enabled(ObservationSignal.AUDIT) || !auditRequired(request)) {
            return;
        }
        var audit = observation.audit()
                .source(sourceClass(handler))
                .change()
                .action(auditAction(request))
                .outcome(outcome)
                .correlationId(correlationId())
                .userName(actor(request))
                .resource(resourceType(request), normalizedRoute(request))
                .attribute("http.method", request.getMethod())
                .attribute("http.route", normalizedRoute(request))
                .attribute("http.status_code", response.getStatus())
                .attribute("scm.operation.duration_ms", durationMs)
                .attribute(ServiceExecuteAuditAttributes.RESOURCE_ID, normalizedRoute(request));
        if (exception != null) {
            audit.failure(exception)
                    .attribute(ChangeEntityAuditAttributes.ERROR_CODE, String.valueOf(response.getStatus()));
        }
        audit.write();
    }

    private void recordMetrics(HttpServletRequest request, HttpServletResponse response, String outcome, long durationMs) {
        if (!enabled(ObservationSignal.METRIC)) {
            return;
        }
        observation.metric()
                .counter(CommonMetricNames.REQUESTS)
                .tag("http.method", request.getMethod())
                .tag("http.route", normalizedRoute(request))
                .tag("scm.operation.type", operationType(request))
                .tag(CommonMetricTags.OUTCOME, outcome)
                .tag("http.status_code", String.valueOf(response.getStatus()))
                .increment();
        observation.metric()
                .timer(CommonMetricNames.REQUEST_DURATION)
                .tag("http.method", request.getMethod())
                .tag("http.route", normalizedRoute(request))
                .tag("scm.operation.type", operationType(request))
                .tag(CommonMetricTags.OUTCOME, outcome)
                .record(durationMs, TimeUnit.MILLISECONDS);
    }

    private boolean auditRequired(HttpServletRequest request) {
        String method = method(request);
        if ("PUT".equals(method) || "PATCH".equals(method) || "DELETE".equals(method)) {
            return true;
        }
        String route = normalizedRoute(request).toLowerCase(Locale.ROOT);
        return "POST".equals(method)
                && (route.contains("/create")
                || route.contains("/put")
                || route.contains("/update")
                || route.contains("/remove")
                || route.contains("/clear")
                || route.contains("/delete")
                || route.contains("/evict"));
    }

    private String auditAction(HttpServletRequest request) {
        return "cache.http." + method(request).toLowerCase(Locale.ROOT) + "." + operationType(request);
    }

    private String operationName(HttpServletRequest request) {
        return method(request).toLowerCase(Locale.ROOT) + " " + normalizedRoute(request);
    }

    private String operationType(HttpServletRequest request) {
        String route = normalizedRoute(request).toLowerCase(Locale.ROOT);
        if (route.contains("/create")) {
            return "create";
        }
        if (route.contains("/update") || route.contains("/save-ttl")) {
            return "update";
        }
        if (route.contains("/remove") || route.contains("/delete")) {
            return "delete";
        }
        if (route.contains("/clear")) {
            return "clear";
        }
        if (route.contains("/evict")) {
            return "evict";
        }
        if (route.contains("/put") || "PUT".equals(method(request)) || "PATCH".equals(method(request))) {
            return "write";
        }
        return "read";
    }

    private String resourceType(HttpServletRequest request) {
        String route = normalizedRoute(request);
        if (route.contains("/maps") || route.contains("/{mapName}")) {
            return "cache.map";
        }
        if (route.contains("/list")) {
            return "cache.list";
        }
        if (route.contains("/session")) {
            return "cache.session";
        }
        return "cache";
    }

    private String normalizedRoute(HttpServletRequest request) {
        Object pattern = request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        if (pattern instanceof String route && !route.isBlank()) {
            return route.trim();
        }
        return safePath(request);
    }

    private String safePath(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri == null || uri.isBlank() ? "/" : uri;
    }

    private Class<?> sourceClass(Object handler) {
        if (handler instanceof HandlerMethod handlerMethod) {
            return handlerMethod.getBeanType();
        }
        return ObservationWebMvcInterceptor.class;
    }

    private String actor(HttpServletRequest request) {
        String actor = request.getHeader(ACTOR_HEADER);
        if (actor != null && !actor.isBlank()) {
            return actor.trim();
        }
        Principal principal = request.getUserPrincipal();
        return principal == null || principal.getName() == null || principal.getName().isBlank()
                ? null
                : principal.getName().trim();
    }

    private String correlationId() {
        String correlationId = MDC.get("correlationId");
        return correlationId == null || correlationId.isBlank() ? null : correlationId;
    }

    private String outcome(HttpServletResponse response, Exception exception) {
        if (exception != null || response.getStatus() >= 500) {
            return "failure";
        }
        return "success";
    }

    private long durationMs(HttpServletRequest request) {
        Object started = request.getAttribute(START_NANOS_ATTRIBUTE);
        if (started instanceof Long startedNanos) {
            return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedNanos);
        }
        return 0L;
    }

    private String method(HttpServletRequest request) {
        return request.getMethod() == null ? "GET" : request.getMethod().trim().toUpperCase(Locale.ROOT);
    }

    private boolean enabled(ObservationSignal signal) {
        return signalPolicy != null && signalPolicy.isEnabled(signal);
    }
}
