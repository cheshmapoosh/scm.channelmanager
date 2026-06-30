package ir.daneshrefah.scm.uaa.security.oauth2.grant.shahkar;

import ir.daneshrefah.scm.uaa.common.utils.Constants;
import ir.daneshrefah.scm.uaa.security.oauth2.grant.legacy.LegacyAuthProperties;
import ir.daneshrefah.scm.uaa.security.oauth2.grant.legacy.client.LegacyClientIdResolver;
import ir.daneshrefah.scm.uaa.security.oauth2.grant.legacy.client.LegacyClientTypeResolver;
import ir.daneshrefah.scm.uaa.security.oauth2.grant.legacy.converter.LegacyRequestParameters;
import ir.daneshrefah.scm.uaa.security.oauth2.grant.shahkar.converter.ShahkarGrantRequestMapper;
import ir.daneshrefah.scm.uaa.security.oauth2.grant.shahkar.session.ShahkarRefreshTokenSessionService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

@SuppressWarnings("removal")
class ShahkarGrantRequestMapperTest {

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void saAppVersionResolvesConfiguredSuperAppClient() {
        ShahkarGrantRequestMapper mapper = mapper();
        MockHttpServletRequest request = shahkarRequest("SA-3.0.0");

        ShahkarGrantAuthenticationToken token = mapper.map(request, new LegacyRequestParameters(request));

        assertEquals("configured-sa", token.getClientId());
        assertEquals("SA-3.0.0", token.getAppVersion());
    }

    @ParameterizedTest
    @ValueSource(strings = {"configured-pwa", "configured-mb", "configured-nib"})
    void pwaMbAndNibClientIdsCannotUseShahkar(String clientId) {
        ShahkarGrantRequestMapper mapper = mapper();
        MockHttpServletRequest request = shahkarRequest("SA-3.0.0");
        request.addParameter(OAuth2ParameterNames.CLIENT_ID, clientId);

        OAuth2AuthenticationException exception = assertThrows(OAuth2AuthenticationException.class, () ->
                mapper.map(request, new LegacyRequestParameters(request)));

        assertEquals("invalid_client", exception.getError().getErrorCode());
    }

    private ShahkarGrantRequestMapper mapper() {
        LegacyAuthProperties properties = new LegacyAuthProperties();
        properties.getClientResolution().setPwaClientId("configured-pwa");
        properties.getClientResolution().setMbClientId("configured-mb");
        properties.getClientResolution().setSuperAppClientId("configured-sa");
        properties.getClientResolution().setNibClientId("configured-nib");
        return new ShahkarGrantRequestMapper(
                new LegacyClientIdResolver(properties),
                new LegacyClientTypeResolver(properties),
                mock(ShahkarRefreshTokenSessionService.class)
        );
    }

    private MockHttpServletRequest shahkarRequest(String appVersion) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter(OAuth2ParameterNames.USERNAME, "0084575948");
        request.addHeader(Constants.ACCESS_PARAM_HEADER, "09121234567");
        request.addHeader("AppVersion", appVersion);
        return request;
    }
}
