package ir.daneshrefah.scm.uaa.observation;

import ir.daneshrefah.scm.observation.ObservationScope;
import ir.daneshrefah.scm.utils.constant.Constants;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 40)
@RequiredArgsConstructor
public class UaaControllerObservationAspect {
    private static final String X_CORRELATION_ID = "X-Correlation-Id";
    private static final String X_FORWARDED_FOR = "X-Forwarded-For";
    private static final String X_REAL_IP = "X-Real-IP";

    private final UaaObservation observation;

    @Around("""
            within(ir.daneshrefah.scm.uaa.controller..*) &&
            (@within(org.springframework.stereotype.Controller) || @within(org.springframework.web.bind.annotation.RestController))
            """)
    public Object observeController(ProceedingJoinPoint joinPoint) throws Throwable {
        ServletRequestAttributes attributes = servletRequestAttributes();
        HttpServletRequest request = attributes == null ? null : attributes.getRequest();
        HttpServletResponse response = attributes == null ? null : attributes.getResponse();
        UaaObservation.ControllerContext ctx = controllerContext(joinPoint, request, null);

        ObservationScope scope = observation.traceController(ctx);
        observation.controllerStarted(ctx);
        try {
            Object result = joinPoint.proceed();
            UaaObservation.ControllerContext completed = ctx
                    .withStatus(statusCode(result, response, HttpServletResponse.SC_OK))
                    .withResult("success");
            observation.controllerAttributes(scope, completed);
            scope.success();
            observation.controllerCompleted(completed);
            return result;
        } catch (Throwable ex) {
            UaaObservation.ControllerContext failed = ctx
                    .withStatus(statusCode(null, response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR))
                    .withResult("failure")
                    .withFailureReason(observation.safeErrorMessage(ex));
            observation.controllerAttributes(scope, failed);
            scope.failure(ex);
            observation.controllerFailed(failed, ex);
            throw ex;
        } finally {
            scope.close();
        }
    }

    private UaaObservation.ControllerContext controllerContext(
            ProceedingJoinPoint joinPoint,
            HttpServletRequest request,
            Integer statusCode
    ) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Class<?> type = joinPoint.getTarget() == null ? signature.getDeclaringType() : joinPoint.getTarget().getClass();
        return new UaaObservation.ControllerContext(
                controllerName(type),
                signature.getMethod().getName(),
                request == null ? null : request.getMethod(),
                requestPath(request),
                statusCode,
                clientIp(request),
                correlationId(request),
                "started",
                null
        );
    }

    private ServletRequestAttributes servletRequestAttributes() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        return attributes instanceof ServletRequestAttributes servletRequestAttributes ? servletRequestAttributes : null;
    }

    private String controllerName(Class<?> type) {
        if (type == null) {
            return "unknown";
        }
        String name = type.getSimpleName();
        return name.endsWith("Controller") ? name.substring(0, name.length() - "Controller".length()) : name;
    }

    private String requestPath(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String path = request.getRequestURI();
        if (!StringUtils.hasText(path)) {
            path = request.getServletPath();
        }
        return StringUtils.hasText(path) ? path : null;
    }

    private Integer statusCode(Object result, HttpServletResponse response, int defaultStatus) {
        if (result instanceof ResponseEntity<?> responseEntity) {
            return responseEntity.getStatusCode().value();
        }
        if (response != null && response.getStatus() > 0) {
            return response.getStatus();
        }
        return defaultStatus;
    }

    private String clientIp(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String forwardedFor = request.getHeader(X_FORWARDED_FOR);
        if (StringUtils.hasText(forwardedFor)) {
            int comma = forwardedFor.indexOf(',');
            return comma >= 0 ? forwardedFor.substring(0, comma).trim() : forwardedFor.trim();
        }
        String realIp = request.getHeader(X_REAL_IP);
        if (StringUtils.hasText(realIp)) {
            return realIp.trim();
        }
        return request.getRemoteAddr();
    }

    private String correlationId(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String correlationId = request.getHeader(X_CORRELATION_ID);
        if (!StringUtils.hasText(correlationId)) {
            correlationId = request.getHeader(Constants.SCM_PARAMETER_CORRELATION_ID);
        }
        if (!StringUtils.hasText(correlationId)) {
            correlationId = request.getHeader(Constants.SCM_PARAMETER_CLIENT_CORRELATION_ID);
        }
        return StringUtils.hasText(correlationId) ? correlationId.trim() : null;
    }
}
