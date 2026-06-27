package ir.daneshrefah.scm.uaa.config;

import ir.daneshrefah.scm.observation.ObservationScope;
import ir.daneshrefah.scm.observation.ScmObservation;
import ir.daneshrefah.scm.observation.attributes.trace.CommonTraceAttributes;
import ir.daneshrefah.scm.uaa.observation.attributes.UaaTraceAttributes;
import ir.daneshrefah.scm.uaa.security.token.PreAuthenticationToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AccessTokenAuthenticationToken;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;


//INSERT INTO LOGGER.MESSAGE_LOG (ID, CORRELATION_ID, USERNAME, ACCESS_PARAM, BODY, IP, STATUS, TRANSACTION_DATE, SERVICE_TYPE, REAL_USERNAME, SUBMIT_DATE, ALLOCATED_TIME, CLIENT_TYPE, AMOUNT) VALUES (18188621, '1640862529925594', '5532377608', null, '{"description":"Success","serverCode":"1","additionalStatus":null,"name":"SUCCESS","code":"0","severity":"INFO"}', null, 'RESPONSE_FROM_CHANNEL', '2021-12-30 11:07:11.468000', 'LoanListInquiryResponse', '5532377608', '2021-12-30 14:37:20.150265', 0, null, null);
@Aspect
@Component
@ConditionalOnProperty(name = "scm.log.trace.aspect.enable", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class AuthenticationAspect {

    private final ScmObservation observation;

    @Around("execution(* authenticate(..)) && args(authentication) && within(ir.daneshrefah.scm.uaa.security.authenticationProvider.OAuth2GeneralAuthenticationProvider)")
    public Object traceAuthenticate(ProceedingJoinPoint joinPoint, Authentication authentication) throws Throwable {
        ObservationScope scope = observation.trace()
                .span("auth.authenticate")
                .spanKind("internal")
                .action("auth.authenticate")
                .attribute(UaaTraceAttributes.AUTH_TYPE, "oauth2")
                .attribute(UaaTraceAttributes.JWT_PRESENT, false)
                .attribute(UaaTraceAttributes.CLIENT_IP, remoteAddress(authentication))
                .attribute(UaaTraceAttributes.AUTH_CLIENT_TYPE, clientType(authentication))
                .start();
        try {
            Object result = joinPoint.proceed();
            scope.attribute(UaaTraceAttributes.JWT_PRESENT, hasAccessToken(result))
                    .attribute(UaaTraceAttributes.JWT_USERNAME, safeUsername(authentication));
            scope.success();
            return result;
        } catch (AuthenticationException ex) {
            scope.attribute(CommonTraceAttributes.ERROR_TYPE, ex.getClass().getName())
                    .attribute(CommonTraceAttributes.ERROR_MESSAGE, safeMessage(ex))
                    .failure();
            log.trace("Authentication failed", ex);
            throw ex;
        } catch (Throwable ex) {
            scope.attribute(CommonTraceAttributes.ERROR_TYPE, ex.getClass().getName())
                    .attribute(CommonTraceAttributes.ERROR_MESSAGE, safeMessage(ex))
                    .failure();
            log.error("Unexpected error during authentication", ex);
            throw ex;
        } finally {
            scope.close();
        }
    }

    private boolean hasAccessToken(Object result) {
        return result instanceof OAuth2AccessTokenAuthenticationToken token && token.getAccessToken() != null;
    }

    private String remoteAddress(Authentication authentication) {
        if (authentication instanceof PreAuthenticationToken preAuthenticationToken) {
            return textOrNull(preAuthenticationToken.getRemoteAddress());
        }
        Object details = authentication == null ? null : authentication.getDetails();
        if (details instanceof WebAuthenticationDetails webAuthenticationDetails) {
            return textOrNull(webAuthenticationDetails.getRemoteAddress());
        }
        return null;
    }

    private String safeUsername(Authentication authentication) {
        String username = null;
        if (authentication instanceof PreAuthenticationToken preAuthenticationToken) {
            username = preAuthenticationToken.getUsername();
        }
        if ((username == null || username.isBlank()) && authentication != null) {
            username = authentication.getName();
        }
        username = textOrNull(username);
        if (username == null) {
            return null;
        }
        username = username.replace('\r', ' ').replace('\n', ' ').trim();
        return username.length() > 128 ? username.substring(0, 128) : username;
    }

    private String clientType(Authentication authentication) {
        if (authentication instanceof PreAuthenticationToken preAuthenticationToken) {
            if (preAuthenticationToken.hasDefaultGrantPreAuthToken()) {
                return preAuthenticationToken.getDefaultGrantPreAuthToken().getAppVersion();
            }
            return preAuthenticationToken.getClientId();
        }
        return null;
    }

    private String textOrNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String safeMessage(Throwable exception) {
        if (exception == null || exception.getMessage() == null) {
            return null;
        }
        String message = exception.getMessage()
                .replace('\r', ' ')
                .replace('\n', ' ')
                .replaceAll("(?i)(password|token|authorization|client_secret|authorization_code|pin|otp|session[_-]?id|card[_-]?number)\\s*[:=]\\s*\\S+", "$1=***")
                .trim();
        return message.length() > 300 ? message.substring(0, 300) : message;
    }
}
