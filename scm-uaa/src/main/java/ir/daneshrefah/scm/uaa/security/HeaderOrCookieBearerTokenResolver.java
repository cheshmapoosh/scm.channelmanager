package ir.daneshrefah.scm.uaa.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.server.resource.BearerTokenErrors;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Resolves mobile banking tokens from the standard Authorization header and
 * PWA tokens from the HttpOnly Authorization cookie.
 */
@Component
public class HeaderOrCookieBearerTokenResolver implements BearerTokenResolver {

    private static final String TOKEN_COOKIE_NAME = HttpHeaders.AUTHORIZATION;

    private final BearerTokenResolver headerResolver = new DefaultBearerTokenResolver();

    @Override
    public String resolve(HttpServletRequest request) {
        String headerToken = headerResolver.resolve(request);
        String cookieToken = resolveCookieToken(request);

        if (headerToken != null && cookieToken != null && !headerToken.equals(cookieToken)) {
            throw invalidRequest("Found different bearer tokens in the Authorization header and cookie");
        }

        return headerToken != null ? headerToken : cookieToken;
    }

    private String resolveCookieToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }

        String token = null;
        for (Cookie cookie : cookies) {
            if (!TOKEN_COOKIE_NAME.equals(cookie.getName()) || !StringUtils.hasText(cookie.getValue())) {
                continue;
            }
            if (token != null) {
                throw invalidRequest("Found multiple bearer tokens in Authorization cookies");
            }
            token = cookie.getValue();
        }
        return token;
    }

    private OAuth2AuthenticationException invalidRequest(String description) {
        return new OAuth2AuthenticationException(BearerTokenErrors.invalidRequest(description));
    }
}
