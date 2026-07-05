package ir.daneshrefah.scm.uaa.client.security.event;

import ir.daneshrefah.scm.uaa.client.properties.ScmResourceServerProperties;
import ir.daneshrefah.scm.uaa.client.security.ScmPrincipal;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.server.resource.BearerTokenAuthenticationException;
import org.springframework.util.StringUtils;

import java.security.Principal;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

final class ScmSecurityEventAttributes {
    private static final String AUTHORIZATION = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private ScmSecurityEventAttributes() {
    }

    static Map<String, Object> base(
            HttpServletRequest request,
            ScmResourceServerProperties properties
    ) {
        Map<String, Object> attributes = new LinkedHashMap<>();
        put(attributes, "http.method", request == null ? null : request.getMethod());
        put(attributes, "url.path", request == null ? null : request.getRequestURI());
        put(attributes, "client.ip", clientIp(request));
        addPrincipal(attributes, currentAuthentication());
        addRequestPrincipal(attributes, request == null ? null : request.getUserPrincipal());
        return attributes;
    }

    static ScmSecurityEventType failureType(
            HttpServletRequest request,
            ScmResourceServerProperties properties,
            Exception exception
    ) {
        String message = safeMessage(exception).toLowerCase(Locale.ROOT);
        if (message.contains("expired")) {
            return ScmSecurityEventType.TOKEN_EXPIRED;
        }
        if (!hasBearerCredential(request, properties)) {
            return ScmSecurityEventType.TOKEN_MISSING;
        }
        return ScmSecurityEventType.TOKEN_INVALID;
    }

    static String errorCode(Exception exception) {
        if (exception instanceof BearerTokenAuthenticationException bearerException) {
            OAuth2Error error = bearerException.getError();
            return error == null ? null : error.getErrorCode();
        }
        return exception == null ? null : exception.getClass().getSimpleName();
    }

    static String safeMessage(Throwable exception) {
        if (exception == null || exception.getMessage() == null) {
            return "";
        }
        String message = exception.getMessage()
                .replace('\r', ' ')
                .replace('\n', ' ')
                .replaceAll("(?i)(bearer|token|authorization|cookie|password|secret)\\s+\\S+", "$1 ***")
                .trim();
        return message.length() > 300 ? message.substring(0, 300) : message;
    }

    static void put(Map<String, Object> attributes, String name, Object value) {
        if (attributes != null && name != null && !name.isBlank() && value != null) {
            if (value instanceof String text && !StringUtils.hasText(text)) {
                return;
            }
            attributes.put(name, value);
        }
    }

    private static Authentication currentAuthentication() {
        try {
            return SecurityContextHolder.getContext().getAuthentication();
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    private static void addPrincipal(Map<String, Object> attributes, Authentication authentication) {
        if (authentication == null) {
            return;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof ScmPrincipal scmPrincipal) {
            put(attributes, "user.name", scmPrincipal.nickname());
            put(attributes, "scm.terminal.code", scmPrincipal.terminalCode());
            put(attributes, "scm.client.id", scmPrincipal.clientId());
            return;
        }
        put(attributes, "user.name", authentication.getName());
    }

    private static void addRequestPrincipal(Map<String, Object> attributes, Principal principal) {
        if (!attributes.containsKey("user.name") && principal != null) {
            put(attributes, "user.name", principal.getName());
        }
    }

    private static boolean hasBearerCredential(
            HttpServletRequest request,
            ScmResourceServerProperties properties
    ) {
        if (request == null) {
            return false;
        }
        String authorization = request.getHeader(AUTHORIZATION);
        if (authorization != null && authorization.trim().startsWith(BEARER_PREFIX)) {
            return true;
        }
        if (properties == null || !properties.getToken().isCookieEnabled() || request.getCookies() == null) {
            return false;
        }
        String cookieName = properties.getToken().getCookieName();
        if (!StringUtils.hasText(cookieName)) {
            return false;
        }
        for (Cookie cookie : request.getCookies()) {
            if (cookie != null && cookieName.equals(cookie.getName()) && StringUtils.hasText(cookie.getValue())) {
                return true;
            }
        }
        return false;
    }

    private static String clientIp(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(forwardedFor)) {
            int comma = forwardedFor.indexOf(',');
            return comma >= 0 ? forwardedFor.substring(0, comma).trim() : forwardedFor.trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        return StringUtils.hasText(realIp) ? realIp.trim() : request.getRemoteAddr();
    }
}
