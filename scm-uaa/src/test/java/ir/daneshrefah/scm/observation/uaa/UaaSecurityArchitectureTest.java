package ir.daneshrefah.scm.observation.uaa;

import ir.daneshrefah.scm.uaa.security.oauth2.grant.legacy.LegacyMbHeaderTokenDeliveryStrategy;
import ir.daneshrefah.scm.uaa.security.oauth2.grant.legacy.LegacyTokenDeliveryContext;
import ir.daneshrefah.scm.uaa.security.oauth2.grant.legacy.LegacyClientType;
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
    void standardLoginPackageDoesNotDependOnLegacyPackageAndIsNotDeprecated() throws Exception {
        Path loginPackage = MAIN.resolve("ir/daneshrefah/scm/uaa/web/login");
        try (var stream = Files.walk(loginPackage)) {
            for (Path file : stream.filter(path -> path.toString().endsWith(".java")).toList()) {
                String source = Files.readString(file);
                assertFalse(source.contains("security.oauth2.grant.legacy"), () -> file + " depends on legacy package");
                assertFalse(source.contains("@Deprecated"), () -> file + " must be standard, not legacy");
            }
        }
    }

    @Test
    void authorizationServerRegistersLegacyOnlyAtTokenEndpointAndDoesNotRegisterSecondPassword() throws Exception {
        String source = Files.readString(MAIN.resolve("ir/daneshrefah/scm/uaa/config/AuthorizationServerSecurityConfig.java"));

        assertTrue(source.contains("LegacyPasswordGrantAuthenticationConverter"));
        assertTrue(source.contains("LegacyPasswordGrantAuthenticationProvider"));
        assertFalse(source.contains("SecondPasswordGrantAuthenticationConverter"));
        assertFalse(source.contains("AuthorizationGrantType.SECOND_PASSWORD"));
    }

    @Test
    void customDelegatorProviderWasRemoved() {
        assertFalse(Files.exists(MAIN.resolve("ir/daneshrefah/scm/uaa/security/authenticationProvider/DelegatorAuthenticationProvider.java")));
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
    void legacyTypesAreRuntimeDeprecatedForRemoval() {
        Deprecated deprecated = LegacyMbHeaderTokenDeliveryStrategy.class.getAnnotation(Deprecated.class);

        assertNotNull(deprecated);
        assertTrue(deprecated.forRemoval());
        assertTrue("9.0.0".equals(deprecated.since()));
    }
}
