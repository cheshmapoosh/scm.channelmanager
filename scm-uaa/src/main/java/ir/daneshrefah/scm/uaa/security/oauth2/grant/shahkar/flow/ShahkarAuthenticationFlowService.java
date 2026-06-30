package ir.daneshrefah.scm.uaa.security.oauth2.grant.shahkar.flow;

import ir.daneshrefah.scm.uaa.common.core.AuthorizationGrantType;
import ir.daneshrefah.scm.uaa.common.model.user.User;
import ir.daneshrefah.scm.uaa.common.security.authenticationDetails.TerminalUserDetails;
import ir.daneshrefah.scm.uaa.common.utils.Constants;
import ir.daneshrefah.scm.uaa.security.oauth2.grant.legacy.LegacyClientType;
import ir.daneshrefah.scm.uaa.security.oauth2.grant.legacy.client.LegacyAppVersion;
import ir.daneshrefah.scm.uaa.security.oauth2.grant.legacy.client.LegacyClientTypeResolver;
import ir.daneshrefah.scm.uaa.security.oauth2.grant.shahkar.ShahkarGrantAuthenticationToken;
import ir.daneshrefah.scm.uaa.service.user.UserService;
import ir.daneshrefah.scm.utils.validation.ValidationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Instant;

import static ir.daneshrefah.scm.common.constant.SecurityConstants.ROLE_SHAHKAR_AUTHENTICATED;
import static ir.daneshrefah.scm.uaa.common.utils.Constants.CLIENT_SETTING_KEY_TERMINAL_CODE;
import static ir.daneshrefah.scm.uaa.common.utils.ErrorUtils.throwError;

@Service
@RequiredArgsConstructor
@SuppressWarnings("removal")
public class ShahkarAuthenticationFlowService {
    public static final String SECOND_STEP_REQUIRED = "second_step_required";

    private final LegacyClientTypeResolver clientTypeResolver;
    private final ShahkarOwnershipVerificationService ownershipVerificationService;
    private final ShahkarOtpChallengeService otpChallengeService;
    private final ShahkarOtpVerificationService otpVerificationService;
    private final UserService userService;

    public ShahkarGrantAuthenticationToken authenticate(ShahkarGrantAuthenticationToken token) {
        validateSuperAppPolicy(token);
        if (token.isAuthenticated()) {
            return token;
        }

        String nationalCode = nationalCode(token);
        String mobileNumber = mobileNumber(token);
        String terminalCode = terminalCode(token);
        ownershipVerificationService.verify(nationalCode, mobileNumber);

        if (!StringUtils.hasText(token.getActivationCode())) {
            otpChallengeService.send(nationalCode, mobileNumber, terminalCode);
            throwError(SECOND_STEP_REQUIRED, Constants.PWA_OTP_CODE_HEADER);
        }

        otpVerificationService.verify(token, terminalCode);
        User user = userService.createShahkarVerifiedUserAndDeleteOld(
                nationalCode,
                mobileNumber,
                terminalCode
        );
        return authenticatedToken(token, user);
    }

    private void validateSuperAppPolicy(ShahkarGrantAuthenticationToken token) {
        LegacyClientType clientType = clientTypeResolver.resolve(
                token.getRegisteredClient(),
                token.getClientId(),
                new LegacyAppVersion(token.getAppVersion()),
                AuthorizationGrantType.SHAHKAR
        );
        if (!LegacyClientType.SA.equals(clientType)) {
            throwError(OAuth2ErrorCodes.INVALID_CLIENT, OAuth2ParameterNames.CLIENT_ID);
        }
    }

    private String nationalCode(ShahkarGrantAuthenticationToken token) {
        Object principal = token.getPrincipal();
        if (!(principal instanceof String)) {
            throwError(Constants.OAUTH2_ERROR_CODE_INVALID_USER, Constants.OAUTH2_PARAM_NAME_USER_USERNAME);
        }
        String nationalCode = (String) principal;
        if (!ValidationUtils.checkIsValidNationalCode(nationalCode)) {
            throwError(Constants.OAUTH2_ERROR_CODE_INVALID_USER, Constants.OAUTH2_PARAM_NAME_USER_USERNAME);
        }
        return nationalCode;
    }

    private String mobileNumber(ShahkarGrantAuthenticationToken token) {
        String mobileNumber = token.getPhoneNumber();
        if (!ValidationUtils.checkIsValidMobileNumber(mobileNumber)) {
            throwError(Constants.OAUTH2_ERROR_CODE_INVALID_USER, Constants.OAUTH2_PARAM_NAME_MOBILE_NUMBER);
        }
        return mobileNumber;
    }

    private String terminalCode(ShahkarGrantAuthenticationToken token) {
        String terminalCode = token.getRegisteredClient()
                .getClientSettings()
                .getSetting(CLIENT_SETTING_KEY_TERMINAL_CODE);
        if (!StringUtils.hasText(terminalCode)) {
            throwError(OAuth2ErrorCodes.INVALID_CLIENT, OAuth2ParameterNames.CLIENT_ID);
        }
        return terminalCode;
    }

    private ShahkarGrantAuthenticationToken authenticatedToken(
            ShahkarGrantAuthenticationToken source,
            User user
    ) {
        Instant now = Instant.now();
        ShahkarGrantAuthenticationToken result = new ShahkarGrantAuthenticationToken(
                new TerminalUserDetails(user),
                source.getPhoneNumber(),
                source.getCredentials(),
                source.getScopes(),
                source.getClientPrincipal(),
                source.getAppVersion(),
                AuthorityUtils.commaSeparatedStringToAuthorityList(ROLE_SHAHKAR_AUTHENTICATED),
                now,
                now
        );
        result.setAccessParameter(source.getAccessParameter());
        result.setClientId(source.getClientId());
        result.setClientVersion(source.getClientVersion());
        result.setClientSignature(source.getClientSignature());
        result.setActivationCode(source.getActivationCode());
        result.setRegisteredClient(source.getRegisteredClient());
        return result;
    }
}
