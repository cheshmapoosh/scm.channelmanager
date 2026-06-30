package ir.daneshrefah.scm.uaa.security.oauth2.grant.shahkar;

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
import ir.daneshrefah.scm.uaa.security.token.OAuth2ShahkarAuthenticationToken;
import ir.daneshrefah.scm.uaa.service.otp.OtpService;
import ir.daneshrefah.scm.uaa.service.otp.dto.OtpVerifyRequest;
import ir.daneshrefah.scm.uaa.service.otp.dto.OtpVerifyResponse;
import ir.daneshrefah.scm.uaa.service.user.UserService;
import ir.daneshrefah.scm.utils.string.StringUtils;
import ir.daneshrefah.scm.utils.validation.ValidationUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
import org.springframework.stereotype.Component;

import java.time.Instant;

import static ir.daneshrefah.scm.common.constant.SecurityConstants.ROLE_SHAHKAR_AUTHENTICATED;
import static ir.daneshrefah.scm.uaa.common.utils.Constants.CLIENT_SETTING_KEY_TERMINAL_CODE;

@Component
@Slf4j
public class ShahkarGrantAuthenticationProvider extends OAuth2TokenGrantAuthenticationProvider<OAuth2ShahkarAuthenticationToken> {
    private final OtpService otpService;
    private final UserService userService;

    public ShahkarGrantAuthenticationProvider(
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
        return AuthorizationGrantType.SHAHKAR;
    }

    @Override
    protected OAuth2ShahkarAuthenticationToken authenticateToken(OAuth2ShahkarAuthenticationToken authenticationToken) {
        String nationalCode = null;
        if (authenticationToken.getPrincipal() instanceof String value) {
            nationalCode = value;
        } else {
            ErrorUtils.throwError(Constants.OAUTH2_ERROR_CODE_INVALID_USER, Constants.OAUTH2_PARAM_NAME_USER_USERNAME);
        }
        if (!ValidationUtils.checkIsValidNationalCode(nationalCode)) {
            ErrorUtils.throwError(Constants.OAUTH2_ERROR_CODE_INVALID_USER, Constants.OAUTH2_PARAM_NAME_USER_USERNAME);
        }

        String mobileNumber = authenticationToken.getPhoneNumber();
        if (!ValidationUtils.checkIsValidMobileNumber(mobileNumber)) {
            ErrorUtils.throwError(Constants.OAUTH2_ERROR_CODE_INVALID_USER, Constants.OAUTH2_PARAM_NAME_MOBILE_NUMBER);
        }
        String otpCode = authenticationToken.getActivationCode();
        String terminalCode = authenticationToken.getRegisteredClient().getClientSettings().getSetting(CLIENT_SETTING_KEY_TERMINAL_CODE);
        User user = userService.createShahkarVerifiedUserAndDeleteOld(nationalCode, mobileNumber, terminalCode);
        ShahkarGrantAuthenticationToken result = new ShahkarGrantAuthenticationToken(
                new TerminalUserDetails(user),
                mobileNumber,
                authenticationToken.getCredentials(),
                authenticationToken.getScopes(),
                authenticationToken.getClientPrincipal(),
                AuthorityUtils.commaSeparatedStringToAuthorityList(ROLE_SHAHKAR_AUTHENTICATED),
                Instant.now(),
                Instant.now()
        );
        if (StringUtils.isNotEmpty(otpCode) && StringUtils.notEquals("-", otpCode)) {
            verifyOtp(authenticationToken, mobileNumber, terminalCode);
            result.setAuthenticated(true);
        } else {
            result.setAuthenticated(false);
        }
        return result;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return OAuth2ShahkarAuthenticationToken.class.isAssignableFrom(authentication);
    }

    private void verifyOtp(
            OAuth2ShahkarAuthenticationToken authenticationToken,
            String mobileNumber,
            String terminalCode
    ) {
        Recipient recipient = Recipient.builder()
                .address(mobileNumber)
                .identifier(mobileNumber)
                .identifierType(UserIdentifierType.MOBILE_NUMBER)
                .terminalCode(terminalCode)
                .accessParameter(authenticationToken.getAccessParameter())
                .build();
        OtpVerifyRequest request = OtpVerifyRequest.builder()
                .otpType(OtpType.SMS)
                .reason(OtpReason.SHAHKAR_AUTHENTICATION)
                .recipient(recipient)
                .claimCode(authenticationToken.getActivationCode())
                .build();
        OtpVerifyResponse verifyResponse = otpService.verifyOtp(request);
        if (!verifyResponse.isSuccessful()) {
            ErrorUtils.throwError(Constants.OAUTH2_ERROR_CODE_INVALID_CLAIM, verifyResponse.getErrorMessage());
        }
    }
}
