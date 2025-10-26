package ir.daneshrefah.scm.uaa.security.token;

import ir.daneshrefah.scm.uaa.common.core.AuthorizationGrantType;
import ir.daneshrefah.scm.common.model.user.AuthenticationMethod;
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
public enum AuthenticationTokenTypes {

    LOGIN_STATIC(FirstLvlStaticAuthenticationToken.class, AuthorizationGrantType.FIRST_PASSWORD, AuthenticationMethod.STATIC_PASSWORD, false),
    LOGIN_SMS_REQUEST(FirstLvlSmsRequestAuthenticationToken.class, AuthorizationGrantType.FIRST_PASSWORD, AuthenticationMethod.SMS, false),
    LOGIN_PIN(FirstLvlPinAuthenticationToken.class, AuthorizationGrantType.FIRST_PASSWORD, AuthenticationMethod.PIN, false),
    LOGIN_PATTERN(FirstLvlPatternAuthenticationToken.class, AuthorizationGrantType.FIRST_PASSWORD, AuthenticationMethod.PATTERN, false),
    LOGIN_SMS_VERIFY(FirstLvlSmsVerifyAuthenticationToken.class, AuthorizationGrantType.FIRST_PASSWORD, AuthenticationMethod.SMS, true),
//    LOGIN_OTP_REQUEST(FirstLvlOtpDeviceRequestAuthenticationToken.class, AuthorizationGrantType.FIRST_PASSWORD, AuthenticationMethod.OTP, false),
    LOGIN_OTP_VERIFY(FirstLvlOtpDeviceVerifyAuthenticationToken.class, AuthorizationGrantType.FIRST_PASSWORD, AuthenticationMethod.OTP, false),
    TRANSACTION_STATIC(SecondLvlStaticAuthenticationToken.class, AuthorizationGrantType.SECOND_PASSWORD, AuthenticationMethod.STATIC_PASSWORD, false),
    TRANSACTION_SMS(SecondLvlSmsAuthenticationToken.class, AuthorizationGrantType.SECOND_PASSWORD, AuthenticationMethod.SMS, false),
    TRANSACTION_OTP(SecondLvlOtpAuthenticationToken.class, AuthorizationGrantType.SECOND_PASSWORD, AuthenticationMethod.OTP, false),
    ;

    private final Class<? extends GeneralAuthenticationToken> tokenClass;
    private final AuthorizationGrantType grantType;
    private final AuthenticationMethod authenticationMethod;
    private final boolean claimCodeProvided;

}
