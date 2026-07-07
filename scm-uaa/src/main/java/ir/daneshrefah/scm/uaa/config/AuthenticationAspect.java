package ir.daneshrefah.scm.uaa.config;

import ir.daneshrefah.scm.observation.ObservationScope;
import ir.daneshrefah.scm.uaa.observation.UaaObservation;
import ir.daneshrefah.scm.uaa.security.authentication.token.PreAuthenticationToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AccessTokenAuthenticationToken;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;

@Aspect
@Component
@ConditionalOnProperty(name = "scm.observation.trace.aspect.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class AuthenticationAspect {

    private final UaaObservation observation;

    @Around("execution(* authenticate(..)) && args(authentication) && within(ir.daneshrefah.scm.uaa.security.oauth2.grant.legacy.provider.LegacyPasswordGrantAuthenticationProvider)")
    public Object traceAuthenticate(ProceedingJoinPoint joinPoint, Authentication authentication) throws Throwable {
        UaaObservation.AuthContext ctx = authContext(authentication)
                .withJwtPresent(false);
        ObservationScope scope = observation.traceAuth(ctx);
        observation.authStarted(ctx);
        try {
            Object result = joinPoint.proceed();
            UaaObservation.AuthContext completed = ctx
                    .withResult("success")
                    .withJwtPresent(hasAccessToken(result))
                    .withJwtUsername(safeUsername(authentication));
            observation.authAttributes(scope, completed);
            scope.success();
            observation.authCompleted(completed);
            return result;
        } catch (AuthenticationException ex) {
            UaaObservation.AuthContext failed = ctx
                    .withResult("failure")
                    .withFailureReason(safeMessage(ex));
            observation.authAttributes(scope, failed);
            scope.failure(ex);
            observation.authFailed(failed, ex);
            log.trace("Authentication failed: {}", safeMessage(ex));
            throw ex;
        } catch (Throwable ex) {
            UaaObservation.AuthContext failed = ctx
                    .withResult("failure")
                    .withFailureReason(safeMessage(ex));
            observation.authAttributes(scope, failed);
            scope.failure(ex);
            observation.authFailed(failed, ex);
            log.error("Unexpected error during authentication: {}", safeMessage(ex));
            throw ex;
        } finally {
            scope.close();
        }
    }

    private UaaObservation.AuthContext authContext(Authentication authentication) {
        return new UaaObservation.AuthContext(
                "oauth2",
                clientId(authentication),
                grantType(authentication),
                "authenticate",
                "authenticate",
                "started",
                null,
                safeUsername(authentication),
                remoteAddress(authentication),
                null,
                "access_token",
                null,
                null,
                safeUsername(authentication),
                null,
                null
        );
    }

    private boolean hasAccessToken(Object result) {
        return result instanceof OAuth2AccessTokenAuthenticationToken token && token.getAccessToken() != null;
    }

    private String remoteAddress(Authentication authentication) {
        if (authentication instanceof PreAuthenticationToken preAuthenticationToken) {
            return textOrNull(preAuthenticationToken.getRemoteAddress());
        }
        Object details = authentication == null ? null : authentication.getDetails();
        if (details instanceof WebAuthenticationDetails webAuthenticationDetails) {
            return textOrNull(webAuthenticationDetails.getRemoteAddress());
        }
        return null;
    }

    private String safeUsername(Authentication authentication) {
        String username = null;
        if (authentication instanceof PreAuthenticationToken preAuthenticationToken) {
            username = preAuthenticationToken.getUsername();
        }
        if ((username == null || username.isBlank()) && authentication != null) {
            username = authentication.getName();
        }
        username = textOrNull(username);
        if (username == null) {
            return null;
        }
        username = username.replace('\r', ' ').replace('\n', ' ').trim();
       /* if (username.length() <= 4) {
            return "****";
        }
        String masked = username.substring(0, 2) + "***" + username.substring(username.length() - 2);*/
        return username.length() > 128 ? username.substring(0, 128) : username;
    }

    private String clientId(Authentication authentication) {
        if (authentication instanceof PreAuthenticationToken preAuthenticationToken) {
            return preAuthenticationToken.getClientId();
        }
        return null;
    }

    private String grantType(Authentication authentication) {
        if (authentication instanceof PreAuthenticationToken preAuthenticationToken
                && preAuthenticationToken.getGrantType() != null) {
            return String.valueOf(preAuthenticationToken.getGrantType());
        }
        return null;
    }

    private String textOrNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String safeMessage(Throwable exception) {
        if (exception == null || exception.getMessage() == null) {
            return null;
        }
        String message = exception.getMessage()
                .replace('\r', ' ')
                .replace('\n', ' ')
                .replaceAll("(?i)(password|token|authorization|client_secret|authorization_code|pin|otp|session[_-]?id|card[_-]?number|cookie|registry[_-]?token|mobile|national[_-]?code)\\s*[:=]\\s*\\S+", "$1=***")
                .trim();
        return message.length() > 300 ? message.substring(0, 300) : message;
    }
}
