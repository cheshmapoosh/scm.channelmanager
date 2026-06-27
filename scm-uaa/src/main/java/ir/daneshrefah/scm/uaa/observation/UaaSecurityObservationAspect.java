package ir.daneshrefah.scm.uaa.observation;

import ir.daneshrefah.scm.observation.ObservationScope;
import ir.daneshrefah.scm.uaa.security.token.PreAuthenticationToken;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AccessTokenAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Locale;

@Aspect
@Component
@Order(Ordered.LOWEST_PRECEDENCE - 40)
@RequiredArgsConstructor
public class UaaSecurityObservationAspect {
    private final UaaObservation observation;

    @Around("execution(public * ir.daneshrefah.scm.uaa.security.converter..*.convert(..))")
    public Object observeConverter(ProceedingJoinPoint joinPoint) throws Throwable {
        return observeOperation(joinPoint, "uaa.auth.pre_auth.convert", "uaa.auth", "pre_auth.convert");
    }

    @Around("""
            execution(public * ir.daneshrefah.scm.uaa.security.authenticationProvider..*.authenticate(..)) &&
            !within(ir.daneshrefah.scm.uaa.security.authenticationProvider.OAuth2GeneralAuthenticationProvider) &&
            !within(ir.daneshrefah.scm.uaa.security.authenticationProvider.JwtAuthenticationProvider)
            """)
    public Object observeAuthenticationProvider(ProceedingJoinPoint joinPoint) throws Throwable {
        return observeOperation(joinPoint, "uaa.auth.provider.authenticate", "uaa.auth", "provider.authenticate");
    }

    @Around("execution(public * ir.daneshrefah.scm.uaa.security.authenticationProvider.JwtAuthenticationProvider.authenticate(..))")
    public Object observeJwtValidation(ProceedingJoinPoint joinPoint) throws Throwable {
        UaaObservation.JwtContext ctx = jwtValidationContext(firstAuthentication(joinPoint));
        observation.jwtValidationStarted(ctx);
        ObservationScope scope = observation.traceJwtValidation(ctx);
        try {
            Object result = joinPoint.proceed();
            UaaObservation.JwtContext completed = jwtValidationCompletedContext(ctx, result);
            observation.jwtAttributes(scope, completed);
            scope.success();
            observation.jwtValidationCompleted(completed);
            return result;
        } catch (Throwable ex) {
            scope.failure(ex);
            observation.jwtValidationFailed(ctx, ex);
            throw ex;
        } finally {
            scope.close();
        }
    }

    @Around("execution(public * ir.daneshrefah.scm.uaa.security.token.generator.AuthenticationResponseTokenGenerator.getAccessToken(..))")
    public Object observeJwtIssue(ProceedingJoinPoint joinPoint) throws Throwable {
        UaaObservation.JwtContext ctx = new UaaObservation.JwtContext(false, "access_token", null,
                safeName(firstAuthentication(joinPoint)), safeName(firstAuthentication(joinPoint)), null, null);
        observation.jwtIssueStarted(ctx);
        ObservationScope scope = observation.traceJwtIssue(ctx);
        try {
            Object result = joinPoint.proceed();
            UaaObservation.JwtContext completed = jwtIssueCompletedContext(ctx, result);
            observation.jwtAttributes(scope, completed);
            scope.success();
            observation.jwtIssueCompleted(completed);
            return result;
        } catch (Throwable ex) {
            scope.failure(ex);
            observation.jwtIssueFailed(ctx, ex);
            throw ex;
        } finally {
            scope.close();
        }
    }

    @Around("execution(public * ir.daneshrefah.scm.uaa.security.token.generator..*.generateToken(..))")
    public Object observePreAuthTokenCreation(ProceedingJoinPoint joinPoint) throws Throwable {
        return observeOperation(joinPoint, "uaa.auth.pre_auth.convert", "uaa.auth", "pre_auth.token.create");
    }

    @Around("execution(public * ir.daneshrefah.scm.uaa.security.userDetails..*.load*(..))")
    public Object observeUserLoad(ProceedingJoinPoint joinPoint) throws Throwable {
        return observeOperation(joinPoint, "uaa.auth.user.load", "uaa.auth", "user.load");
    }

    @Around("execution(public * ir.daneshrefah.scm.uaa.config.DynamicRegisteredClientRepository.findBy*(..))")
    public Object observeClientLoad(ProceedingJoinPoint joinPoint) throws Throwable {
        return observeOperation(joinPoint, "uaa.auth.client.load", "uaa.auth", "client.load");
    }

    private Object observeOperation(
            ProceedingJoinPoint joinPoint,
            String spanName,
            String category,
            String stepName
    ) throws Throwable {
        UaaObservation.OperationContext ctx = new UaaObservation.OperationContext(
                spanName,
                category,
                stepName + "." + typeName(joinPoint) + "." + methodName(joinPoint),
                "started",
                null
        );
        ObservationScope scope = observation.traceSecurity(ctx);
        observation.operationStarted(ctx);
        try {
            Object result = joinPoint.proceed();
            UaaObservation.OperationContext completed = ctx.withResult("success");
            observation.operationAttributes(scope, completed);
            scope.success();
            observation.operationCompleted(completed);
            return result;
        } catch (Throwable ex) {
            UaaObservation.OperationContext failed = ctx
                    .withResult("failure")
                    .withFailureReason(observation.safeErrorMessage(ex));
            observation.operationAttributes(scope, failed);
            scope.failure(ex);
            observation.operationFailed(failed, ex);
            throw ex;
        } finally {
            scope.close();
        }
    }

    private UaaObservation.JwtContext jwtValidationContext(Authentication authentication) {
        if (authentication instanceof BearerTokenAuthenticationToken bearer) {
            return new UaaObservation.JwtContext(
                    true,
                    "bearer",
                    null,
                    null,
                    null,
                    observation.jwtMasked(bearer.getToken()),
                    null
            );
        }
        return new UaaObservation.JwtContext(authentication != null, "bearer", null, safeName(authentication),
                safeName(authentication), null, null);
    }

    private UaaObservation.JwtContext jwtValidationCompletedContext(UaaObservation.JwtContext ctx, Object result) {
        String username = result instanceof Authentication authentication ? safeName(authentication) : null;
        return new UaaObservation.JwtContext(
                ctx.present(),
                ctx.tokenType(),
                ctx.issuer(),
                username,
                username,
                ctx.jwtMasked(),
                ctx.expiration()
        );
    }

    private UaaObservation.JwtContext jwtIssueCompletedContext(UaaObservation.JwtContext ctx, Object result) {
        if (!(result instanceof OAuth2AccessTokenAuthenticationToken token) || token.getAccessToken() == null) {
            return ctx;
        }
        OAuth2AccessToken accessToken = token.getAccessToken();
        Instant expiresAt = accessToken.getExpiresAt();
        Object principal = token.getPrincipal();
        String username = principal instanceof Authentication authentication
                ? safeName(authentication)
                : principal == null ? null : safeText(String.valueOf(principal));
        return new UaaObservation.JwtContext(
                true,
                "access_token",
                null,
                username,
                username,
                observation.jwtMasked(accessToken.getTokenValue()),
                expiresAt == null ? null : expiresAt.toString()
        );
    }

    private Authentication firstAuthentication(ProceedingJoinPoint joinPoint) {
        for (Object arg : joinPoint.getArgs()) {
            if (arg instanceof Authentication authentication) {
                return authentication;
            }
        }
        return null;
    }

    private String safeName(Authentication authentication) {
        if (authentication == null) {
            return null;
        }
        String username = null;
        if (authentication instanceof PreAuthenticationToken preAuthenticationToken) {
            username = preAuthenticationToken.getUsername();
        }
        if (username == null || username.isBlank()) {
            username = authentication.getName();
        }
        return safeText(username);
    }

    @SuppressWarnings("unused")
    private String remoteAddress(Authentication authentication) {
        if (authentication instanceof PreAuthenticationToken preAuthenticationToken) {
            return safeText(preAuthenticationToken.getRemoteAddress());
        }
        Object details = authentication == null ? null : authentication.getDetails();
        if (details instanceof WebAuthenticationDetails webAuthenticationDetails) {
            return safeText(webAuthenticationDetails.getRemoteAddress());
        }
        return null;
    }

    private String safeText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String text = value.replace('\r', ' ').replace('\n', ' ').trim();
        return text.length() > 128 ? text.substring(0, 128) : text;
    }

    private String methodName(ProceedingJoinPoint joinPoint) {
        return ((MethodSignature) joinPoint.getSignature()).getMethod().getName();
    }

    private String typeName(ProceedingJoinPoint joinPoint) {
        Class<?> type = joinPoint.getTarget() == null
                ? ((MethodSignature) joinPoint.getSignature()).getDeclaringType()
                : joinPoint.getTarget().getClass();
        return normalize(type.getSimpleName());
    }

    private String normalize(String value) {
        String normalized = value == null ? "" : value
                .replaceAll("([a-z])([A-Z]+)", "$1.$2")
                .replaceAll("[^A-Za-z0-9]+", ".")
                .replaceAll("\\.+", ".")
                .replaceAll("^\\.|\\.$", "")
                .toLowerCase(Locale.ROOT);
        return normalized.isBlank() ? "unknown" : normalized;
    }
}
