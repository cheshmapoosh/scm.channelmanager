package ir.daneshrefah.scm.cache.client.utility.ratelimit.aspect;

import ir.daneshrefah.scm.cache.client.utility.ratelimit.RateLimitExceededException;
import ir.daneshrefah.scm.cache.client.utility.ratelimit.RateLimitResult;
import ir.daneshrefah.scm.cache.client.utility.ratelimit.RateLimiterUtility;
import ir.daneshrefah.scm.cache.client.utility.ratelimit.annotation.RateLimiter;
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

@Aspect
@RequiredArgsConstructor
@Slf4j
public class RateLimiterAspect {

    private static final String DEFAULT_GLOBAL_KEY = "global";
    private static final String DEFAULT_ANONYMOUS_KEY = "anonymous";

    private final RateLimiterUtility rateLimiterUtility;
    private final ExpressionParser expressionParser = new SpelExpressionParser();
    private final ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

    @Around("@annotation(rateLimiter)")
    public Object applyRateLimit(ProceedingJoinPoint joinPoint, RateLimiter rateLimiter) throws Throwable {
        int requestedTokens = Math.max(1, rateLimiter.tokens());
        String key = resolveRateLimitKey(joinPoint, rateLimiter);
        RateLimitResult result = rateLimiterUtility.tryConsume(rateLimiter.bucket(), key, requestedTokens);

        log.debug("Rate limiter checked: bucket='{}', key='{}', allowed={}, remaining={}",
                result.bucketName(), result.key(), result.allowed(), result.remainingTokens());

        if (!result.allowed()) {
            log.warn("Rate limit exceeded: bucket='{}', key='{}', retryAfterSeconds={}",
                    result.bucketName(), result.key(), result.retryAfterSeconds());
            throw new RateLimitExceededException(result);
        }
        return joinPoint.proceed();
    }

    private String resolveRateLimitKey(ProceedingJoinPoint joinPoint, RateLimiter rateLimiter) {
        if (StringUtils.hasText(rateLimiter.key())) {
            return evaluateExpressionKey(joinPoint, rateLimiter.key());
        }

        if (rateLimiter.perUser() && rateLimiter.global()) {
            throw new IllegalArgumentException("RateLimiter cannot be both perUser and global. method=" + resolveMethodKey(joinPoint));
        }
        if (rateLimiter.global()) {
            return DEFAULT_GLOBAL_KEY;
        }
        if (rateLimiter.perUser()) {
            return resolveMethodKey(joinPoint) + "::uid::" + resolveCurrentUserKey();
        }
        return resolveMethodKey(joinPoint);
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
            log.debug("Spring security context is not available. rate-limit key falls back to anonymous.");
        } catch (Exception exception) {
            log.debug("Could not resolve principal from security context. rate-limit key falls back to anonymous.", exception);
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
            throw new IllegalArgumentException("RateLimiter key expression returned null: '" + expressionText + "'");
        }
        String resolved = value.toString().trim();
        if (!StringUtils.hasText(resolved)) {
            throw new IllegalArgumentException("RateLimiter key expression returned blank value: '" + expressionText + "'");
        }
        return resolved;
    }
}
