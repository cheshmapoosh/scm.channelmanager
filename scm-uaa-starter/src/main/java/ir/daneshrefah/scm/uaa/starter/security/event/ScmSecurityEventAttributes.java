package ir.daneshrefah.scm.uaa.starter.security.event;

import ir.daneshrefah.scm.common.event.ScmSafeEventAttributes;
import ir.daneshrefah.scm.uaa.starter.properties.ScmResourceServerProperties;
import ir.daneshrefah.scm.uaa.starter.security.ScmPrincipal;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
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
        addAuthentication(attributes, currentAuthentication());
        addRequestPrincipal(attributes, request == null ? null : request.getUserPrincipal());
        return attributes;
    }

    static Map<String, Object> authentication(Authentication authentication) {
        Map<String, Object> attributes = new LinkedHashMap<>();
        addAuthentication(attributes, authentication);
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
        if (exception instanceof OAuth2AuthenticationException oauthException) {
            OAuth2Error error = oauthException.getError();
            return error == null ? null : error.getErrorCode();
        }
        return exception == null ? null : exception.getClass().getSimpleName();
    }

    static String safeMessage(Throwable exception) {
        if (exception == null || exception.getMessage() == null) {
            return "";
        }
        String message = ScmSafeEventAttributes.sanitizeMessage(exception.getMessage());
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

    private static void addAuthentication(Map<String, Object> attributes, Authentication authentication) {
        if (authentication == null) {
            return;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof ScmPrincipal scmPrincipal) {
            put(attributes, "security.subject", scmPrincipal.subject());
            put(attributes, "session.id", scmPrincipal.sessionId());
            put(attributes, "user.name", scmPrincipal.nickname());
            put(attributes, "scm.terminal.code", scmPrincipal.terminalCode());
            put(attributes, "scm.client.id", scmPrincipal.clientId());
            put(attributes, "security.jti", scmPrincipal.tokenId());
            addJwt(attributes, authentication);
            return;
        }
        put(attributes, "user.name", authentication.getName());
        addJwt(attributes, authentication);
    }

    private static void addJwt(Map<String, Object> attributes, Authentication authentication) {
        Jwt jwt = jwt(authentication);
        if (jwt == null) {
            return;
        }
        putIfAbsent(attributes, "security.subject", jwt.getSubject());
        putIfAbsent(attributes, "user.name", firstClaim(jwt, "preferred_username", "nickname", "name"));
        putIfAbsent(attributes, "session.id", firstClaim(jwt, "sid", "session_id", "sessionId"));
        putIfAbsent(attributes, "scm.client.id", firstClaim(jwt, "client_id", "azp", "clientId"));
        putIfAbsent(attributes, "scm.terminal.code", firstClaim(jwt, "terminal_code", "terminalCode", "terminal"));
        putIfAbsent(attributes, "security.jti", firstClaim(jwt, "jti"));
    }

    private static Jwt jwt(Authentication authentication) {
        if (authentication == null) {
            return null;
        }
        if (authentication instanceof JwtAuthenticationToken jwtAuthenticationToken) {
            return jwtAuthenticationToken.getToken();
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof Jwt jwt) {
            return jwt;
        }
        Object credentials = authentication.getCredentials();
        if (credentials instanceof Jwt jwt) {
            return jwt;
        }
        return null;
    }

    private static String firstClaim(Jwt jwt, String... claimNames) {
        if (jwt == null || claimNames == null) {
            return null;
        }
        for (String claimName : claimNames) {
            if (!StringUtils.hasText(claimName)) {
                continue;
            }
            String value = jwt.getClaimAsString(claimName);
            if (StringUtils.hasText(value)) {
                return value;
            }
        }
        return null;
    }

    private static void putIfAbsent(Map<String, Object> attributes, String name, Object value) {
        if (attributes == null || attributes.containsKey(name)) {
            return;
        }
        put(attributes, name, value);
    }

    private static void addRequestPrincipal(Map<String, Object> attributes, Principal principal) {
        if (!attributes.containsKey("user.name") && principal != null) {
            put(attributes, "user.name", principal.getName());
        }
    }

    static boolean hasBearerCredential(
            HttpServletRequest request,
            ScmResourceServerProperties properties
    ) {
        if (request == null) {
            return false;
        }
        String authorization = request.getHeader(AUTHORIZATION);
        if (authorization != null
                && authorization.trim().regionMatches(true, 0, BEARER_PREFIX, 0, BEARER_PREFIX.length())) {
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

    static boolean isPublicEndpoint(HttpServletRequest request, ScmResourceServerProperties properties) {
        if (request == null || properties == null || properties.getPublicPaths() == null) {
            return false;
        }
        String requestUri = request.getRequestURI();
        String servletPath = request.getServletPath();
        for (String publicPath : properties.getPublicPaths()) {
            if (!StringUtils.hasText(publicPath)) {
                continue;
            }
            String pattern = publicPath.trim();
            if (matchesPublicPath(requestUri, pattern) || matchesPublicPath(servletPath, pattern)) {
                return true;
            }
        }
        return false;
    }

    private static boolean matchesPublicPath(String path, String pattern) {
        if (!StringUtils.hasText(path) || !StringUtils.hasText(pattern)) {
            return false;
        }
        if (path.equals(pattern)) {
            return true;
        }
        if (pattern.endsWith("/**")) {
            String prefix = pattern.substring(0, pattern.length() - 3);
            return path.equals(prefix) || path.startsWith(prefix + "/");
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
