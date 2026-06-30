package ir.daneshrefah.scm.observation.uaa;

import ir.daneshrefah.scm.uaa.common.core.AuthorizationGrantType;
import ir.daneshrefah.scm.uaa.common.utils.Constants;
import ir.daneshrefah.scm.uaa.security.oauth2.grant.legacy.LegacyAuthProperties;
import ir.daneshrefah.scm.uaa.security.oauth2.grant.legacy.LegacyClientType;
import ir.daneshrefah.scm.uaa.security.oauth2.grant.legacy.client.LegacyAppVersion;
import ir.daneshrefah.scm.uaa.security.oauth2.grant.legacy.client.LegacyClientIdResolver;
import ir.daneshrefah.scm.uaa.security.oauth2.grant.legacy.client.LegacyClientTypeResolver;
import ir.daneshrefah.scm.uaa.security.oauth2.grant.legacy.converter.LegacyRequestParameters;
import ir.daneshrefah.scm.uaa.security.oauth2.policy.RegisteredClientLegacyPolicy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("removal")
class LegacyClientResolutionTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "AppVersion",
            "Appversion",
            "app_version",
            "app-version",
            "APP_VERSION",
            "APPVERSION"
    })
    void appVersionHeaderAliasesAreResolvedInCode(String headerName) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(headerName, "PWA-1.2.3");

        assertEquals("PWA-1.2.3", new LegacyRequestParameters(request).appVersion().orElseThrow());
    }

    @ParameterizedTest
    @ValueSource(strings = {"aPpVeRsIoN", "aPp_VeRsIoN", "ApP-vErSiOn"})
    void appVersionHeaderNormalizationHandlesMixedCaseUnderscoresAndHyphens(String headerName) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(headerName, "MB-2.0.0");

        assertEquals("MB-2.0.0", new LegacyRequestParameters(request).appVersion().orElseThrow());
    }

    @Test
    void requestParametersAndHeadersUseCaseInsensitiveLookup() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("ClIeNt_Id", "parameter-client");
        request.addHeader("SiGnAtUrE", "header-signature");
        LegacyRequestParameters parameters = new LegacyRequestParameters(request);

        assertEquals("parameter-client", parameters.first("client_id").orElseThrow());
        assertEquals("header-signature", parameters.first("signature").orElseThrow());
        assertEquals("parameter-client", parameters.firstAny("missing", "CLIENT_ID").orElseThrow());
    }

    @Test
    void appVersionCanBeReadFromLegacyRequestParameter() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("APP_VERSION", "PWA-parameter");

        assertEquals("PWA-parameter", new LegacyRequestParameters(request).appVersion().orElseThrow());
    }

    @Test
    void appVersionRequestParameterMapsDefaultGrantClientId() {
        LegacyClientIdResolver resolver = new LegacyClientIdResolver(configuredProperties());
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("app_version", "PWA-parameter");
        LegacyRequestParameters parameters = new LegacyRequestParameters(request);

        assertEquals("configured-pwa", resolver.resolve(
                null,
                parameters,
                new LegacyAppVersion(parameters.appVersion().orElse(null)),
                AuthorizationGrantType.DEFAULT
        ));
    }

    @Test
    void clientIdHeaderDoesNotCountAsExplicitClientIdParameter() {
        LegacyClientIdResolver resolver = new LegacyClientIdResolver(configuredProperties());
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(OAuth2ParameterNames.CLIENT_ID, "header-client");
        LegacyRequestParameters parameters = new LegacyRequestParameters(request);

        OAuth2AuthenticationException exception = assertThrows(OAuth2AuthenticationException.class, () ->
                resolver.resolve(
                        null,
                        parameters,
                        new LegacyAppVersion(null),
                        AuthorizationGrantType.DEFAULT
                ));

        assertEquals("invalid_client", exception.getError().getErrorCode());
    }

    @ParameterizedTest
    @CsvSource({
            "PWA-1.0.0, true, false, false, PWA",
            "pwa-web, true, false, false, PWA",
            "MB-9.4.0, false, true, false, MB",
            "mb-android, false, true, false, MB",
            "SA-3.0.0, false, false, true, SA",
            "sa-mobile, false, false, true, SA"
    })
    void appVersionClassifiesKnownPrefixes(
            String rawValue,
            boolean pwa,
            boolean mb,
            boolean superApp,
            LegacyClientType clientType
    ) {
        LegacyAppVersion appVersion = new LegacyAppVersion(rawValue);

        assertEquals(pwa, appVersion.isPwa());
        assertEquals(mb, appVersion.isMb());
        assertEquals(superApp, appVersion.isSuperApp());
        assertTrue(appVersion.isKnown());
        assertEquals(clientType, appVersion.clientType());
    }

    @Test
    void appVersionNormalizesWhitespaceAndRejectsUnknownValues() {
        assertEquals("PWA-1", new LegacyAppVersion("  PWA-1  ").rawValue());

        LegacyAppVersion unknown = new LegacyAppVersion("WEB-1.0.0");
        assertTrue(unknown.isPresent());
        assertFalse(unknown.isKnown());
        assertNull(unknown.clientType());

        LegacyAppVersion missing = new LegacyAppVersion("  ");
        assertFalse(missing.isPresent());
        assertFalse(missing.isKnown());
    }

    @ParameterizedTest
    @CsvSource({
            "PWA-1.0.0, configured-pwa",
            "MB-1.0.0, configured-mb",
            "SA-1.0.0, configured-sa"
    })
    void defaultGrantMapsKnownAppVersionToConfiguredClientId(String rawAppVersion, String expectedClientId) {
        LegacyAuthProperties properties = configuredProperties();
        LegacyClientIdResolver resolver = new LegacyClientIdResolver(properties);
        MockHttpServletRequest request = requestWithAppVersion("app_version", rawAppVersion);
        LegacyRequestParameters parameters = new LegacyRequestParameters(request);

        assertEquals(expectedClientId, resolver.resolve(
                null,
                parameters,
                new LegacyAppVersion(parameters.appVersion().orElse(null)),
                AuthorizationGrantType.DEFAULT
        ));
    }

    @ParameterizedTest
    @CsvSource({
            "PWA-1.0.0, PWA",
            "MB-1.0.0, MB",
            "SA-1.0.0, SA"
    })
    void defaultGrantKeepsCompatibilityClientIdDefaults(String rawAppVersion, String expectedClientId) {
        LegacyClientIdResolver resolver = new LegacyClientIdResolver(new LegacyAuthProperties());
        MockHttpServletRequest request = requestWithAppVersion("AppVersion", rawAppVersion);
        LegacyRequestParameters parameters = new LegacyRequestParameters(request);

        assertEquals(expectedClientId, resolver.resolve(
                null,
                parameters,
                new LegacyAppVersion(parameters.appVersion().orElse(null)),
                AuthorizationGrantType.DEFAULT
        ));
    }

    @Test
    void explicitClientIdOverridesKnownAppVersion() {
        LegacyClientIdResolver resolver = new LegacyClientIdResolver(configuredProperties());
        MockHttpServletRequest request = requestWithAppVersion("AppVersion", "PWA-1.0.0");
        request.addParameter(OAuth2ParameterNames.CLIENT_ID, "explicit-client");
        LegacyRequestParameters parameters = new LegacyRequestParameters(request);

        assertEquals("explicit-client", resolver.resolve(
                null,
                parameters,
                new LegacyAppVersion(parameters.appVersion().orElse(null)),
                AuthorizationGrantType.DEFAULT
        ));
    }

    @Test
    void authenticatedOauthClientOverridesExplicitClientIdAndAppVersion() {
        LegacyClientIdResolver resolver = new LegacyClientIdResolver(configuredProperties());
        MockHttpServletRequest request = requestWithAppVersion("app-version", "PWA-1.0.0");
        request.addParameter(OAuth2ParameterNames.CLIENT_ID, "explicit-client");
        LegacyRequestParameters parameters = new LegacyRequestParameters(request);
        RegisteredClient registeredClient = registeredClient("authenticated-client", null);
        OAuth2ClientAuthenticationToken clientPrincipal = new OAuth2ClientAuthenticationToken(
                registeredClient,
                ClientAuthenticationMethod.NONE,
                null
        );

        assertEquals("authenticated-client", resolver.resolve(
                clientPrincipal,
                parameters,
                new LegacyAppVersion(parameters.appVersion().orElse(null)),
                AuthorizationGrantType.DEFAULT
        ));
    }

    @Test
    void unknownAppVersionFailsWithInvalidAppVersion() {
        LegacyClientIdResolver resolver = new LegacyClientIdResolver(configuredProperties());
        MockHttpServletRequest request = requestWithAppVersion("APPVERSION", "WEB-1.0.0");
        LegacyRequestParameters parameters = new LegacyRequestParameters(request);

        OAuth2AuthenticationException exception = assertThrows(OAuth2AuthenticationException.class, () ->
                resolver.resolve(
                        null,
                        parameters,
                        new LegacyAppVersion(parameters.appVersion().orElse(null)),
                        AuthorizationGrantType.DEFAULT
                ));

        assertEquals(Constants.OAUTH2_ERROR_CODE_INVALID_APP_VERSION, exception.getError().getErrorCode());
    }

    @Test
    void missingAppVersionAndClientFailsWithInvalidClient() {
        LegacyClientIdResolver resolver = new LegacyClientIdResolver(configuredProperties());
        LegacyRequestParameters parameters = new LegacyRequestParameters(new MockHttpServletRequest());

        OAuth2AuthenticationException exception = assertThrows(OAuth2AuthenticationException.class, () ->
                resolver.resolve(
                        null,
                        parameters,
                        new LegacyAppVersion(null),
                        AuthorizationGrantType.DEFAULT
                ));

        assertEquals("invalid_client", exception.getError().getErrorCode());
    }

    @Test
    void appVersionFallbackIsLimitedToDefaultGrant() {
        LegacyClientIdResolver resolver = new LegacyClientIdResolver(configuredProperties());
        MockHttpServletRequest request = requestWithAppVersion(Constants.APP_VERSION_HEADER, "PWA-1.0.0");
        LegacyRequestParameters parameters = new LegacyRequestParameters(request);

        OAuth2AuthenticationException exception = assertThrows(OAuth2AuthenticationException.class, () ->
                resolver.resolve(
                        null,
                        parameters,
                        new LegacyAppVersion(parameters.appVersion().orElse(null)),
                        AuthorizationGrantType.FIRST_PASSWORD
                ));

        assertEquals("invalid_client", exception.getError().getErrorCode());
    }

    @Test
    void registeredClientSettingWinsOverClientIdAndRequestHint() {
        LegacyClientTypeResolver resolver = new LegacyClientTypeResolver(configuredProperties());
        RegisteredClient registeredClient = registeredClient("configured-pwa", LegacyClientType.MB.name());

        assertEquals(LegacyClientType.MB, resolver.resolve(
                registeredClient,
                "configured-pwa",
                new LegacyAppVersion("PWA-1.0.0"),
                AuthorizationGrantType.DEFAULT
        ));
    }

    @Test
    void resolvedClientIdWinsOverRequestHint() {
        LegacyClientTypeResolver resolver = new LegacyClientTypeResolver(configuredProperties());

        assertEquals(LegacyClientType.MB, resolver.resolve(
                null,
                "configured-mb",
                new LegacyAppVersion("PWA-1.0.0"),
                AuthorizationGrantType.DEFAULT
        ));
    }

    @Test
    void appVersionTypeHintIsUsedOnlyForDefaultCompatibility() {
        LegacyClientTypeResolver resolver = new LegacyClientTypeResolver(configuredProperties());
        LegacyAppVersion appVersion = new LegacyAppVersion("PWA-1.0.0");

        assertEquals(LegacyClientType.PWA, resolver.resolve(
                null,
                "unclassified-client",
                appVersion,
                AuthorizationGrantType.DEFAULT
        ));
        assertNull(resolver.resolve(
                null,
                "unclassified-client",
                appVersion,
                AuthorizationGrantType.FIRST_PASSWORD
        ));
    }

    @Test
    void invalidRegisteredClientTypeSettingCannotFallBackToPwaHint() {
        LegacyAuthProperties properties = configuredProperties();
        LegacyClientTypeResolver resolver = new LegacyClientTypeResolver(properties);
        RegisteredClient registeredClient = registeredClient("configured-pwa", "NOT_A_CLIENT_TYPE");
        RegisteredClientLegacyPolicy policy = new RegisteredClientLegacyPolicy(properties);

        assertNull(resolver.resolve(
                registeredClient,
                "configured-pwa",
                new LegacyAppVersion("PWA-1.0.0"),
                AuthorizationGrantType.DEFAULT
        ));
        assertFalse(policy.isLegacyPasswordGrantAllowed(registeredClient, LegacyClientType.PWA));
    }

    @Test
    void configuredClientIdsRemainSubjectToRegisteredClientPolicy() {
        LegacyAuthProperties properties = configuredProperties();
        RegisteredClientLegacyPolicy policy = new RegisteredClientLegacyPolicy(properties);

        assertTrue(policy.isLegacyPasswordGrantAllowed(
                registeredClient("configured-pwa", null),
                LegacyClientType.PWA
        ));
        assertFalse(policy.isLegacyPasswordGrantAllowed(
                registeredClient("different-client", null),
                LegacyClientType.PWA
        ));
    }

    private MockHttpServletRequest requestWithAppVersion(String name, String value) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(name, value);
        return request;
    }

    private LegacyAuthProperties configuredProperties() {
        LegacyAuthProperties properties = new LegacyAuthProperties();
        properties.getClientResolution().setPwaClientId("configured-pwa");
        properties.getClientResolution().setMbClientId("configured-mb");
        properties.getClientResolution().setSuperAppClientId("configured-sa");
        properties.getClientResolution().setNibClientId("configured-nib");
        return properties;
    }

    private RegisteredClient registeredClient(String clientId, String clientTypeSetting) {
        ClientSettings.Builder settings = ClientSettings.builder();
        if (clientTypeSetting != null) {
            settings.setting(RegisteredClientLegacyPolicy.SETTING_LEGACY_CLIENT_TYPE, clientTypeSetting);
        }
        return RegisteredClient.withId("test-" + clientId)
                .clientId(clientId)
                .clientAuthenticationMethod(ClientAuthenticationMethod.NONE)
                .authorizationGrantType(new org.springframework.security.oauth2.core.AuthorizationGrantType("first_password"))
                .redirectUri("https://client.example/callback")
                .scope("openid")
                .clientSettings(settings.build())
                .build();
    }
}
