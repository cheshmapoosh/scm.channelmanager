package ir.daneshrefah.scm.cache.client.utility.lock.aspect;

import ir.daneshrefah.scm.cache.client.utility.lock.LockUtility;
import ir.daneshrefah.scm.cache.client.utility.lock.annotation.WithLock;
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
public class WithLockAspect {

    private static final String DEFAULT_GLOBAL_KEY = "global";
    private static final String DEFAULT_ANONYMOUS_KEY = "anonymous";

    private final LockUtility lockUtility;
    private final ExpressionParser expressionParser = new SpelExpressionParser();
    private final ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

    @Around("@annotation(withLock)")
    public Object applyLock(ProceedingJoinPoint joinPoint, WithLock withLock) {
        String lockName = resolveLockName(joinPoint, withLock);
        Duration waitTime = resolveWaitTime(withLock);

        log.debug("Lock guard requested: lock='{}', waitTime={}", lockName, waitTime);
        return lockUtility.executeWithLock(lockName, waitTime, proceedCallable(joinPoint));
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

    private Duration resolveWaitTime(WithLock withLock) {
        if (withLock.waitMillis() < 0) {
            return null;
        }
        if (withLock.waitMillis() == 0) {
            return Duration.ZERO;
        }
        return Duration.ofMillis(withLock.waitMillis());
    }

    private String resolveLockName(ProceedingJoinPoint joinPoint, WithLock withLock) {
        if (!StringUtils.hasText(withLock.name())) {
            throw new IllegalArgumentException("WithLock name must not be blank. method=" + resolveMethodKey(joinPoint));
        }

        String keyPart;
        if (StringUtils.hasText(withLock.key())) {
            keyPart = evaluateExpressionKey(joinPoint, withLock.key());
        } else {
            if (withLock.perUser() && withLock.global()) {
                throw new IllegalArgumentException("WithLock cannot be both perUser and global. method=" + resolveMethodKey(joinPoint));
            }
            if (withLock.global()) {
                keyPart = DEFAULT_GLOBAL_KEY;
            } else if (withLock.perUser()) {
                keyPart = resolveMethodKey(joinPoint) + "::uid::" + resolveCurrentUserKey();
            } else {
                keyPart = resolveMethodKey(joinPoint);
            }
        }
        return withLock.name().trim() + "::" + keyPart;
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
            log.debug("Spring security context is not available. lock key falls back to anonymous.");
        } catch (Exception exception) {
            log.debug("Could not resolve principal from security context. lock key falls back to anonymous.", exception);
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
            throw new IllegalArgumentException("WithLock key expression returned null: '" + expressionText + "'");
        }
        String resolved = value.toString().trim();
        if (!StringUtils.hasText(resolved)) {
            throw new IllegalArgumentException("WithLock key expression returned blank value: '" + expressionText + "'");
        }
        return resolved;
    }
}
