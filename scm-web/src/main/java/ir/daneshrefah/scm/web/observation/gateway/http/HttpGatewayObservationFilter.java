package ir.daneshrefah.scm.web.observation.gateway.http;

import ir.daneshrefah.scm.observation.ObservationContext;
import ir.daneshrefah.scm.observation.ObservationIds;
import ir.daneshrefah.scm.observation.TraceContext;
import ir.daneshrefah.scm.observation.TraceContextHolder;
import ir.daneshrefah.scm.observation.gateway.GatewayObservationContext;
import ir.daneshrefah.scm.observation.gateway.GatewayObservationLifecycle;
import ir.daneshrefah.scm.observation.gateway.GatewayObservationRequest;
import ir.daneshrefah.scm.observation.gateway.GatewayObservationResult;
import ir.daneshrefah.scm.observation.gateway.GatewayObservationScope;
import ir.daneshrefah.scm.observation.gateway.GatewayProtocol;
import ir.daneshrefah.scm.web.observation.attributes.WebTraceAttributes;
import ir.daneshrefah.scm.web.observation.propagation.ScmTraceParent;
import ir.daneshrefah.scm.web.observation.propagation.ScmTraceParentParser;
import ir.daneshrefah.scm.web.observation.propagation.ScmTraceParentWriter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
public class HttpGatewayObservationFilter extends OncePerRequestFilter {
    private static final String DEFAULT_VALUE = "default";

    private final GatewayObservationLifecycle gatewayObservationLifecycle;
    private final ObservationContext observationContext;
    private final ScmTraceParentParser traceParentParser;

    public HttpGatewayObservationFilter(
            GatewayObservationLifecycle gatewayObservationLifecycle,
            ObservationContext observationContext,
            ScmTraceParentParser traceParentParser
    ) {
        this.gatewayObservationLifecycle = gatewayObservationLifecycle;
        this.observationContext = observationContext;
        this.traceParentParser = traceParentParser;
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
        ScmTraceParent incomingTraceParent = traceParentParser.parse(request.getHeader(ScmTraceParentWriter.TRACEPARENT))
                .orElse(null);
        String correlationId = ObservationIds.correlationId();
        GatewayObservationRequest observationRequest = GatewayObservationRequest.builder()
                .protocol(GatewayProtocol.HTTP)
                .gatewayName(textOrDefault(observationContext.gatewayName()))
                .channelCode(textOrDefault(observationContext.channelCode()))
                .correlationId(correlationId)
                .traceId(incomingTraceParent == null ? ObservationIds.traceId() : incomingTraceParent.traceId())
                .spanId(ObservationIds.spanId())
                .requestName(requestName(request))
                .clientAddress(clientIp(request))
                .attribute(WebTraceAttributes.HTTP_METHOD, textOrDefault(request.getMethod()))
                .attribute(WebTraceAttributes.URL_PATH, safePath(request))
                .attribute(WebTraceAttributes.QUERY_PRESENT, hasQuery(request))
                .attribute(WebTraceAttributes.CLIENT_IP, clientIp(request))
                .build();

        TraceContextHolder.Scope incomingParentScope = openIncomingParentScope(incomingTraceParent);
        GatewayObservationScope observationScope = null;

        try {
            observationScope = gatewayObservationLifecycle.start(observationRequest);
            GatewayObservationContext gatewayContext = observationScope.context();
            setRequestAttributes(request, gatewayContext);
            putMdc(gatewayContext);

            filterChain.doFilter(request, response);
            observationScope.success(GatewayObservationResult.builder()
                    .outcome("success")
                    .statusCode(response.getStatus())
                    .attribute(WebTraceAttributes.HTTP_STATUS_CODE, response.getStatus())
                    .build());
        } catch (Throwable ex) {
            int statusCode = statusCode(response, ex);
            observationScope.failure(GatewayObservationResult.builder()
                    .outcome("failure")
                    .statusCode(statusCode)
                    .errorCode(errorCode(statusCode, ex))
                    .error(ex)
                    .attribute(WebTraceAttributes.HTTP_STATUS_CODE, statusCode)
                    .build());
            rethrow(ex);
        } finally {
            if (observationScope != null) {
                observationScope.close();
            }
            clearMdc();
            closeIncomingParentScope(incomingParentScope);
        }
    }

    private void setRequestAttributes(HttpServletRequest request, GatewayObservationContext gatewayContext) {
        request.setAttribute(GatewayObservationContext.REQUEST_ATTRIBUTE, gatewayContext);
        request.setAttribute(GatewayObservationContext.CORRELATION_ID_ATTRIBUTE, gatewayContext.correlationId());
        request.setAttribute(GatewayObservationContext.TRACE_ID_ATTRIBUTE, gatewayContext.traceId());
        request.setAttribute(GatewayObservationContext.GATEWAY_SPAN_ID_ATTRIBUTE, gatewayContext.gatewaySpanId());
        request.setAttribute(GatewayObservationContext.GATEWAY_NAME_ATTRIBUTE, gatewayContext.gatewayName());
        request.setAttribute(GatewayObservationContext.CHANNEL_CODE_ATTRIBUTE, gatewayContext.channelCode());
    }

    private String requestName(HttpServletRequest request) {
        return "HTTP " + textOrDefault(request.getMethod());
    }

    private String clientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            int comma = forwardedFor.indexOf(',');
            String first = comma >= 0 ? forwardedFor.substring(0, comma) : forwardedFor;
            if (!first.isBlank()) {
                return first.trim();
            }
        }
        return textOrDefault(request.getRemoteAddr());
    }

    private String safePath(HttpServletRequest request) {
        return textOrDefault(request.getRequestURI());
    }

    private boolean hasQuery(HttpServletRequest request) {
        String query = request.getQueryString();
        return query != null && !query.isBlank();
    }

    private int statusCode(HttpServletResponse response, Throwable failure) {
        int statusCode = response.getStatus();
        if (failure != null && statusCode < 400) {
            return 500;
        }
        return statusCode;
    }

    private String errorCode(int statusCode, Throwable failure) {
        if (statusCode >= 400) {
            return "http.status." + statusCode;
        }
        return failure == null ? null : failure.getClass().getSimpleName();
    }

    private String textOrDefault(String value) {
        return value == null || value.isBlank() ? DEFAULT_VALUE : value.trim();
    }

    private void putMdc(GatewayObservationContext gatewayContext) {
        MDC.put("correlationId", gatewayContext.correlationId());
        MDC.put("traceId", gatewayContext.traceId());
        MDC.put("spanId", gatewayContext.gatewaySpanId());
        MDC.put("gatewayName", gatewayContext.gatewayName());
        MDC.put("channelCode", gatewayContext.channelCode());
        MDC.put("protocol", gatewayContext.protocol().value());
    }

    private void clearMdc() {
        MDC.remove("correlationId");
        MDC.remove("traceId");
        MDC.remove("spanId");
        MDC.remove("gatewayName");
        MDC.remove("channelCode");
        MDC.remove("protocol");
    }

    private TraceContextHolder.Scope openIncomingParentScope(ScmTraceParent traceParent) {
        if (traceParent == null) {
            return null;
        }
        return TraceContextHolder.open(new TraceContext(traceParent.traceId(), traceParent.parentId(), null, null));
    }

    private void closeIncomingParentScope(TraceContextHolder.Scope scope) {
        if (scope == null) {
            return;
        }
        scope.close();
    }

    private void rethrow(Throwable throwable) throws ServletException, IOException {
        if (throwable instanceof ServletException servletException) {
            throw servletException;
        }
        if (throwable instanceof IOException ioException) {
            throw ioException;
        }
        if (throwable instanceof RuntimeException runtimeException) {
            throw runtimeException;
        }
        if (throwable instanceof Error error) {
            throw error;
        }
        throw new ServletException(throwable);
    }
}
