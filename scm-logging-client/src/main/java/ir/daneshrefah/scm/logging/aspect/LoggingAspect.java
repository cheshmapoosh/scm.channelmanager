package ir.daneshrefah.scm.logging.aspect;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import ir.daneshrefah.scm.logging.utils.SpanUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Objects;

@Aspect
@Component
@ConditionalOnProperty(name = "scm.log.aspect.enable", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class LoggingAspect {

    private final Tracer tracer;

    @Around("@within(org.springframework.web.bind.annotation.RestController)    &&" +
            "@annotation(org.springframework.web.bind.annotation.PutMapping)    ||" +
            "@annotation(org.springframework.web.bind.annotation.GetMapping)    ||" +
            "@annotation(org.springframework.web.bind.annotation.PostMapping)   ||" +
            "@annotation(org.springframework.web.bind.annotation.PatchMapping)  ||" +
            "@annotation(org.springframework.web.bind.annotation.DeleteMapping) ||" +
            "@annotation(org.springframework.web.bind.annotation.RequestMapping)&& " +
            "!@annotation(ir.daneshrefah.scm.logging.aspect.annotation.SkipLog)")
    private Object logAdviser(ProceedingJoinPoint joinPoint) throws Throwable {
        boolean hasError = false;
        HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
        Span span = tracer.spanBuilder(request.getServletPath()).setSpanKind(SpanKind.SERVER).startSpan();
        try (Scope rootScope = span.makeCurrent()) {
            SpanUtil.setRequestSpanAttributes(request, span);
            Object result = joinPoint.proceed();
            SpanUtil.setResponseSpanAttributes(result, span);
            return result;
        } catch (Throwable ex) {
            request.setAttribute("otel.span", span);
            SpanUtil.setException(ex, span);
            hasError = true;
            throw ex;
        } finally {
            if (!hasError) {
                span.setStatus(StatusCode.OK);
                span.end();
            }
        }
    }
}


