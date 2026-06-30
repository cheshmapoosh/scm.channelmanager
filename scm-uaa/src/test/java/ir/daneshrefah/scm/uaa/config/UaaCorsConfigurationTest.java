package ir.daneshrefah.scm.uaa.config;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.cors.CorsConfigurationSource;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UaaCorsConfigurationTest {
    private static final Path CONFIG = Path.of("src/main/java/ir/daneshrefah/scm/uaa/config");

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(CorsSecurityConfig.class)
            .withPropertyValues(
                    "scm.uaa.cors.enabled=true",
                    "scm.uaa.cors.allow-credentials=true"
            );

    @Test
    void contextStartsWithExplicitUaaCorsBeanAndDevLocalOrigins() {
        contextRunner
                .withPropertyValues(
                        "spring.profiles.active=dev",
                        "scm.uaa.cors.allowed-origin-patterns[0]=http://localhost:*",
                        "scm.uaa.cors.allowed-origin-patterns[1]=https://localhost:*"
                )
                .run(context -> {
                    assertTrue(context.containsBean("uaaCorsConfigurationSource"));
                    CorsConfigurationSource source = context.getBean(
                            "uaaCorsConfigurationSource",
                            CorsConfigurationSource.class
                    );
                    HttpServletRequest request = new MockHttpServletRequest("GET", "/oauth2/token");
                    var configuration = source.getCorsConfiguration(request);
                    assertNotNull(configuration);
                    assertTrue(configuration.getAllowedOriginPatterns().contains("http://localhost:*"));
                    assertEquals(Boolean.TRUE, configuration.getAllowCredentials());
                });
    }

    @Test
    void wildcardIsRejectedWhenCredentialsAreEnabled() {
        contextRunner
                .withPropertyValues("scm.uaa.cors.allowed-origin-patterns[0]=*")
                .run(context -> assertNotNull(context.getStartupFailure()));
    }

    @Test
    void emptyOriginsAreRejectedForEveryKubernetesProfile() {
        for (String profile : new String[]{"test", "pilot", "prod"}) {
            contextRunner
                    .withPropertyValues("spring.profiles.active=" + profile)
                    .run(context -> assertNotNull(context.getStartupFailure(), profile));
        }
    }

    @Test
    void securityChainsSelectUaaCorsBeanWithoutPrimary() throws Exception {
        String corsConfig = Files.readString(CONFIG.resolve("CorsSecurityConfig.java"));
        String authorization = Files.readString(CONFIG.resolve("AuthorizationServerSecurityConfig.java"));
        String resource = Files.readString(CONFIG.resolve("ResourceServerSecurityConfig.java"));

        assertTrue(corsConfig.contains("@Bean(\"uaaCorsConfigurationSource\")"));
        assertFalse(corsConfig.contains("@Primary"));
        assertTrue(authorization.contains("@Qualifier(\"uaaCorsConfigurationSource\")"));
        assertTrue(authorization.contains("cors.configurationSource(corsConfigurationSource)"));
        assertTrue(resource.contains("@Qualifier(\"uaaCorsConfigurationSource\")"));
        assertTrue(resource.contains("cors.configurationSource(corsConfigurationSource)"));
    }
}
