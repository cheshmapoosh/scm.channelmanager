package ir.daneshrefah.scm.uaa.client.resource;

import ir.daneshrefah.scm.uaa.starter.properties.ScmResourceServerProperties;
import ir.daneshrefah.scm.uaa.starter.resource.ScmBearerTokenResolver;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ScmBearerTokenResolverTest {
    @Test
    void authorizationHeaderIsDefaultTokenSource() {
        ScmResourceServerProperties properties = new ScmResourceServerProperties();
        ScmBearerTokenResolver resolver = new ScmBearerTokenResolver(properties);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer mb-token");
        request.setCookies(new Cookie("__Host-SCM-PWA", "pwa-token"));

        assertEquals("mb-token", resolver.resolve(request));
    }

    @Test
    void cookieIsIgnoredWhenNotEnabled() {
        ScmResourceServerProperties properties = new ScmResourceServerProperties();
        ScmBearerTokenResolver resolver = new ScmBearerTokenResolver(properties);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie("__Host-SCM-PWA", "pwa-token"));

        assertNull(resolver.resolve(request));
    }

    @Test
    void cookieIsFallbackWhenEnabledAndHeaderMissing() {
        ScmResourceServerProperties properties = new ScmResourceServerProperties();
        properties.getToken().setCookieEnabled(true);
        ScmBearerTokenResolver resolver = new ScmBearerTokenResolver(properties);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie("__Host-SCM-PWA",
                URLEncoder.encode("pwa token", StandardCharsets.UTF_8)));

        assertEquals("pwa token", resolver.resolve(request));
    }

    @Test
    void headerWinsOverCookieUnlessPreferenceIsExplicit() {
        ScmResourceServerProperties properties = new ScmResourceServerProperties();
        properties.getToken().setCookieEnabled(true);
        ScmBearerTokenResolver resolver = new ScmBearerTokenResolver(properties);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer header-token");
        request.setCookies(new Cookie("__Host-SCM-PWA", "cookie-token"));

        assertEquals("header-token", resolver.resolve(request));

        properties.getToken().setPreferCookie(true);

        assertEquals("cookie-token", resolver.resolve(request));
    }
}
