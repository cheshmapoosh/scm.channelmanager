package ir.daneshrefah.scm.uaa.security.oauth2.grant.legacy;

import ir.daneshrefah.scm.uaa.common.exception.TwoStepAuthenticationRequiredException;
import ir.daneshrefah.scm.uaa.common.utils.ErrorUtils;
import ir.daneshrefah.scm.uaa.security.authenticationProvider.BaseGeneralAuthenticationProvider;
import ir.daneshrefah.scm.uaa.security.token.GeneralAuthenticationToken;
import ir.daneshrefah.scm.uaa.security.token.PostAuthenticationToken;
import ir.daneshrefah.scm.uaa.security.token.PreAuthenticationToken;
import ir.daneshrefah.scm.uaa.security.token.generator.AuthenticationResponseTokenGenerator;
import ir.daneshrefah.scm.uaa.security.token.generator.OAuth2AuthenticationRequestTokenGenerator;
import ir.daneshrefah.scm.uaa.security.userDetails.UserDetailsService;
import ir.daneshrefah.scm.uaa.service.activation.nib.UserActivationAuthenticationService;
import ir.daneshrefah.scm.uaa.service.activation.pwa.services.authentication.PwaAuthenticationService;
import ir.daneshrefah.scm.uaa.service.client.ClientService;
import ir.daneshrefah.scm.uaa.service.otp.dto.OtpSendResponse;
import ir.daneshrefah.scm.utils.date.DateUtils;
import ir.daneshrefah.scm.utils.string.StringUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
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
 * Legacy password provider kept only for old NIB/PWA/MB compatibility.
 * Remove this class after migration to UAA-hosted login and authorization-code flow is complete.
 * No new feature should be added here unless strictly required for migration safety.
 */
@Component
@Deprecated(since = "9.0.0", forRemoval = true)
@SuppressWarnings("removal")
public class LegacyPasswordGrantAuthenticationProvider extends BaseGeneralAuthenticationProvider {
    private final AuthenticationResponseTokenGenerator responseTokenGenerator;

    public LegacyPasswordGrantAuthenticationProvider(
            RegisteredClientRepository clientRepository,
            UserCache userCache,
            ClientService clientService,
            UserDetailsService userDetailsService,
            OAuth2AuthenticationRequestTokenGenerator authenticationTokenGenerator,
            AuthenticationManager authenticationManager,
            AuthenticationResponseTokenGenerator responseTokenGenerator,
            UserActivationAuthenticationService userActivationService,
            PwaAuthenticationService pwaAuthenticationService
    ) {
        super(
                clientRepository,
                clientService,
                userCache,
                userDetailsService,
                authenticationTokenGenerator,
                authenticationManager,
                userActivationService,
                pwaAuthenticationService
        );
        this.responseTokenGenerator = responseTokenGenerator;
    }

    @Override
    protected PreAuthenticationToken extractPreAuthenticationToken(Authentication authentication) {
        PreAuthenticationToken preAuthenticationToken = (PreAuthenticationToken) authentication;
        Authentication clientPrincipal = preAuthenticationToken.getClientPrincipal();
        if (clientPrincipal instanceof OAuth2ClientAuthenticationToken clientAuthenticationToken) {
            preAuthenticationToken.setRegisteredClient(clientAuthenticationToken.getRegisteredClient());
        }
        return preAuthenticationToken;
    }

    @Override
    protected Authentication buildResponse(
            Authentication requestAuthentication,
            PreAuthenticationToken preAuthenticationToken,
            GeneralAuthenticationToken authentication
    ) {
        PostAuthenticationToken.AuthenticationStatus status = ((PostAuthenticationToken) authentication).getAuthenticationStatus();
        if (PostAuthenticationToken.AuthenticationStatus.INCOMPLETE.equals(status)) {
            throwError(authentication, new TwoStepAuthenticationRequiredException(authentication));
        }
        return responseTokenGenerator.getAccessToken(
                requestAuthentication,
                preAuthenticationToken.getClientPrincipal(),
                preAuthenticationToken.getRegisteredClient(),
                authentication
        );
    }

    @Override
    protected void throwError(Authentication authentication, Exception exception) throws AuthenticationException {
        String parameterName = extractParameterName(exception);
        String errorCode = customizeExceptionMessage(exception);
        if (StringUtils.isEmpty(errorCode)) {
            errorCode = parameterName;
        }
        ErrorUtils.throwError(errorCode, parameterName);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return LegacyPasswordGrantAuthenticationToken.class.isAssignableFrom(authentication);
    }

    private String customizeExceptionMessage(Exception exception) {
        if (exception == null || exception.getCause() != null) {
            return null;
        }
        if (exception instanceof TwoStepAuthenticationRequiredException twoStepException) {
            if (!(twoStepException.getAuthentication() instanceof PostAuthenticationToken authenticationToken)) {
                return null;
            }
            String code = authenticationToken.getPrincipal().getUser().getLoginAuthenticationMethod().getCode();
            Map<String, String> response = new HashMap<>();
            if (Objects.nonNull(authenticationToken.getOtpSendResponse())) {
                OtpSendResponse otpSendResponse = authenticationToken.getOtpSendResponse();
                Instant expireTimeInstant = otpSendResponse.getOtp().getExpireTime();
                LocalDateTime nowLocalDateTime = LocalDateTime.now();
                LocalDateTime expirationLocalDateTime = DateUtils.DateConverter.convertToLocalDateTime(
                        DateUtils.DateConverter.convertToTimestamp(expireTimeInstant)
                );
                response.put("expirationDurationSeconds",
                        String.valueOf(Duration.between(nowLocalDateTime, expirationLocalDateTime).toSeconds()));
                response.put("recipient", safeRecipient(otpSendResponse.getOtp().getRecipient().getAddress()));
            }
            response.put("authenticationMethod", code);
            return applyErrorCodeResponsePattern(response.toString());
        }
        return null;
    }

    private String applyErrorCodeResponsePattern(String responseString) {
        return responseString.replace("=", StringUtils.COLON);
    }

    private String safeRecipient(String recipient) {
        if (recipient == null || recipient.length() < 4) {
            return "****";
        }
        return "***" + recipient.substring(recipient.length() - 4);
    }
}
