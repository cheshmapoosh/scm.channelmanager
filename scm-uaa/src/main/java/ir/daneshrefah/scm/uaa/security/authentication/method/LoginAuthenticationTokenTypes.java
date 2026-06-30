package ir.daneshrefah.scm.uaa.security.authentication.method;

import ir.daneshrefah.scm.uaa.common.core.AuthorizationGrantType;
import ir.daneshrefah.scm.common.model.user.AuthenticationMethod;
import ir.daneshrefah.scm.uaa.security.authentication.method.token.DeviceOtpRequestLoginAuthenticationToken;
import ir.daneshrefah.scm.uaa.security.authentication.method.token.DeviceOtpVerifyLoginAuthenticationToken;
import ir.daneshrefah.scm.uaa.security.authentication.method.token.PatternLoginAuthenticationToken;
import ir.daneshrefah.scm.uaa.security.authentication.method.token.PinLoginAuthenticationToken;
import ir.daneshrefah.scm.uaa.security.authentication.method.token.SmsOtpRequestLoginAuthenticationToken;
import ir.daneshrefah.scm.uaa.security.authentication.method.token.SmsOtpVerifyLoginAuthenticationToken;
import ir.daneshrefah.scm.uaa.security.authentication.method.token.StaticPasswordLoginAuthenticationToken;
import ir.daneshrefah.scm.uaa.security.authentication.token.UserLoginAuthenticationToken;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-23
 */
@AllArgsConstructor
@Getter
public enum LoginAuthenticationTokenTypes {

    LOGIN_STATIC_PASSWORD(StaticPasswordLoginAuthenticationToken.class, AuthorizationGrantType.FIRST_PASSWORD, AuthenticationMethod.STATIC_PASSWORD, false),
    LOGIN_SMS_OTP_REQUEST(SmsOtpRequestLoginAuthenticationToken.class, AuthorizationGrantType.FIRST_PASSWORD, AuthenticationMethod.SMS, false),
    LOGIN_SMS_OTP_VERIFY(SmsOtpVerifyLoginAuthenticationToken.class, AuthorizationGrantType.FIRST_PASSWORD, AuthenticationMethod.SMS, true),
    LOGIN_PIN(PinLoginAuthenticationToken.class, AuthorizationGrantType.FIRST_PASSWORD, AuthenticationMethod.PIN, false),
    LOGIN_PATTERN(PatternLoginAuthenticationToken.class, AuthorizationGrantType.FIRST_PASSWORD, AuthenticationMethod.PATTERN, false),
    LOGIN_DEVICE_OTP_REQUEST(DeviceOtpRequestLoginAuthenticationToken.class, AuthorizationGrantType.FIRST_PASSWORD, AuthenticationMethod.OTP, false),
    LOGIN_DEVICE_OTP_VERIFY(DeviceOtpVerifyLoginAuthenticationToken.class, AuthorizationGrantType.FIRST_PASSWORD, AuthenticationMethod.OTP, true),
    ;

    private final Class<? extends UserLoginAuthenticationToken> tokenClass;
    private final AuthorizationGrantType grantType;
    private final AuthenticationMethod authenticationMethod;
    private final boolean claimCodeProvided;

}
