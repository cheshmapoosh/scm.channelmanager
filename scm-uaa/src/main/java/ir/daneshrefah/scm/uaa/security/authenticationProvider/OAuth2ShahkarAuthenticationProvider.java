package ir.daneshrefah.scm.uaa.security.authenticationProvider;

import ir.daneshrefah.scm.uaa.common.utils.Constants;
import ir.daneshrefah.scm.uaa.common.utils.ErrorUtils;
import ir.daneshrefah.scm.uaa.security.token.OAuth2ShahkarAuthenticationToken;
import ir.daneshrefah.scm.uaa.security.token.OAuth2SmsOtpAuthenticationToken;
import ir.daneshrefah.scm.uaa.service.otp.OtpService;
import ir.daneshrefah.scm.uaa.service.otp.dto.OtpVerifyRequest;
import ir.daneshrefah.scm.uaa.service.otp.dto.OtpVerifyResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
import org.springframework.stereotype.Component;

import static ir.daneshrefah.scm.common.constant.SecurityConstants.ROLE_SMS_OTP_AUTHENTICATED;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-05-27
 */
@Component
@Slf4j
public class OAuth2ShahkarAuthenticationProvider extends BaseTokenAuthenticationProvider<OAuth2ShahkarAuthenticationToken> {

    private final OtpService otpService;

    public OAuth2ShahkarAuthenticationProvider(RegisteredClientRepository registeredClientRepository, OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator, OtpService otpService) {
        super(registeredClientRepository, tokenGenerator);
        this.otpService = otpService;
    }

    @Override
    protected OAuth2ShahkarAuthenticationToken authenticateToken(OAuth2ShahkarAuthenticationToken authenticationToken) {
        //TODO verify otp
        //TODO verify shahkar

        return new OAuth2ShahkarAuthenticationToken(authenticationToken.getPrincipal(), authenticationToken.getCredentials(),
                authenticationToken.getPhoneNumber(), authenticationToken.getScopes(), authenticationToken.getClientPrincipal(),
                AuthorityUtils.commaSeparatedStringToAuthorityList(ROLE_SMS_OTP_AUTHENTICATED));
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return OAuth2ShahkarAuthenticationToken.class.isAssignableFrom(authentication);
    }

}
