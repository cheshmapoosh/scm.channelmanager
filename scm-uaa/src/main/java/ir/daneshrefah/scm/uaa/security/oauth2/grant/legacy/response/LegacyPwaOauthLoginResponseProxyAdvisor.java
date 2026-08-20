package ir.daneshrefah.scm.uaa.security.oauth2.grant.legacy.response;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.model.message.TokenType;
import ir.daneshrefah.scm.common.model.service.HttpContentType;
import ir.daneshrefah.scm.uaa.common.constants.PwaOauthMessage;
import ir.daneshrefah.scm.uaa.common.core.AuthorizationGrantType;
import ir.daneshrefah.scm.uaa.common.utils.Constants;
import ir.daneshrefah.scm.uaa.security.authentication.token.PreAuthenticationToken;
import ir.daneshrefah.scm.uaa.security.oauth2.grant.legacy.LegacyClientType;
import ir.daneshrefah.scm.uaa.security.oauth2.grant.legacy.client.LegacyAppVersion;
import ir.daneshrefah.scm.uaa.security.oauth2.grant.legacy.client.LegacyClientTypeResolver;
import ir.daneshrefah.scm.uaa.security.oauth2.grant.legacy.converter.LegacyRequestParameters;
import ir.daneshrefah.scm.uaa.security.oauth2.grant.shahkar.ShahkarGrantAuthenticationToken;
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
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

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

    private static final String LEGACY_APP_VERSION = "pwa";

    private static final String UAA_ERROR_PROP = "error";
    private static final String UAA_ERROR_DESC_PROP = "error_description";
    private static final String UAA_ACCESS_TOKEN_PROP = "access_token";
    private static final String UAA_EXPIRE_IN_PROP = "expires_in";
    private static final String UAA_TOKEN_TYPE_PROP = "token_type";

    private static final String AUTH_COOKIE_NAME = HttpHeaders.AUTHORIZATION;
    private static final String REGISTRY_TOKEN_COOKIE_NAME = Constants.REGISTRY_TOKEN_HEADER;
    private static final String COOKIE_PATH = "/";
    private static final String SAME_SITE_LAX = "SameSite=Lax";
    private static final String COOKIE_HTTP_ONLY = "HttpOnly";
    private static final String COOKIE_SECURE = "Secure";

    private final ObjectMapper objectMapper;
    private final PwaOauthResponseMapper responseMapper;
    private final PwaAuthenticationService pwaAuthenticationService;
    private final LegacyClientTypeResolver clientTypeResolver;

    @Override
    public ResponseProxy<?> applyProxy(HttpServletRequest request, HttpServletResponse response, String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            PreAuthenticationToken preAuthenticationToken =
                    (PreAuthenticationToken) request.getAttribute(PRE_AUTHENTICATION_INSTANCE);

            if (root.has(UAA_ERROR_PROP)) {
                if (root.get(UAA_ERROR_DESC_PROP).asText().contains("required_claim")) {
                    PwaOAuth2AccessToken resp =
                            pwaAuthenticationService.createTwoPhaseLoginResponse(preAuthenticationToken);
                    applyResponseCookies(resp, request, response);
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

                PwaOAuth2AccessToken resp =
                        pwaAuthenticationService.postAuthenticate(preAuthenticationToken, tokenType, jwt, expiresIn);

                applyResponseCookies(resp, request, response);
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
                    String msg = root.get(UAA_ERROR_DESC_PROP)
                            .asText()
                            .replace("OAuth 2.0 Parameter:", StringUtils.EMPTY)
                            .trim();
                    return PwaOauthMessage.findByText(msg)
                            .orElseGet(() -> mapUaaOauthError(root.get(UAA_ERROR_PROP).asText()));
                });
    }

    /**
     * نسخه 9.0.2:
     * - Registry-Token از هدر درخواست خوانده می‌شود و اگر موجود باشد cookie می‌شود
     * - Access-Token هم به صورت cookie با نام Authorization ست می‌شود
     */
    private void applyResponseCookies(PwaOAuth2AccessToken token,
                                      HttpServletRequest request,
                                      HttpServletResponse response) {
        addRegistryTokenCookieIfPresent(request, response, token);
        addAccessTokenCookie(response, token);
    }

    private void addRegistryTokenCookieIfPresent(HttpServletRequest request,
                                                 HttpServletResponse response,
                                                 PwaOAuth2AccessToken token) {
        String registryToken = request.getHeader(Constants.REGISTRY_TOKEN_HEADER);
        if (StringUtils.isNotBlank(registryToken)) {
            String encodedRegistryToken = URLEncoder.encode(registryToken, StandardCharsets.UTF_8);
            String cookieValue = buildCookieHeader(REGISTRY_TOKEN_COOKIE_NAME, encodedRegistryToken, Integer.MAX_VALUE);
            response.addHeader(HttpHeaders.SET_COOKIE, cookieValue);
            token.setHasRegistryTokenFromCookie(true);
        }
    }

    private void addAccessTokenCookie(HttpServletResponse response, PwaOAuth2AccessToken token) {
        Optional.ofNullable(token.getAccessToken())
                .ifPresent(accessToken -> {
                    String encodedToken = URLEncoder.encode(accessToken, StandardCharsets.UTF_8);
                    long maxAgeSeconds = Math.max(
                            0,
                            (token.getExpirationDate().getTime() - System.currentTimeMillis()) / 1000
                    );
                    String cookieValue = buildCookieHeader(AUTH_COOKIE_NAME, encodedToken, maxAgeSeconds);
                    response.addHeader(HttpHeaders.SET_COOKIE, cookieValue);
                });
    }

    private String buildCookieHeader(String name, String value, long maxAgeSeconds) {
        return name + "=" + value +
                "; " + COOKIE_HTTP_ONLY +
                "; " + COOKIE_SECURE +
                "; Path=" + COOKIE_PATH +
                "; Max-Age=" + maxAgeSeconds +
                "; " + SAME_SITE_LAX;
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
                .replaceAll(
                        "(?i)(password|token|authorization|client_secret|authorization_code|pin|otp|session[_-]?id|card[_-]?number|registry[_-]?token|mobile|national[_-]?code|access[_-]?parameter)\\s*[:=]\\s*\\S+",
                        "$1=***"
                )
                .trim();

        return message.length() > 300 ? message.substring(0, 300) : message;
    }
}
