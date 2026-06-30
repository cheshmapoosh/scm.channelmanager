package ir.daneshrefah.scm.uaa.service.proxy;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.model.message.TokenType;
import ir.daneshrefah.scm.common.model.service.HttpContentType;
import ir.daneshrefah.scm.uaa.common.constants.PwaOauthMessage;
import ir.daneshrefah.scm.uaa.common.utils.Constants;
import ir.daneshrefah.scm.uaa.security.oauth2.grant.legacy.LegacyClientType;
import ir.daneshrefah.scm.uaa.security.oauth2.grant.legacy.LegacyClientTypeResolver;
import ir.daneshrefah.scm.uaa.security.oauth2.grant.legacy.LegacyTokenDeliveryContext;
import ir.daneshrefah.scm.uaa.security.oauth2.grant.legacy.LegacyTokenDeliveryStrategy;
import ir.daneshrefah.scm.uaa.security.token.AbstractAuthenticationToken;
import ir.daneshrefah.scm.uaa.security.token.OAuth2ShahkarAuthenticationToken;
import ir.daneshrefah.scm.uaa.security.token.PreAuthenticationToken;
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
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

import static ir.daneshrefah.scm.uaa.common.constants.PwaOauthMessage.*;
import static ir.daneshrefah.scm.uaa.common.utils.Constants.PRE_AUTHENTICATION_INSTANCE;

/**
 * Legacy PWA/MB response adapter kept only for old compatibility.
 * Remove this class after migration to UAA-hosted login and authorization-code flow is complete.
 * No new feature should be added here unless strictly required for migration safety.
 */
@Component
@Slf4j
@RequiredArgsConstructor
@Deprecated(since = "9.0.0", forRemoval = true)
@SuppressWarnings("removal")
public class LegacyPwaOauthLoginResponseProxyAdvisor implements ResponseProxyAdvisor {

    private static final String LEGACY_APP_VERSION = "pwa";
    private static final String UAA_ERROR_PROP = "error";
    private static final String UAA_ERROR_DESC_PROP = "error_description";
    private static final String UAA_ACCESS_TOKEN_PROP = "access_token";
    private static final String UAA_EXPIRE_IN_PROP = "expires_in";
    private static final String UAA_TOKEN_TYPE_PROP = "token_type";
    private final ObjectMapper objectMapper;
    private final PwaOauthResponseMapper responseMapper;
    private final PwaAuthenticationService pwaAuthenticationService;
    private final LegacyClientTypeResolver clientTypeResolver;
    private final List<LegacyTokenDeliveryStrategy> tokenDeliveryStrategies;

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
        LegacyTokenDeliveryContext context = new LegacyTokenDeliveryContext(clientType, request, response, token);
        tokenDeliveryStrategies.stream()
                .filter(strategy -> strategy.supports(clientType))
                .forEach(strategy -> strategy.deliver(context));
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
        if(request.getAttribute(PRE_AUTHENTICATION_INSTANCE) instanceof OAuth2ShahkarAuthenticationToken){
            return false;
        }
        return Optional
                .ofNullable(request.getHeader(Constants.APP_VERSION_HEADER))
                .stream()
                .anyMatch(s -> StringUtils.equalsIgnoreCase(s, LEGACY_APP_VERSION) || StringUtils.startsWithIgnoreCase(s, "MB"));
    }

    @Override
    public String filterUrlPathPattern() {
        return Urls.OAUTH2_TOKEN;
    }

    private LegacyClientType resolveLegacyClientType(HttpServletRequest request) {
        Object preAuthentication = request.getAttribute(PRE_AUTHENTICATION_INSTANCE);
        String clientId = preAuthentication instanceof AbstractAuthenticationToken token ? token.getClientId() : null;
        return clientTypeResolver.resolve(clientId, request.getHeader(Constants.APP_VERSION_HEADER), null);
    }

    private String safeMessage(Throwable exception) {
        if (exception == null || exception.getMessage() == null) {
            return exception == null ? null : exception.getClass().getSimpleName();
        }
        String message = exception.getMessage()
                .replace('\r', ' ')
                .replace('\n', ' ')
                .replaceAll("(?i)(password|token|authorization|client_secret|authorization_code|pin|otp|session[_-]?id|card[_-]?number|registry[_-]?token)\\s*[:=]\\s*\\S+", "$1=***")
                .trim();
        return message.length() > 300 ? message.substring(0, 300) : message;
    }
}
