package ir.daneshrefah.scm.uaa.security.oauth2.grant.legacy;

import ir.daneshrefah.scm.observation.ObservationScope;
import ir.daneshrefah.scm.uaa.observation.UaaObservation;
import ir.daneshrefah.scm.uaa.security.oauth2.policy.LegacyCookiePolicy;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;

/**
 * Legacy PWA cookie delivery kept only for old PWA compatibility.
 * Remove this class after migration to UAA-hosted login and authorization-code flow is complete.
 * No new feature should be added here unless strictly required for migration safety.
 *
 * TODO Replace this legacy JWT cookie with authorization-code flow or an opaque server-side session.
 */
@Component
@Deprecated(since = "9.0.0", forRemoval = true)
@SuppressWarnings("removal")
public class LegacyPwaCookieTokenDeliveryStrategy implements LegacyTokenDeliveryStrategy {
    private final LegacyAuthProperties properties;
    private final LegacyCookiePolicy cookiePolicy;
    private final UaaObservation observation;

    public LegacyPwaCookieTokenDeliveryStrategy(
            LegacyAuthProperties properties,
            LegacyCookiePolicy cookiePolicy,
            UaaObservation observation
    ) {
        this.properties = properties;
        this.cookiePolicy = cookiePolicy;
        this.observation = observation;
    }

    @Override
    public boolean supports(LegacyClientType clientType) {
        return clientType == LegacyClientType.PWA;
    }

    @Override
    public void deliver(LegacyTokenDeliveryContext context) {
        if (!cookiePolicy.canCreateCookie(context.clientType()) || context.token() == null) {
            return;
        }
        UaaObservation.OperationContext operation = new UaaObservation.OperationContext(
                "uaa.cookie.create",
                "uaa.legacy.token.delivery",
                "legacy.pwa.cookie.create",
                "started",
                null
        );
        ObservationScope scope = observation.traceSecurity(operation);
        observation.operationStarted(operation);
        try {
            Optional.ofNullable(context.token().getAccessToken())
                    .filter(StringUtils::hasText)
                    .ifPresent(accessToken -> addAccessTokenCookie(context.response(), context, accessToken));
            UaaObservation.OperationContext completed = operation.withResult("success");
            observation.operationAttributes(scope, completed);
            scope.success();
            observation.operationCompleted(completed);
        } catch (RuntimeException ex) {
            UaaObservation.OperationContext failed = operation
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

    private void addAccessTokenCookie(
            HttpServletResponse response,
            LegacyTokenDeliveryContext context,
            String accessToken
    ) {
        String encodedToken = URLEncoder.encode(accessToken, StandardCharsets.UTF_8);
        long maxAgeSeconds = resolveMaxAgeSeconds(context);
        response.addHeader(HttpHeaders.SET_COOKIE, buildCookieHeader(encodedToken, maxAgeSeconds));
    }

    private long resolveMaxAgeSeconds(LegacyTokenDeliveryContext context) {
        Duration configured = properties.getPwa().getCookie().getMaxAge();
        if (configured != null && !configured.isNegative() && !configured.isZero()) {
            return configured.toSeconds();
        }
        if (context.token().getExpirationDate() == null) {
            return 0;
        }
        return Math.max(0, (context.token().getExpirationDate().getTime() - System.currentTimeMillis()) / 1000);
    }

    private String buildCookieHeader(String value, long maxAgeSeconds) {
        LegacyAuthProperties.Cookie cookie = properties.getPwa().getCookie();
        StringBuilder builder = new StringBuilder()
                .append(cookie.getName())
                .append('=')
                .append(value);
        if (cookie.isHttpOnly()) {
            builder.append("; HttpOnly");
        }
        if (cookie.isSecure()) {
            builder.append("; Secure");
        }
        builder.append("; Path=").append(StringUtils.hasText(cookie.getPath()) ? cookie.getPath() : "/");
        builder.append("; Max-Age=").append(maxAgeSeconds);
        if (StringUtils.hasText(cookie.getSameSite())) {
            builder.append("; SameSite=").append(cookie.getSameSite().trim());
        }
        return builder.toString();
    }
}
