package ir.daneshrefah.scm.uaa.client.resource;

import ir.daneshrefah.scm.uaa.client.properties.ScmResourceServerProperties;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;

public class ScmBearerTokenResolver implements BearerTokenResolver {
    private final BearerTokenResolver headerResolver;
    private final BearerTokenResolver cookieResolver;
    private final ScmResourceServerProperties properties;

    public ScmBearerTokenResolver(ScmResourceServerProperties properties) {
        this(properties, new HeaderBearerTokenResolver(),
                new CookieBearerTokenResolver(properties.getToken().getCookieName()));
    }

    ScmBearerTokenResolver(
            ScmResourceServerProperties properties,
            BearerTokenResolver headerResolver,
            BearerTokenResolver cookieResolver
    ) {
        this.properties = properties;
        this.headerResolver = headerResolver;
        this.cookieResolver = cookieResolver;
    }

    @Override
    public String resolve(HttpServletRequest request) {
        if (!properties.getToken().isCookieEnabled()) {
            return headerResolver.resolve(request);
        }
        if (properties.getToken().isPreferCookie()) {
            String cookie = cookieResolver.resolve(request);
            return cookie != null ? cookie : headerResolver.resolve(request);
        }
        String header = headerResolver.resolve(request);
        return header != null ? header : cookieResolver.resolve(request);
    }
}
