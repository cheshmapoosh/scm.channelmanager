package ir.daneshrefah.scm.cache.client.utility.concurrencylimit.aspect;

import ir.daneshrefah.scm.cache.client.utility.concurrencylimit.ConcurrencyLimiterUtility;
import ir.daneshrefah.scm.cache.client.utility.concurrencylimit.annotation.WithConcurrencyLimit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.concurrent.Callable;

@Aspect
@RequiredArgsConstructor
@Slf4j
public class WithConcurrencyLimitAspect {

    private static final String DEFAULT_GLOBAL_KEY = "global";
    private static final String DEFAULT_ANONYMOUS_KEY = "anonymous";

    private final ConcurrencyLimiterUtility concurrencyLimiterUtility;
    private final ExpressionParser expressionParser = new SpelExpressionParser();
    private final ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

    @Around("@annotation(withConcurrencyLimit)")
    public Object applyConcurrencyLimit(ProceedingJoinPoint joinPoint, WithConcurrencyLimit withConcurrencyLimit) {
        String limitName = resolveLimitName(joinPoint, withConcurrencyLimit);
        int maxConcurrentExecutions = resolveMaxConcurrentExecutions(withConcurrencyLimit);
        Duration waitTime = resolveWaitTime(withConcurrencyLimit);

        log.debug("Concurrency limit requested: name='{}', maxConcurrentExecutions={}, waitTime={}",
                limitName, maxConcurrentExecutions, waitTime);
        return concurrencyLimiterUtility.executeWithConcurrencyLimit(
                limitName,
                maxConcurrentExecutions,
                waitTime,
                proceedCallable(joinPoint)
        );
    }

    private Callable<Object> proceedCallable(ProceedingJoinPoint joinPoint) {
        return () -> {
            try {
                return joinPoint.proceed();
            } catch (RuntimeException exception) {
                throw exception;
            } catch (Throwable throwable) {
                throw new Exception(throwable);
            }
        };
    }

    private int resolveMaxConcurrentExecutions(WithConcurrencyLimit withConcurrencyLimit) {
        if (withConcurrencyLimit.maxConcurrentExecutions() <= 0) {
            throw new IllegalArgumentException("WithConcurrencyLimit maxConcurrentExecutions must be greater than zero");
        }
        return withConcurrencyLimit.maxConcurrentExecutions();
    }

    private Duration resolveWaitTime(WithConcurrencyLimit withConcurrencyLimit) {
        if (withConcurrencyLimit.waitMillis() < 0) {
            return null;
        }
        if (withConcurrencyLimit.waitMillis() == 0) {
            return Duration.ZERO;
        }
        return Duration.ofMillis(withConcurrencyLimit.waitMillis());
    }

    private String resolveLimitName(ProceedingJoinPoint joinPoint, WithConcurrencyLimit withConcurrencyLimit) {
        if (!StringUtils.hasText(withConcurrencyLimit.name())) {
            throw new IllegalArgumentException("WithConcurrencyLimit name must not be blank. method=" + resolveMethodKey(joinPoint));
        }

        String keyPart;
        if (StringUtils.hasText(withConcurrencyLimit.key())) {
            keyPart = evaluateExpressionKey(joinPoint, withConcurrencyLimit.key());
        } else {
            if (withConcurrencyLimit.perUser() && withConcurrencyLimit.global()) {
                throw new IllegalArgumentException("WithConcurrencyLimit cannot be both perUser and global. method=" + resolveMethodKey(joinPoint));
            }
            if (withConcurrencyLimit.global()) {
                keyPart = DEFAULT_GLOBAL_KEY;
            } else if (withConcurrencyLimit.perUser()) {
                keyPart = resolveMethodKey(joinPoint) + "::uid::" + resolveCurrentUserKey();
            } else {
                keyPart = resolveMethodKey(joinPoint);
            }
        }

        return withConcurrencyLimit.name().trim() + "::" + keyPart;
    }

    private String resolveMethodKey(ProceedingJoinPoint joinPoint) {
        return joinPoint.getSignature().getDeclaringTypeName() + "::" + joinPoint.getSignature().getName();
    }

    private String resolveCurrentUserKey() {
        try {
            Class<?> holderClass = Class.forName("org.springframework.security.core.context.SecurityContextHolder");
            Object context = holderClass.getMethod("getContext").invoke(null);
            if (context == null) {
                return DEFAULT_ANONYMOUS_KEY;
            }
            Object authentication = context.getClass().getMethod("getAuthentication").invoke(context);
            if (authentication == null) {
                return DEFAULT_ANONYMOUS_KEY;
            }
            Object name = authentication.getClass().getMethod("getName").invoke(authentication);
            if (name instanceof String stringName && StringUtils.hasText(stringName)) {
                return stringName.trim();
            }
        } catch (ClassNotFoundException exception) {
            log.debug("Spring security context is not available. concurrency-limit key falls back to anonymous.");
        } catch (Exception exception) {
            log.debug("Could not resolve principal from security context. concurrency-limit key falls back to anonymous.", exception);
        }
        return DEFAULT_ANONYMOUS_KEY;
    }

    private String evaluateExpressionKey(ProceedingJoinPoint joinPoint, String expressionText) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        MethodBasedEvaluationContext context = new MethodBasedEvaluationContext(
                joinPoint.getTarget(),
                method,
                joinPoint.getArgs(),
                parameterNameDiscoverer
        );
        context.setVariable("methodName", method.getName());
        context.setVariable("authenticationName", resolveCurrentUserKey());

        Expression expression = expressionParser.parseExpression(expressionText);
        Object value = expression.getValue(context);
        if (value == null) {
            throw new IllegalArgumentException("WithConcurrencyLimit key expression returned null: '" + expressionText + "'");
        }
        String resolved = value.toString().trim();
        if (!StringUtils.hasText(resolved)) {
            throw new IllegalArgumentException("WithConcurrencyLimit key expression returned blank value: '" + expressionText + "'");
        }
        return resolved;
    }
}
