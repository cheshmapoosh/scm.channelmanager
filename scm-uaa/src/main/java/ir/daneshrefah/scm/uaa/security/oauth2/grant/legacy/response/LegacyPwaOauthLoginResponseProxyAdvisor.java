package ir.daneshrefah.scm.uaa.security.oauth2.grant.legacy.response;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.model.message.TokenType;
import ir.daneshrefah.scm.common.model.service.HttpContentType;
import ir.daneshrefah.scm.uaa.common.constants.PwaOauthMessage;
import ir.daneshrefah.scm.uaa.common.core.AuthorizationGrantType;
import ir.daneshrefah.scm.uaa.security.oauth2.grant.legacy.LegacyClientType;
import ir.daneshrefah.scm.uaa.security.oauth2.grant.legacy.client.LegacyAppVersion;
import ir.daneshrefah.scm.uaa.security.oauth2.grant.legacy.client.LegacyClientTypeResolver;
import ir.daneshrefah.scm.uaa.security.oauth2.grant.legacy.converter.LegacyRequestParameters;
import ir.daneshrefah.scm.uaa.security.oauth2.grant.legacy.delivery.LegacyPwaCookieTokenDeliveryStrategy;
import ir.daneshrefah.scm.uaa.security.oauth2.grant.legacy.delivery.LegacyTokenDeliveryContext;
import ir.daneshrefah.scm.uaa.security.oauth2.grant.shahkar.ShahkarGrantAuthenticationToken;
import ir.daneshrefah.scm.uaa.security.authentication.token.PreAuthenticationToken;
import ir.daneshrefah.scm.uaa.service.activation.pwa.common.PwaOauthResponseMapper;
import ir.daneshrefah.scm.uaa.service.activation.pwa.model.ActivationResponse;
import ir.daneshrefah.scm.uaa.service.activation.pwa.services.authentication.PwaAuthenticationService;
import ir.daneshrefah.scm.uaa.service.activation.pwa.services.authentication.model.PwaOAuth2AccessToken;
import ir.daneshrefah.scm.uaa.service.proxy.spec.ResponseProxy;
import ir.daneshrefah.scm.uaa.service.proxy.spec.ResponseProxyAdvisor;
import ir.daneshrefah.scm.uaa.utils.Urls;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.stereotype.Component;

import static ir.daneshrefah.scm.uaa.common.constants.PwaOauthMessage.*;
import static ir.daneshrefah.scm.uaa.common.utils.Constants.PRE_AUTHENTICATION_INSTANCE;

/**
 * Legacy PWA/MB response adapter kept only for old NIB/PWA/MB compatibility.
 * Remove this class after migration to UAA-hosted login and authorization-code flow is complete.
 * No new feature should be added here unless strictly required for migration safety.
 */
@Component
@ConditionalOnBean(name = "activationDataSource")
@Slf4j
@RequiredArgsConstructor
@Deprecated(since = "9.0.0", forRemoval = true)
@SuppressWarnings("removal")
public class LegacyPwaOauthLoginResponseProxyAdvisor implements ResponseProxyAdvisor {

    private static final String UAA_ERROR_PROP = "error";
    private static final String UAA_ERROR_DESC_PROP = "error_description";
    private static final String UAA_ACCESS_TOKEN_PROP = "access_token";
    private static final String UAA_EXPIRE_IN_PROP = "expires_in";
    private static final String UAA_TOKEN_TYPE_PROP = "token_type";
    private final ObjectMapper objectMapper;
    private final PwaOauthResponseMapper responseMapper;
    private final PwaAuthenticationService pwaAuthenticationService;
    private final LegacyClientTypeResolver clientTypeResolver;
    private final LegacyPwaCookieTokenDeliveryStrategy pwaCookieTokenDeliveryStrategy;

    @Override
    public ResponseProxy<?> applyProxy(HttpServletRequest request, HttpServletResponse response, String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            PreAuthenticationToken preAuthenticationToken = (PreAuthenticationToken) request.getAttribute(PRE_AUTHENTICATION_INSTANCE);
            if (root.has(UAA_ERROR_PROP)) {
                if (root.get(UAA_ERROR_DESC_PROP).asText().contains("required_claim")) {
                    PwaOAuth2AccessToken resp = pwaAuthenticationService.createTwoPhaseLoginResponse(preAuthenticationToken);
                    deliverToken(resp, request, response);
                    return createResponseProxy(resp);
                }
                PwaOauthMessage error = resolveOauthMessage(root);
                if (!error.equals(REACHED_LOGIN_LIMIT)) {
                    pwaAuthenticationService.checkLoginTrails(preAuthenticationToken);
                }
                ActivationResponse resp = responseMapper.getMessage(error);
                return createResponseProxy(resp);
            } else if (root.has(UAA_ACCESS_TOKEN_PROP)) {
                String jwt = root.get(UAA_ACCESS_TOKEN_PROP).asText();
                int expiresIn = root.get(UAA_EXPIRE_IN_PROP).asInt();
                TokenType tokenType = TokenType.valueOf(root.get(UAA_TOKEN_TYPE_PROP).asText().toUpperCase());
                PwaOAuth2AccessToken resp = pwaAuthenticationService.postAuthenticate(preAuthenticationToken, tokenType, jwt, expiresIn);
                deliverToken(resp, request, response);
                return createResponseProxy(resp);
            } else {
                return createResponseProxy(responseMapper.getMessage(CLIENT_LOGIN_FAILED));
            }
        } catch (Exception e) {
            log.error("legacy response proxy failed: {}", safeMessage(e));
            return createResponseProxy(responseMapper.getMessage(INVALID_CREDENTIALS));
        }
    }

    private PwaOauthMessage resolveOauthMessage(JsonNode root) {
        return PwaOauthMessage
                .findByText(root.get(UAA_ERROR_PROP).asText())
                .orElseGet(() -> {
                    String msg = root.get(UAA_ERROR_DESC_PROP).asText().replace("OAuth 2.0 Parameter:", StringUtils.EMPTY).trim();
                    return PwaOauthMessage.findByText(msg).orElseGet(() -> mapUaaOauthError(root.get(UAA_ERROR_PROP).asText()));
                });
    }

    private void deliverToken(PwaOAuth2AccessToken token, HttpServletRequest request, HttpServletResponse response) {
        LegacyClientType clientType = resolveLegacyClientType(request);
        if (!LegacyClientType.PWA.equals(clientType)) {
            return;
        }
        PreAuthenticationToken preAuthenticationToken = preAuthenticationToken(request);
        LegacyTokenDeliveryContext context = new LegacyTokenDeliveryContext(
                clientType,
                preAuthenticationToken == null ? null : preAuthenticationToken.getRegisteredClient(),
                request,
                response,
                token
        );
        if (pwaCookieTokenDeliveryStrategy.supports(context)) {
            pwaCookieTokenDeliveryStrategy.deliver(context);
        }
    }

    private PwaOauthMessage mapUaaOauthError(String error) {
        return switch (error) {
            case "invalid_claim" -> PwaOauthMessage.CLIENT_INVALID_OTP;
            case "invalid_user", "invalid_password" -> INVALID_CREDENTIALS;
            default -> PwaOauthMessage.CLIENT_LOGIN_FAILED;
        };
    }

    private ResponseProxy<ActivationResponse> createResponseProxy(ActivationResponse response) {
        ResponseProxy<ActivationResponse> proxy = new ResponseProxy<>();
        proxy.setResponseBody(response);
        proxy.setHttpStatusCode(response.getHttpCode().value());
        proxy.setContentType(HttpContentType.RAW_JSON.getValue());
        return proxy;
    }

    private ResponseProxy<PwaOAuth2AccessToken> createResponseProxy(PwaOAuth2AccessToken token) {
        ResponseProxy<PwaOAuth2AccessToken> proxy = new ResponseProxy<>();
        proxy.setResponseBody(token);
        proxy.setHttpStatusCode(HttpServletResponse.SC_OK);
        proxy.setContentType(HttpContentType.RAW_JSON.getValue());
        return proxy;
    }

    @Override
    public boolean support(HttpServletRequest request) {
        if (request.getAttribute(PRE_AUTHENTICATION_INSTANCE) instanceof ShahkarGrantAuthenticationToken) {
            return false;
        }
        LegacyClientType clientType = resolveLegacyClientType(request);
        return LegacyClientType.PWA.equals(clientType) || LegacyClientType.MB.equals(clientType);
    }

    @Override
    public String filterUrlPathPattern() {
        return Urls.OAUTH2_TOKEN;
    }

    private LegacyClientType resolveLegacyClientType(HttpServletRequest request) {
        PreAuthenticationToken preAuthentication = preAuthenticationToken(request);
        LegacyRequestParameters parameters = new LegacyRequestParameters(request);
        String clientId = preAuthentication == null ? null : preAuthentication.getClientId();
        LegacyAppVersion appVersion = new LegacyAppVersion(parameters.appVersion().orElse(null));
        AuthorizationGrantType grantType = preAuthentication == null
                ? AuthorizationGrantType.findByCode(
                        parameters.firstParameter(OAuth2ParameterNames.GRANT_TYPE).orElse(null))
                : preAuthentication.getGrantType();
        return clientTypeResolver.resolve(
                preAuthentication == null ? null : preAuthentication.getRegisteredClient(),
                clientId,
                appVersion,
                grantType
        );
    }

    private PreAuthenticationToken preAuthenticationToken(HttpServletRequest request) {
        Object preAuthentication = request.getAttribute(PRE_AUTHENTICATION_INSTANCE);
        return preAuthentication instanceof PreAuthenticationToken token ? token : null;
    }

    private String safeMessage(Throwable exception) {
        if (exception == null || exception.getMessage() == null) {
            return exception == null ? null : exception.getClass().getSimpleName();
        }
        String message = exception.getMessage()
                .replace('\r', ' ')
                .replace('\n', ' ')
                .replaceAll("(?i)(password|token|authorization|client_secret|authorization_code|pin|otp|session[_-]?id|card[_-]?number|registry[_-]?token|mobile|national[_-]?code|access[_-]?parameter)\\s*[:=]\\s*\\S+", "$1=***")
                .trim();
        return message.length() > 300 ? message.substring(0, 300) : message;
    }
}
