package ir.daneshrefah.scm.observation.uaa;

import ir.daneshrefah.scm.uaa.common.core.AuthorizationGrantType;
import ir.daneshrefah.scm.uaa.security.oauth2.grant.legacy.LegacyAuthProperties;
import ir.daneshrefah.scm.uaa.security.oauth2.grant.legacy.LegacyClientType;
import ir.daneshrefah.scm.uaa.security.oauth2.grant.legacy.LegacyClientTypeResolver;
import ir.daneshrefah.scm.uaa.security.oauth2.grant.legacy.LegacyMbHeaderTokenDeliveryStrategy;
import ir.daneshrefah.scm.uaa.security.oauth2.grant.legacy.LegacyTokenDeliveryContext;
import ir.daneshrefah.scm.uaa.security.oauth2.policy.LegacyCookiePolicy;
import ir.daneshrefah.scm.uaa.service.activation.pwa.services.authentication.model.PwaOAuth2AccessToken;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("removal")
class UaaSecurityArchitectureTest {
    private static final Path MAIN = Path.of("src/main/java");
    private static final Path OLD_AUTH_PACKAGE = MAIN.resolve("ir/daneshrefah/scm/uaa/security/" + "authenticationProvider");
    private static final String OLD_AUTH_IMPORT = "ir.daneshrefah.scm.uaa.security." + "authenticationProvider";

    @Test
    void legacyClassesAreIsolatedAndDeprecatedForRemoval() throws Exception {
        Path legacyPackage = MAIN.resolve("ir/daneshrefah/scm/uaa/security/oauth2/grant/legacy");
        List<Path> files;
        try (var stream = Files.walk(legacyPackage)) {
            files = stream.filter(path -> path.toString().endsWith(".java")).toList();
        }

        assertFalse(files.isEmpty());
        for (Path file : files) {
            String source = Files.readString(file);
            assertTrue(source.contains("@Deprecated(since = \"9.0.0\", forRemoval = true)"),
                    () -> file + " must be deprecated for removal");
            assertTrue(source.contains("Remove this class") || source.contains("Remove this enum"),
                    () -> file + " must explain legacy removal intent");
        }
    }

    @Test
    void oldAuthenticationProviderPackageIsDeletedAndNotImported() throws Exception {
        assertFalse(Files.exists(OLD_AUTH_PACKAGE), "old authentication provider package must be deleted");
        for (Path file : javaFiles(MAIN)) {
            String source = Files.readString(file);
            assertFalse(source.contains(OLD_AUTH_IMPORT), () -> file + " imports old authentication provider package");
        }
    }

    @Test
    void activeProviderNamesDoNotUseLegacyHierarchyNames() throws Exception {
        List<String> bannedNames = List.of(
                "General" + "AuthenticationProvider",
                "BaseGeneral" + "AuthenticationProvider",
                "Delegator" + "AuthenticationProvider",
                "General" + "AuthenticationMethodProvider"
        );
        for (Path file : javaFiles(MAIN)) {
            String fileName = file.getFileName().toString();
            for (String bannedName : bannedNames) {
                assertFalse(fileName.contains(bannedName), () -> file + " must not use old provider hierarchy name");
            }
        }
    }

    @Test
    void standardLoginPackageDoesNotDependOnLegacyPackageAndIsNotDeprecated() throws Exception {
        Path loginPackage = MAIN.resolve("ir/daneshrefah/scm/uaa/web/login");
        for (Path file : javaFiles(loginPackage)) {
            String source = Files.readString(file);
            assertFalse(source.contains("security.oauth2.grant.legacy"), () -> file + " depends on legacy package");
            assertFalse(source.contains("@Deprecated"), () -> file + " must be standard, not legacy");
        }
    }

    @Test
    void grantPackagesDoNotDependOnLegacyUnlessTheyAreLegacy() throws Exception {
        assertPackageDoesNotContain("ir/daneshrefah/scm/uaa/security/oauth2/grant/shahkar", "security.oauth2.grant.legacy");
        assertPackageDoesNotContain("ir/daneshrefah/scm/uaa/security/oauth2/grant/smsotp", "security.oauth2.grant.legacy");
    }

    @Test
    void authorizationServerRegistersLegacyOnlyAtTokenEndpointAndDoesNotRegisterSecondPassword() throws Exception {
        String source = Files.readString(MAIN.resolve("ir/daneshrefah/scm/uaa/config/AuthorizationServerSecurityConfig.java"));

        assertTrue(source.contains("LegacyPasswordGrantAuthenticationConverter"));
        assertTrue(source.contains("LegacyPasswordGrantAuthenticationProvider"));
        assertTrue(source.contains("SmsOtpGrantAuthenticationConverter"));
        assertTrue(source.contains("SmsOtpGrantAuthenticationProvider"));
        assertTrue(source.contains("ShahkarGrantAuthenticationConverter"));
        assertTrue(source.contains("ShahkarGrantAuthenticationProvider"));
        assertFalse(source.contains("SecondPassword" + "GrantAuthenticationConverter"));
        assertFalse(source.contains("AuthorizationGrantType." + "SECOND_PASSWORD"));
    }

    @Test
    void secondPasswordGrantIsNotRegisteredAnywhereInAuthorizationServerConfig() throws Exception {
        String source = Files.readString(MAIN.resolve("ir/daneshrefah/scm/uaa/config/AuthorizationServerSecurityConfig.java"));

        assertFalse(source.contains("second_" + "password"));
        assertFalse(source.contains("SECOND_" + "PASSWORD"));
    }

    @Test
    void mbTokenDeliveryDoesNotCreateCookie() {
        LegacyMbHeaderTokenDeliveryStrategy strategy = new LegacyMbHeaderTokenDeliveryStrategy();
        MockHttpServletResponse response = new MockHttpServletResponse();
        strategy.deliver(new LegacyTokenDeliveryContext(
                LegacyClientType.MB,
                new MockHttpServletRequest(),
                response,
                new PwaOAuth2AccessToken()
        ));

        assertFalse(response.containsHeader("Set-Cookie"));
    }

    @Test
    void pwaCookiePolicyCannotBeSelectedForMbClientType() {
        LegacyAuthProperties properties = new LegacyAuthProperties();
        properties.setEnabled(true);
        properties.getPwa().getCookie().setEnabled(true);
        LegacyCookiePolicy policy = new LegacyCookiePolicy(properties);

        assertFalse(policy.canCreateCookie(LegacyClientType.MB));
        assertTrue(policy.canCreateCookie(LegacyClientType.PWA));
    }

    @Test
    void requestHintAloneCannotForcePwaCookieForNonPwaClient() {
        LegacyClientTypeResolver resolver = new LegacyClientTypeResolver();
        LegacyAuthProperties properties = new LegacyAuthProperties();
        properties.setEnabled(true);
        properties.getPwa().getCookie().setEnabled(true);
        LegacyCookiePolicy policy = new LegacyCookiePolicy(properties);

        LegacyClientType resolved = resolver.resolve("MB", "PWA-REQUEST-HINT", AuthorizationGrantType.DEFAULT);

        assertTrue(LegacyClientType.MB.equals(resolved));
        assertFalse(policy.canCreateCookie(resolved));
    }

    @Test
    void legacyTypesAreRuntimeDeprecatedForRemoval() {
        Deprecated deprecated = LegacyMbHeaderTokenDeliveryStrategy.class.getAnnotation(Deprecated.class);

        assertNotNull(deprecated);
        assertTrue(deprecated.forRemoval());
        assertTrue("9.0.0".equals(deprecated.since()));
    }

    @Test
    void noSensitiveValuesAreLoggedDirectlyByAuthenticationClasses() throws Exception {
        for (Path file : javaFiles(MAIN.resolve("ir/daneshrefah/scm/uaa/security"))) {
            String source = Files.readString(file);
            for (String line : source.lines().filter(line -> line.contains("log.") || line.contains("logger.")).toList()) {
                assertFalse(line.contains("getTokenValue()"), () -> file + " must not log raw JWT values");
                assertFalse(line.contains("Authorization\""), () -> file + " must not log Authorization header");
                assertFalse(line.contains("client_secret"), () -> file + " must not log client_secret");
                assertFalse(line.contains("otpCode"), () -> file + " must not log OTP code");
            }
        }
    }

    private void assertPackageDoesNotContain(String packagePath, String forbiddenText) throws Exception {
        for (Path file : javaFiles(MAIN.resolve(packagePath))) {
            String source = Files.readString(file);
            assertFalse(source.contains(forbiddenText), () -> file + " contains forbidden dependency " + forbiddenText);
        }
    }

    private List<Path> javaFiles(Path root) throws Exception {
        if (!Files.exists(root)) {
            return List.of();
        }
        try (var stream = Files.walk(root)) {
            return stream.filter(path -> path.toString().endsWith(".java")).toList();
        }
    }
}
