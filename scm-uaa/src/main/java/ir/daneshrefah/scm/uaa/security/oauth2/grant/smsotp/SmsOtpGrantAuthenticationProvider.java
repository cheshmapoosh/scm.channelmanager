package ir.daneshrefah.scm.uaa.security.oauth2.grant.smsotp;

import ir.daneshrefah.scm.common.constant.otp.OtpReason;
import ir.daneshrefah.scm.common.constant.otp.OtpType;
import ir.daneshrefah.scm.common.model.recipient.Recipient;
import ir.daneshrefah.scm.common.model.user.UserIdentifierType;
import ir.daneshrefah.scm.uaa.common.core.AuthorizationGrantType;
import ir.daneshrefah.scm.uaa.common.model.user.User;
import ir.daneshrefah.scm.uaa.common.security.authenticationDetails.TerminalUserDetails;
import ir.daneshrefah.scm.uaa.common.utils.Constants;
import ir.daneshrefah.scm.uaa.common.utils.ErrorUtils;
import ir.daneshrefah.scm.uaa.security.oauth2.grant.OAuth2TokenGrantAuthenticationProvider;
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

@Component
@Slf4j
public class SmsOtpGrantAuthenticationProvider extends OAuth2TokenGrantAuthenticationProvider<SmsOtpGrantAuthenticationToken> {
    private final OtpService otpService;
    private final UserService userService;

    public SmsOtpGrantAuthenticationProvider(
            RegisteredClientRepository registeredClientRepository,
            OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator,
            OtpService otpService,
            UserService userService
    ) {
        super(registeredClientRepository, tokenGenerator);
        this.otpService = otpService;
        this.userService = userService;
    }

    @Override
    protected AuthorizationGrantType grantType() {
        return AuthorizationGrantType.SMS_OTP;
    }

    @Override
    protected SmsOtpGrantAuthenticationToken authenticateToken(SmsOtpGrantAuthenticationToken authenticationToken) {
        String mobileNumber;
        if (authenticationToken.getPrincipal() instanceof String value) {
            mobileNumber = value;
        } else {
            ErrorUtils.throwError(Constants.OAUTH2_ERROR_CODE_INVALID_USER, Constants.OAUTH2_PARAM_NAME_USER_USERNAME);
            return null;
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
        return new SmsOtpGrantAuthenticationToken(
                new TerminalUserDetails(user),
                authenticationToken.getCredentials(),
                authenticationToken.getScopes(),
                authenticationToken.getClientPrincipal(),
                AuthorityUtils.commaSeparatedStringToAuthorityList(ROLE_SMS_OTP_AUTHENTICATED)
        );
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return SmsOtpGrantAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
