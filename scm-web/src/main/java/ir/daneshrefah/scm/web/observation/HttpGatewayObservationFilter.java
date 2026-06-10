package ir.daneshrefah.scm.web.observation;

import ir.daneshrefah.scm.observation.ObservationContext;
import ir.daneshrefah.scm.observation.ObservationIds;
import ir.daneshrefah.scm.observation.attributes.ScmClientAttributes;
import ir.daneshrefah.scm.observation.attributes.ScmHttpAttributes;
import ir.daneshrefah.scm.observation.gateway.GatewayObservationContext;
import ir.daneshrefah.scm.observation.gateway.GatewayObservationLifecycle;
import ir.daneshrefah.scm.observation.gateway.GatewayObservationRequest;
import ir.daneshrefah.scm.observation.gateway.GatewayObservationResult;
import ir.daneshrefah.scm.observation.gateway.GatewayObservationScope;
import ir.daneshrefah.scm.observation.gateway.GatewayProtocol;
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
    private static final String CORRELATION_HEADER = "X-Correlation-Id";
    private static final String DEFAULT_VALUE = "default";

    private final GatewayObservationLifecycle gatewayObservationLifecycle;
    private final ObservationContext observationContext;

    public HttpGatewayObservationFilter(
            GatewayObservationLifecycle gatewayObservationLifecycle,
            ObservationContext observationContext
    ) {
        this.gatewayObservationLifecycle = gatewayObservationLifecycle;
        this.observationContext = observationContext;
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
        String correlationId = resolveCorrelationId(request);
        GatewayObservationRequest observationRequest = GatewayObservationRequest.builder()
                .protocol(GatewayProtocol.HTTP)
                .gatewayName(textOrDefault(observationContext.gatewayName()))
                .channelCode(textOrDefault(observationContext.channelCode()))
                .correlationId(correlationId)
                .traceId(ObservationIds.traceId())
                .spanId(ObservationIds.spanId())
                .requestName(requestName(request))
                .clientAddress(clientIp(request))
                .attribute(ScmHttpAttributes.METHOD, textOrDefault(request.getMethod()))
                .attribute(ScmHttpAttributes.URL_PATH, safePath(request))
                .attribute(ScmHttpAttributes.QUERY_PRESENT, hasQuery(request))
                .attribute(ScmClientAttributes.IP, clientIp(request))
                .build();

        GatewayObservationScope observationScope = gatewayObservationLifecycle.start(observationRequest);
        GatewayObservationContext gatewayContext = observationScope.context();
        setRequestAttributes(request, gatewayContext);
        response.setHeader(CORRELATION_HEADER, gatewayContext.correlationId());
        putMdc(gatewayContext);

        try {
            filterChain.doFilter(request, response);
            observationScope.success(GatewayObservationResult.builder()
                    .outcome("success")
                    .statusCode(response.getStatus())
                    .attribute(ScmHttpAttributes.STATUS_CODE, response.getStatus())
                    .build());
        } catch (Throwable ex) {
            int statusCode = statusCode(response, ex);
            observationScope.failure(GatewayObservationResult.builder()
                    .outcome("failure")
                    .statusCode(statusCode)
                    .errorCode(errorCode(statusCode, ex))
                    .error(ex)
                    .attribute(ScmHttpAttributes.STATUS_CODE, statusCode)
                    .build());
            rethrow(ex);
        } finally {
            observationScope.close();
            clearMdc();
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

    private String resolveCorrelationId(HttpServletRequest request) {
        String value = request.getHeader(CORRELATION_HEADER);
        return value == null || value.isBlank() ? ObservationIds.correlationId() : value.trim();
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
