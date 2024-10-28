package ir.daneshrefah.scm.uaa.security.authenticationProvider;

import ir.daneshrefah.scm.common.constant.otp.OtpReason;
import ir.daneshrefah.scm.common.constant.otp.OtpType;
import ir.daneshrefah.scm.common.model.recipient.Recipient;
import ir.daneshrefah.scm.common.model.user.UserIdentifierType;
import ir.daneshrefah.scm.uaa.common.model.user.User;
import ir.daneshrefah.scm.uaa.common.security.authenticationDetails.TerminalUserDetails;
import ir.daneshrefah.scm.uaa.common.utils.Constants;
import ir.daneshrefah.scm.uaa.common.utils.ErrorUtils;
import ir.daneshrefah.scm.uaa.security.token.OAuth2SmsOtpAuthenticationToken;
import ir.daneshrefah.scm.uaa.service.otp.OtpService;
import ir.daneshrefah.scm.uaa.service.otp.dto.OtpVerifyRequest;
import ir.daneshrefah.scm.uaa.service.otp.dto.OtpVerifyResponse;
import ir.daneshrefah.scm.uaa.service.user.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
import org.springframework.stereotype.Component;

import static ir.daneshrefah.scm.common.constant.SecurityConstants.ROLE_SMS_OTP_AUTHENTICATED;
import static ir.daneshrefah.scm.uaa.common.utils.Constants.CLIENT_SETTING_KEY_TERMINAL_CODE;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-05-27
 */
@Component
@Slf4j
public class OAuth2SmsOtpAuthenticationProvider extends BaseTokenAuthenticationProvider<OAuth2SmsOtpAuthenticationToken> {

    private final OtpService otpService;
    private final UserService userService;

    public OAuth2SmsOtpAuthenticationProvider(RegisteredClientRepository registeredClientRepository, OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator,
                                              OtpService otpService, UserService userService) {
        super(registeredClientRepository, tokenGenerator);
        this.otpService = otpService;
        this.userService = userService;
    }


    @Override
    protected OAuth2SmsOtpAuthenticationToken authenticateToken(OAuth2SmsOtpAuthenticationToken authenticationToken) {
        String mobileNumber = null;
        if (authenticationToken.getPrincipal() instanceof String) {
            mobileNumber = (String) authenticationToken.getPrincipal();
        } else {
            ErrorUtils.throwError(Constants.OAUTH2_ERROR_CODE_INVALID_USER, Constants.OAUTH2_PARAM_NAME_USER_USERNAME);
        }
        String terminalCode = authenticationToken.getRegisteredClient().getClientSettings().getSetting(CLIENT_SETTING_KEY_TERMINAL_CODE);
        Recipient recipient = Recipient.builder()
                .address(mobileNumber)
                .identifier(mobileNumber)
                .identifierType(UserIdentifierType.MOBILE_NUMBER)
                .terminalCode(terminalCode)
                .accessParameter(authenticationToken.getAccessParameter())
                .build();

        OtpVerifyRequest request = OtpVerifyRequest.builder()
                .otpType(OtpType.SMS)
                .reason(OtpReason.AUTHENTICATION)
                .recipient(recipient)
                .claimCode(authenticationToken.getCredentials())
                .build();
        OtpVerifyResponse verifyResponse = otpService.verifyOtp(request);
        if (!verifyResponse.isSuccessful()) {
            ErrorUtils.throwError(Constants.OAUTH2_ERROR_CODE_INVALID_CLAIM, verifyResponse.getErrorMessage());
        }

        User user = userService.createSmsVerifiedUserAndDeleteOld(mobileNumber, terminalCode);

        return new OAuth2SmsOtpAuthenticationToken(new TerminalUserDetails(user), authenticationToken.getCredentials(),
                authenticationToken.getScopes(), authenticationToken.getClientPrincipal(),
                AuthorityUtils.commaSeparatedStringToAuthorityList(ROLE_SMS_OTP_AUTHENTICATED));
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return OAuth2SmsOtpAuthenticationToken.class.isAssignableFrom(authentication);
    }

}
