package ir.daneshrefah.scm.uaa.security.authenticationProvider;


import ir.daneshrefah.scm.uaa.common.exception.TwoStepAuthenticationRequiredException;
import ir.daneshrefah.scm.uaa.common.utils.ErrorUtils;
import ir.daneshrefah.scm.uaa.security.token.GeneralAuthenticationToken;
import ir.daneshrefah.scm.uaa.security.token.PostAuthenticationToken;
import ir.daneshrefah.scm.uaa.security.token.PreAuthenticationToken;
import ir.daneshrefah.scm.uaa.security.token.generator.AuthenticationResponseTokenGenerator;
import ir.daneshrefah.scm.uaa.security.token.generator.OAuth2AuthenticationRequestTokenGenerator;
import ir.daneshrefah.scm.uaa.security.userDetails.UserDetailsService;
import ir.daneshrefah.scm.uaa.service.activation.UserActivationAuthenticationService;
import ir.daneshrefah.scm.uaa.service.client.ClientService;
import ir.daneshrefah.scm.uaa.service.otp.dto.OtpSendResponse;
import ir.daneshrefah.scm.utils.date.DateUtils;
import ir.daneshrefah.scm.utils.string.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserCache;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-18
 */
@Component
public class OAuth2GeneralAuthenticationProvider extends BaseGeneralAuthenticationProvider {

    private static final Logger log = LoggerFactory.getLogger(OAuth2GeneralAuthenticationProvider.class);
    private final AuthenticationResponseTokenGenerator responseTokenGenerator;

    public OAuth2GeneralAuthenticationProvider(RegisteredClientRepository clientRepository, UserCache userCache,
                                               ClientService clientService,
                                               UserDetailsService userDetailsService,
                                               OAuth2AuthenticationRequestTokenGenerator authenticationTokenGenerator,
                                               DelegatorAuthenticationProvider delegatorAuthenticationProvider,
                                               AuthenticationResponseTokenGenerator responseTokenGenerator,
                                               UserActivationAuthenticationService userActivationService) {
        super(clientRepository,
                clientService,
                userCache,
                userDetailsService,
                authenticationTokenGenerator,
                delegatorAuthenticationProvider,
                userActivationService);
        this.responseTokenGenerator = responseTokenGenerator;
    }

    @Override
    protected PreAuthenticationToken extractPreAuthenticationToken(Authentication authentication) {
        PreAuthenticationToken preAuthenticationToken = (PreAuthenticationToken) authentication;
        Authentication clientPrincipal = preAuthenticationToken.getClientPrincipal();
        if (null != clientPrincipal && clientPrincipal instanceof OAuth2ClientAuthenticationToken) {
            preAuthenticationToken.setRegisteredClient(((OAuth2ClientAuthenticationToken) clientPrincipal).getRegisteredClient());
        }
        return (PreAuthenticationToken) authentication;
    }

    @Override
    protected Authentication buildResponse(Authentication requestAuthentication,
                                           PreAuthenticationToken preAuthenticationToken, GeneralAuthenticationToken authentication) {
        PostAuthenticationToken.AuthenticationStatus status = ((PostAuthenticationToken) authentication).getAuthenticationStatus();
        if (PostAuthenticationToken.AuthenticationStatus.INCOMPLETE.equals(status)) {
            throwError(authentication, new TwoStepAuthenticationRequiredException(authentication));
        }

        return responseTokenGenerator.getAccessToken(requestAuthentication, preAuthenticationToken.getClientPrincipal(),
                preAuthenticationToken.getRegisteredClient(), authentication);
    }

    @Override
    protected void throwError(Authentication authentication, Exception exception) {
        String parameterName = extractParameterName(exception);
        String errorCode = customizeExceptionMessage(exception);
        if (StringUtils.isEmpty(errorCode)) {
            errorCode = parameterName;
        }
        ErrorUtils.throwError(errorCode, parameterName);
    }

    private String customizeExceptionMessage(Exception exception) {
        if (null != exception.getCause()){
            return null;
        }
        if (exception instanceof TwoStepAuthenticationRequiredException twoStepException){
            if (!(twoStepException.getAuthentication() instanceof PostAuthenticationToken authenticationToken)) {
                return null;
            }
            String code = authenticationToken.getPrincipal().getUser().getLoginAuthenticationMethod().getCode();
            Map<String,String> response = new HashMap<>();
            String expirationDuration;
            String recipient;
            if (Objects.nonNull(authenticationToken.getOtpSendResponse())){
                OtpSendResponse otpSendResponse = authenticationToken.getOtpSendResponse();
                Instant expireTimeInstant = otpSendResponse.getOtp().getExpireTime();
                LocalDateTime nowLocalDateTime = LocalDateTime.now();
                LocalDateTime expirationLocalDateTime = DateUtils.DateConverter.convertToLocalDateTime(DateUtils.DateConverter.convertToTimestamp(expireTimeInstant));
                expirationDuration = String.valueOf(Duration.between(nowLocalDateTime,expirationLocalDateTime).toSeconds());
                recipient = otpSendResponse.getOtp().getRecipient().getAddress();
                response.put("expirationDurationSeconds",expirationDuration);
                response.put("recipient",recipient);
                //TODO LOG FOR DEV
                log.info(">>> OTP CODE : {}", otpSendResponse.getOtp().getOtpCode());
            }
            response.put("authenticationMethod",code);
            String responseString = response.toString();
            return applyErrorCodeResponsePattern(responseString);
        }
        return null;
    }

    private String applyErrorCodeResponsePattern(String responseString) {
        responseString =responseString.replace("=",StringUtils.COLON);
        return responseString;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return PreAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
