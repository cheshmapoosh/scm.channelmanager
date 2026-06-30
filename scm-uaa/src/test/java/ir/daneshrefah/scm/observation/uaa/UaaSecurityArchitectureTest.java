package ir.daneshrefah.scm.observation.uaa;

import ir.daneshrefah.scm.uaa.common.core.AuthorizationGrantType;
import ir.daneshrefah.scm.uaa.security.oauth2.grant.legacy.LegacyAuthProperties;
import ir.daneshrefah.scm.uaa.security.oauth2.grant.legacy.LegacyClientType;
import ir.daneshrefah.scm.uaa.security.oauth2.grant.legacy.client.LegacyAppVersion;
import ir.daneshrefah.scm.uaa.security.oauth2.grant.legacy.client.LegacyClientTypeResolver;
import ir.daneshrefah.scm.uaa.security.oauth2.policy.LegacyCookiePolicy;
import ir.daneshrefah.scm.uaa.security.oauth2.policy.RegisteredClientLegacyPolicy;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
    void noEmptyServiceOrComponentClassExists() throws Exception {
        for (Path file : javaFiles(MAIN)) {
            String source = Files.readString(file);
            if (!source.contains("@Service") && !source.contains("@Component")) {
                continue;
            }
            String compact = source
                    .replaceAll("(?s)/\\*.*?\\*/", "")
                    .replaceAll("(?m)//.*$", "")
                    .replaceAll("\\s+", "");
            assertFalse(compact.matches(".*class[A-Za-z0-9_]+[^{}]*\\{\\}.*"),
                    () -> file + " must not be an empty @Service/@Component marker");
        }
    }

    @Test
    void legacyPackageIsSplitIntoExpectedResponsibilitySubpackages() throws Exception {
        Path legacyRoot = MAIN.resolve("ir/daneshrefah/scm/uaa/security/oauth2/grant/legacy");
        Set<String> rootFiles;
        try (var stream = Files.list(legacyRoot)) {
            rootFiles = stream
                    .filter(path -> path.toString().endsWith(".java"))
                    .map(path -> path.getFileName().toString())
                    .collect(Collectors.toSet());
        }
        assertEquals(Set.of(
                "LegacyAuthProperties.java",
                "LegacyClientType.java",
                "LegacyPasswordGrantAuthenticationToken.java",
                "LegacyPasswordGrantRequest.java"
        ), rootFiles);

        assertJavaFileExists("ir/daneshrefah/scm/uaa/security/oauth2/grant/legacy/converter/LegacyPasswordGrantAuthenticationConverter.java");
        assertJavaFileExists("ir/daneshrefah/scm/uaa/security/oauth2/grant/legacy/converter/LegacyPasswordGrantRequestMapper.java");
        assertJavaFileExists("ir/daneshrefah/scm/uaa/security/oauth2/grant/legacy/converter/LegacyRequestParameters.java");
        assertJavaFileExists("ir/daneshrefah/scm/uaa/security/oauth2/grant/legacy/converter/LegacyDefaultGrantRequestMapper.java");
        assertJavaFileExists("ir/daneshrefah/scm/uaa/security/oauth2/grant/legacy/provider/LegacyPasswordGrantAuthenticationProvider.java");
        assertJavaFileExists("ir/daneshrefah/scm/uaa/security/oauth2/grant/legacy/client/LegacyAppVersion.java");
        assertJavaFileExists("ir/daneshrefah/scm/uaa/security/oauth2/grant/legacy/client/LegacyClientIdResolver.java");
        assertJavaFileExists("ir/daneshrefah/scm/uaa/security/oauth2/grant/legacy/client/LegacyClientTypeResolver.java");
        assertJavaFileExists("ir/daneshrefah/scm/uaa/security/oauth2/grant/legacy/delivery/LegacyTokenDeliveryContext.java");
        assertJavaFileExists("ir/daneshrefah/scm/uaa/security/oauth2/grant/legacy/delivery/LegacyTokenDeliveryStrategy.java");
        assertJavaFileExists("ir/daneshrefah/scm/uaa/security/oauth2/grant/legacy/delivery/LegacyPwaCookieTokenDeliveryStrategy.java");
        assertJavaFileExists("ir/daneshrefah/scm/uaa/security/oauth2/grant/legacy/response/LegacyPwaOauthLoginResponseProxyAdvisor.java");
    }

    @Test
    void removedMarkerAndNoOpLegacyClassesDoNotExist() {
        assertJavaFileDoesNotExist("ir/daneshrefah/scm/uaa/security/oauth2/grant/legacy/LegacyActivationFlowService.java");
        assertJavaFileDoesNotExist("ir/daneshrefah/scm/uaa/security/oauth2/grant/legacy/LegacyLoginFlowService.java");
        assertJavaFileDoesNotExist("ir/daneshrefah/scm/uaa/security/oauth2/grant/legacy/LegacyPasswordGrantAdapter.java");
        assertJavaFileDoesNotExist("ir/daneshrefah/scm/uaa/security/oauth2/grant/legacy/LegacyMbHeaderTokenDeliveryStrategy.java");
        assertJavaFileDoesNotExist("ir/daneshrefah/scm/uaa/security/oauth2/grant/legacy/LegacyNibBackToBackTokenDeliveryStrategy.java");
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
    void shahkarReusesCentralLegacyClientResolution() throws Exception {
        String mapper = Files.readString(MAIN.resolve(
                "ir/daneshrefah/scm/uaa/security/oauth2/grant/shahkar/converter/ShahkarGrantRequestMapper.java"
        ));
        assertTrue(mapper.contains("LegacyClientIdResolver"));
        assertTrue(mapper.contains("LegacyClientTypeResolver"));
    }

    @Test
    void authorizationServerRegistersLegacyOnlyAtTokenEndpointAndDoesNotRegisterSecondPassword() throws Exception {
        String source = Files.readString(MAIN.resolve("ir/daneshrefah/scm/uaa/config/AuthorizationServerSecurityConfig.java"));

        assertTrue(source.contains("LegacyPasswordGrantAuthenticationConverter"));
        assertTrue(source.contains("LegacyPasswordGrantAuthenticationProvider"));
        assertFalse(source.contains("SmsOtpGrantAuthenticationConverter"));
        assertFalse(source.contains("SmsOtpGrantAuthenticationProvider"));
        assertTrue(source.contains("ShahkarGrantAuthenticationConverter"));
        assertTrue(source.contains("ShahkarGrantAuthenticationProvider"));
        assertFalse(source.contains("new LegacyPasswordGrantAuthenticationConverter"));
        assertFalse(source.contains("new LegacyPasswordGrantRequestMapper"));
        assertFalse(source.contains("SecondPassword" + "GrantAuthenticationConverter"));
        assertFalse(source.contains("AuthorizationGrantType." + "SECOND_PASSWORD"));
    }

    @Test
    void removedSmsOtpGrantHasNoPackageOrGrantTypeRegistration() throws Exception {
        assertFalse(Files.exists(MAIN.resolve(
                "ir/daneshrefah/scm/uaa/security/oauth2/grant/smsotp"
        )));
        String grantType = Files.readString(Path.of("../scm-uaa-common/src/main/java/ir/daneshrefah/scm/uaa/common/core/AuthorizationGrantType.java"));
        String mapper = Files.readString(MAIN.resolve("ir/daneshrefah/scm/uaa/mapper/AuthorizationGrantTypeMapper.java"));
        assertFalse(grantType.contains("SMS_" + "OTP"));
        assertFalse(mapper.contains("SMS_" + "OTP"));
        assertTrue(mapper.contains("AuthorizationGrantType.SHAHKAR.getCode()"));
    }

    @Test
    void firstLevelNamingAndOldSecurityTokenPackageAreGone() throws Exception {
        Path oldTokenPackage = MAIN.resolve("ir/daneshrefah/scm/uaa/security/token");
        assertTrue(javaFiles(oldTokenPackage).isEmpty());
        for (Path file : javaFiles(MAIN)) {
            String source = Files.readString(file);
            assertFalse(file.getFileName().toString().startsWith("First" + "Lvl"),
                    () -> file + " uses removed first-level naming");
            assertFalse(source.contains("First" + "Lvl"), () -> file + " references removed first-level naming");
            assertFalse(source.contains("ir.daneshrefah.scm.uaa.security." + "token"),
                    () -> file + " references removed security.token package");
        }
    }

    @Test
    void authenticationAndOauthTokensLiveInResponsibilityPackages() {
        assertJavaFileExists("ir/daneshrefah/scm/uaa/security/authentication/token/PreAuthenticationToken.java");
        assertJavaFileExists("ir/daneshrefah/scm/uaa/security/authentication/token/UserLoginAuthenticationToken.java");
        assertJavaFileExists("ir/daneshrefah/scm/uaa/security/authentication/token/AuthenticationOutcomeToken.java");
        assertJavaFileExists("ir/daneshrefah/scm/uaa/security/authentication/method/LoginAuthenticationTokenFactory.java");
        assertJavaFileExists("ir/daneshrefah/scm/uaa/security/authentication/method/token/SmsOtpRequestLoginAuthenticationToken.java");
        assertJavaFileExists("ir/daneshrefah/scm/uaa/security/authentication/method/token/DeviceOtpVerifyLoginAuthenticationToken.java");
        assertJavaFileExists("ir/daneshrefah/scm/uaa/security/oauth2/token/AuthenticationResponseTokenGenerator.java");
        assertJavaFileExists("ir/daneshrefah/scm/uaa/security/oauth2/token/OAuth2AuthenticationRequestTokenGenerator.java");
    }

    @Test
    void shahkarConverterIsThinAndProviderDelegatesBusinessFlow() throws Exception {
        String converter = Files.readString(MAIN.resolve(
                "ir/daneshrefah/scm/uaa/security/oauth2/grant/shahkar/ShahkarGrantAuthenticationConverter.java"
        ));
        assertTrue(converter.contains("ShahkarGrantRequestMapper"));
        assertFalse(converter.contains("Hazelcast"));
        assertFalse(converter.contains("ShahkarOwnershipService"));
        assertFalse(converter.contains("OtpService"));
        assertFalse(converter.contains("OtpUserService"));
        assertFalse(converter.contains("createShahkarVerifiedUserAndDeleteOld"));

        String provider = Files.readString(MAIN.resolve(
                "ir/daneshrefah/scm/uaa/security/oauth2/grant/shahkar/ShahkarGrantAuthenticationProvider.java"
        ));
        assertTrue(provider.contains("ShahkarAuthenticationFlowService"));
        assertFalse(provider.contains("OtpService"));
        assertFalse(provider.contains("ShahkarOwnershipService"));
        assertFalse(provider.contains("UserService"));
    }

    @Test
    void shahkarFlowOrdersOwnershipChallengeOtpAndVerifiedUserCreation() throws Exception {
        String flow = Files.readString(MAIN.resolve(
                "ir/daneshrefah/scm/uaa/security/oauth2/grant/shahkar/flow/ShahkarAuthenticationFlowService.java"
        ));
        int ownership = flow.indexOf("ownershipVerificationService.verify");
        int challenge = flow.indexOf("otpChallengeService.send");
        int otpVerification = flow.indexOf("otpVerificationService.verify");
        int createVerifiedUser = flow.indexOf("createShahkarVerifiedUserAndDeleteOld");
        assertTrue(ownership >= 0 && ownership < challenge);
        assertTrue(ownership < otpVerification);
        assertTrue(otpVerification < createVerifiedUser);
    }

    @Test
    void shahkarSessionCacheIsIsolatedFromConverterAndResponseFilter() throws Exception {
        String session = Files.readString(MAIN.resolve(
                "ir/daneshrefah/scm/uaa/security/oauth2/grant/shahkar/session/ShahkarRefreshTokenSessionService.java"
        ));
        String converter = Files.readString(MAIN.resolve(
                "ir/daneshrefah/scm/uaa/security/oauth2/grant/shahkar/ShahkarGrantAuthenticationConverter.java"
        ));
        String responseFilter = Files.readString(MAIN.resolve(
                "ir/daneshrefah/scm/uaa/filter/ResponseProxyAdviosrFilter.java"
        ));
        assertTrue(session.contains("HazelcastInstance"));
        assertFalse(converter.contains("HazelcastInstance"));
        assertFalse(responseFilter.contains("refresh:token-cache"));
    }

    @Test
    void legacyPasswordSafetyTypesAndPasswordEncoderUsageRemain() throws Exception {
        assertJavaFileExists("ir/daneshrefah/scm/uaa/security/password/LegacyPassword.java");
        assertJavaFileExists("ir/daneshrefah/scm/uaa/security/password/LegacyUsernameSaltedMd5PasswordEncoder.java");
        String provider = Files.readString(MAIN.resolve(
                "ir/daneshrefah/scm/uaa/security/authentication/method/LoginAuthenticationMethodProvider.java"
        ));
        assertTrue(provider.contains("passwordEncoder.matches("));
        assertFalse(provider.contains("MessageDigest"));
        assertFalse(provider.toLowerCase().contains("getinstance(\"md5\")"));
        String encoderConfig = Files.readString(MAIN.resolve(
                "ir/daneshrefah/scm/uaa/config/PasswordEncoderConfig.java"
        ));
        assertTrue(encoderConfig.contains("LegacyUsernameSaltedMd5PasswordEncoder"));
        for (Path file : javaFiles(MAIN)) {
            if (!file.getFileName().toString().contains("Provider")) {
                continue;
            }
            String source = Files.readString(file).toLowerCase();
            assertFalse(source.contains("messagedigest"), () -> file + " manually hashes passwords");
            assertFalse(source.contains("getinstance(\"md5\")"), () -> file + " manually hashes passwords");
        }
    }

    @Test
    void shahkarRefreshCompatibilityDefaultsToSuperAppClient() throws Exception {
        String refreshController = Files.readString(MAIN.resolve(
                "ir/daneshrefah/scm/uaa/controller/token/LegacyRefreshController.java"
        ));
        assertTrue(refreshController.contains("SCM_UAA_LEGACY_SUPER_APP_CLIENT_ID:SA"));
        assertFalse(refreshController.contains("client-id:MB"));
    }

    @Test
    void secondPasswordGrantIsNotRegisteredAnywhereInAuthorizationServerConfig() throws Exception {
        String source = Files.readString(MAIN.resolve("ir/daneshrefah/scm/uaa/config/AuthorizationServerSecurityConfig.java"));

        assertFalse(source.contains("second_" + "password"));
        assertFalse(source.contains("SECOND_" + "PASSWORD"));
    }

    @Test
    void requestHintAloneCannotForcePwaCookieForNonPwaClient() {
        LegacyAuthProperties properties = enabledLegacyProperties(true);
        LegacyClientTypeResolver resolver = new LegacyClientTypeResolver(properties);
        RegisteredClientLegacyPolicy registeredPolicy = new RegisteredClientLegacyPolicy(properties);
        LegacyCookiePolicy cookiePolicy = new LegacyCookiePolicy(registeredPolicy);

        LegacyClientType resolved = resolver.resolve(
                registeredClient("MB", LegacyClientType.MB),
                "MB",
                new LegacyAppVersion("PWA-REQUEST-HINT"),
                AuthorizationGrantType.DEFAULT
        );

        assertEquals(LegacyClientType.MB, resolved);
        assertFalse(cookiePolicy.canCreateCookie(registeredClient("MB", LegacyClientType.MB), resolved));
    }

    @Test
    void mbNeverReceivesCookie() {
        LegacyAuthProperties properties = enabledLegacyProperties(true);
        LegacyClientTypeResolver resolver = new LegacyClientTypeResolver(properties);
        RegisteredClientLegacyPolicy registeredPolicy = new RegisteredClientLegacyPolicy(properties);
        LegacyCookiePolicy cookiePolicy = new LegacyCookiePolicy(registeredPolicy);
        LegacyClientType resolved = resolver.resolve(
                registeredClient("MB", LegacyClientType.MB),
                "MB",
                new LegacyAppVersion("MB-ANDROID-1.0.0"),
                AuthorizationGrantType.DEFAULT
        );

        assertEquals(LegacyClientType.MB, resolved);
        assertTrue(registeredPolicy.isMbHeaderOnly(registeredClient("MB", LegacyClientType.MB), resolved));
        assertFalse(cookiePolicy.canCreateCookie(registeredClient("MB", LegacyClientType.MB), resolved));
    }

    @Test
    void pwaCookieRequiresGlobalAndClientPolicy() {
        RegisteredClientLegacyPolicy enabledPolicy = new RegisteredClientLegacyPolicy(enabledLegacyProperties(true));
        RegisteredClient pwaClient = registeredClient("PWA", LegacyClientType.PWA);
        RegisteredClient webClient = registeredClient("WEB", null);

        assertTrue(enabledPolicy.isPwaCookieAllowed(pwaClient, LegacyClientType.PWA));
        assertFalse(enabledPolicy.isPwaCookieAllowed(webClient, LegacyClientType.PWA));
        assertFalse(enabledPolicy.isPwaCookieAllowed(
                registeredClient("PWA", LegacyClientType.PWA, RegisteredClientLegacyPolicy.SETTING_LEGACY_PWA_COOKIE_ENABLED, false),
                LegacyClientType.PWA
        ));
        assertFalse(new RegisteredClientLegacyPolicy(enabledLegacyProperties(false))
                .isPwaCookieAllowed(pwaClient, LegacyClientType.PWA));
    }

    @Test
    void legacyPasswordGrantCanBeDisabledGloballyAndForNonLegacyClients() {
        RegisteredClient pwaClient = registeredClient("PWA", LegacyClientType.PWA);
        RegisteredClient webClient = registeredClient("WEB", null);

        assertFalse(new RegisteredClientLegacyPolicy(enabledLegacyProperties(false))
                .isLegacyPasswordGrantAllowed(pwaClient, LegacyClientType.PWA));
        assertFalse(new RegisteredClientLegacyPolicy(enabledLegacyProperties(true))
                .isLegacyPasswordGrantAllowed(webClient, LegacyClientType.PWA));
    }

    @Test
    void pwaCookieCanBeCreatedOnlyByPwaCookieDeliveryClass() throws Exception {
        Path allowed = MAIN
                .resolve("ir/daneshrefah/scm/uaa/security/oauth2/grant/legacy/delivery/LegacyPwaCookieTokenDeliveryStrategy.java")
                .normalize();
        for (Path file : javaFiles(MAIN)) {
            String source = Files.readString(file);
            if (source.contains("SET_COOKIE") || source.contains("Set-Cookie") || source.contains("addCookie(")) {
                assertEquals(allowed, file.normalize(), () -> file + " must not create legacy token cookies");
            }
        }
    }

    @Test
    void legacyPasswordMapperDoesNotOwnClientFallbackPolicy() throws Exception {
        String source = Files.readString(MAIN.resolve(
                "ir/daneshrefah/scm/uaa/security/oauth2/grant/legacy/converter/LegacyPasswordGrantRequestMapper.java"
        ));

        assertFalse(source.contains("defaultClientId"));
        assertFalse(source.contains("return \"PWA\""));
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
                assertFalse(line.contains("getCredentials()"), () -> file + " must not log raw credentials");
            }
        }
        for (String relativePath : List.of(
                "ir/daneshrefah/scm/uaa/service/shahkar/ShahkarOwnershipService.java",
                "ir/daneshrefah/scm/uaa/service/user/OtpUserService.java"
        )) {
            Path file = MAIN.resolve(relativePath);
            for (String line : Files.readString(file).lines().filter(line -> line.contains("log.")).toList()) {
                assertFalse(line.contains("request.getNationalId()"), () -> file + " logs a raw national code");
                assertFalse(line.contains("accessParameter,"), () -> file + " logs a raw mobile/access parameter");
                assertFalse(line.contains("getResponseBodyAsString"), () -> file + " logs a provider response body");
            }
        }
    }

    private LegacyAuthProperties enabledLegacyProperties(boolean enabled) {
        LegacyAuthProperties properties = new LegacyAuthProperties();
        properties.setEnabled(enabled);
        properties.getPwa().getCookie().setEnabled(enabled);
        return properties;
    }

    private RegisteredClient registeredClient(String clientId, LegacyClientType clientType) {
        return registeredClient(clientId, clientType, null, null);
    }

    private RegisteredClient registeredClient(String clientId, LegacyClientType clientType, String settingKey, Object settingValue) {
        ClientSettings.Builder settings = ClientSettings.builder();
        if (clientType != null) {
            settings.setting(RegisteredClientLegacyPolicy.SETTING_LEGACY_CLIENT_TYPE, clientType.name());
        }
        if (settingKey != null) {
            settings.setting(settingKey, settingValue);
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

    private void assertJavaFileExists(String packagePath) {
        assertTrue(Files.exists(MAIN.resolve(packagePath)), () -> packagePath + " must exist");
    }

    private void assertJavaFileDoesNotExist(String packagePath) {
        assertFalse(Files.exists(MAIN.resolve(packagePath)), () -> packagePath + " must not exist");
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
