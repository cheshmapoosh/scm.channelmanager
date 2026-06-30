package ir.daneshrefah.scm.uaa.security.authentication.method;

import ir.daneshrefah.scm.common.model.user.AuthenticationMethod;
import ir.daneshrefah.scm.uaa.common.core.AuthorizationGrantType;
import ir.daneshrefah.scm.uaa.common.model.user.User;
import ir.daneshrefah.scm.uaa.common.security.authenticationDetails.TerminalUserDetails;
import ir.daneshrefah.scm.uaa.security.authentication.method.token.DeviceOtpRequestLoginAuthenticationToken;
import ir.daneshrefah.scm.uaa.security.authentication.method.token.DeviceOtpVerifyLoginAuthenticationToken;
import ir.daneshrefah.scm.uaa.security.authentication.method.token.SmsOtpRequestLoginAuthenticationToken;
import ir.daneshrefah.scm.uaa.security.authentication.method.token.SmsOtpVerifyLoginAuthenticationToken;
import ir.daneshrefah.scm.uaa.security.authentication.token.PreAuthenticationToken;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class LoginAuthenticationTokenFactoryTest {
    private final LoginAuthenticationTokenFactory factory = new LoginAuthenticationTokenFactory();

    @Test
    void defaultGrantUsesSmsRequestAndVerifyLoginTokens() {
        TerminalUserDetails user = user(AuthenticationMethod.SMS);

        assertInstanceOf(SmsOtpRequestLoginAuthenticationToken.class,
                factory.create(defaultGrant(null), user));
        assertInstanceOf(SmsOtpVerifyLoginAuthenticationToken.class,
                factory.create(defaultGrant("123456"), user));
    }

    @Test
    void defaultGrantUsesDeviceRequestAndVerifyLoginTokens() {
        TerminalUserDetails user = user(AuthenticationMethod.OTP);

        assertInstanceOf(DeviceOtpRequestLoginAuthenticationToken.class,
                factory.create(defaultGrant(null), user));
        assertInstanceOf(DeviceOtpVerifyLoginAuthenticationToken.class,
                factory.create(defaultGrant("123456"), user));
    }

    private PreAuthenticationToken defaultGrant(String claimCode) {
        PreAuthenticationToken token = new PreAuthenticationToken(
                "legacy-user",
                "password",
                AuthorizationGrantType.DEFAULT,
                null,
                Set.of("openid"),
                null
        );
        token.setClaimCode(claimCode);
        return token;
    }

    private TerminalUserDetails user(AuthenticationMethod authenticationMethod) {
        User user = new User();
        user.setLoginAuthenticationMethod(authenticationMethod);
        return new TerminalUserDetails(user);
    }
}
