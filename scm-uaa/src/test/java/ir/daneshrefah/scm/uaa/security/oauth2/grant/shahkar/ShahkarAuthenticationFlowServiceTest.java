package ir.daneshrefah.scm.uaa.security.oauth2.grant.shahkar;

import ir.daneshrefah.scm.uaa.common.model.user.User;
import ir.daneshrefah.scm.uaa.common.utils.Constants;
import ir.daneshrefah.scm.uaa.security.oauth2.grant.legacy.LegacyClientType;
import ir.daneshrefah.scm.uaa.security.oauth2.grant.legacy.client.LegacyClientTypeResolver;
import ir.daneshrefah.scm.uaa.security.oauth2.grant.shahkar.flow.ShahkarAuthenticationFlowService;
import ir.daneshrefah.scm.uaa.security.oauth2.grant.shahkar.flow.ShahkarOtpChallengeService;
import ir.daneshrefah.scm.uaa.security.oauth2.grant.shahkar.flow.ShahkarOtpVerificationService;
import ir.daneshrefah.scm.uaa.security.oauth2.grant.shahkar.flow.ShahkarOwnershipVerificationService;
import ir.daneshrefah.scm.uaa.security.oauth2.policy.RegisteredClientLegacyPolicy;
import ir.daneshrefah.scm.uaa.service.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("removal")
class ShahkarAuthenticationFlowServiceTest {
    private LegacyClientTypeResolver clientTypeResolver;
    private ShahkarOwnershipVerificationService ownershipVerificationService;
    private ShahkarOtpChallengeService otpChallengeService;
    private ShahkarOtpVerificationService otpVerificationService;
    private UserService userService;
    private ShahkarAuthenticationFlowService flowService;

    @BeforeEach
    void setUp() {
        clientTypeResolver = mock(LegacyClientTypeResolver.class);
        ownershipVerificationService = mock(ShahkarOwnershipVerificationService.class);
        otpChallengeService = mock(ShahkarOtpChallengeService.class);
        otpVerificationService = mock(ShahkarOtpVerificationService.class);
        userService = mock(UserService.class);
        when(clientTypeResolver.resolve(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any()))
                .thenReturn(LegacyClientType.SA);
        flowService = new ShahkarAuthenticationFlowService(
                clientTypeResolver,
                ownershipVerificationService,
                otpChallengeService,
                otpVerificationService,
                userService
        );
    }

    @Test
    void firstStepChecksOwnershipAndSendsOtpWithoutCreatingVerifiedUser() {
        ShahkarGrantAuthenticationToken token = token(null);

        OAuth2AuthenticationException exception = assertThrows(OAuth2AuthenticationException.class, () ->
                flowService.authenticate(token));

        assertEquals(ShahkarAuthenticationFlowService.SECOND_STEP_REQUIRED,
                exception.getError().getErrorCode());
        InOrder order = inOrder(ownershipVerificationService, otpChallengeService);
        order.verify(ownershipVerificationService).verify("0084575948", "09121234567");
        order.verify(otpChallengeService).send("0084575948", "09121234567", "SUPER_APP");
        verify(otpVerificationService, never()).verify(org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any());
        verify(userService, never()).createShahkarVerifiedUserAndDeleteOld(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any()
        );
    }

    @Test
    void secondStepCreatesVerifiedUserOnlyAfterOtpVerification() {
        ShahkarGrantAuthenticationToken token = token("123456");
        User user = mock(User.class);
        when(userService.createShahkarVerifiedUserAndDeleteOld(
                "0084575948", "09121234567", "SUPER_APP"))
                .thenReturn(user);

        ShahkarGrantAuthenticationToken result = flowService.authenticate(token);

        assertTrue(result.isAuthenticated());
        assertSame(user, ((ir.daneshrefah.scm.uaa.common.security.authenticationDetails.TerminalUserDetails)
                result.getPrincipal()).getUser());
        InOrder order = inOrder(ownershipVerificationService, otpVerificationService, userService);
        order.verify(ownershipVerificationService).verify("0084575948", "09121234567");
        order.verify(otpVerificationService).verify(token, "SUPER_APP");
        order.verify(userService).createShahkarVerifiedUserAndDeleteOld(
                "0084575948", "09121234567", "SUPER_APP");
        verify(otpChallengeService, never()).send(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any()
        );
    }

    private ShahkarGrantAuthenticationToken token(String otpCode) {
        ShahkarGrantAuthenticationToken token = new ShahkarGrantAuthenticationToken(
                "0084575948",
                "09121234567",
                "09121234567",
                Set.of("openid"),
                null,
                "SA-3.0.0"
        );
        token.setActivationCode(otpCode);
        token.setClientId("configured-sa");
        token.setRegisteredClient(RegisteredClient.withId("sa-test")
                .clientId("configured-sa")
                .clientAuthenticationMethod(ClientAuthenticationMethod.NONE)
                .authorizationGrantType(new org.springframework.security.oauth2.core.AuthorizationGrantType("ext_shk"))
                .redirectUri("https://client.example/callback")
                .scope("openid")
                .clientSettings(ClientSettings.builder()
                        .setting(RegisteredClientLegacyPolicy.SETTING_LEGACY_CLIENT_TYPE, LegacyClientType.SA.name())
                        .setting(Constants.CLIENT_SETTING_KEY_TERMINAL_CODE, "SUPER_APP")
                        .build())
                .build());
        return token;
    }
}
