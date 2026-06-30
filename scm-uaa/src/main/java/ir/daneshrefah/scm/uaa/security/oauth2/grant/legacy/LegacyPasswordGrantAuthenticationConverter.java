package ir.daneshrefah.scm.uaa.security.oauth2.grant.legacy;

import ir.daneshrefah.scm.uaa.common.core.AuthorizationGrantType;
import ir.daneshrefah.scm.uaa.common.utils.Constants;
import ir.daneshrefah.scm.uaa.security.token.DefaultGrantPreAuthenticationToken;
import ir.daneshrefah.scm.uaa.utils.RequestUtils;
import ir.daneshrefah.scm.uaa.utils.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static ir.daneshrefah.scm.uaa.common.utils.Constants.PRE_AUTHENTICATION_INSTANCE;
import static ir.daneshrefah.scm.uaa.common.utils.ErrorUtils.throwError;

/**
 * Legacy password converter kept only for old NIB/PWA/MB compatibility.
 * Remove this class after migration to UAA-hosted login and authorization-code flow is complete.
 * No new feature should be added here unless strictly required for migration safety.
 */
@Deprecated(since = "9.0.0", forRemoval = true)
public class LegacyPasswordGrantAuthenticationConverter implements AuthenticationConverter {
    private final LegacyClientTypeResolver clientTypeResolver;

    public LegacyPasswordGrantAuthenticationConverter(LegacyClientTypeResolver clientTypeResolver) {
        this.clientTypeResolver = clientTypeResolver;
    }

    @Override
    public Authentication convert(HttpServletRequest request) {
        AuthorizationGrantType grantType = grantType(request);
        if (!AuthorizationGrantType.FIRST_PASSWORD.equals(grantType) && !AuthorizationGrantType.DEFAULT.equals(grantType)) {
            return null;
        }

        Authentication clientPrincipal = SecurityContextHolder.getContext().getAuthentication();
        ParameterSearch parameters = new ParameterSearch(request, AuthorizationGrantType.DEFAULT.equals(grantType));
        String username = requiredSingle(parameters, OAuth2ParameterNames.USERNAME, Constants.OAUTH2_PARAM_NAME_USER_USERNAME);
        String password = password(parameters, grantType);
        String appVersion = parameters.getFirst(Constants.APP_VERSION_HEADER).orElse(null);
        String clientId = clientId(clientPrincipal, request, appVersion, grantType);
        Set<String> scopes = scopes(parameters);
        LegacyClientType legacyClientType = clientTypeResolver.resolve(clientId, appVersion, grantType);
        LegacyPasswordGrantRequest grantRequest = new LegacyPasswordGrantRequest(
                username,
                password,
                grantType,
                clientPrincipal,
                scopes,
                clientId,
                request.getRemoteAddr(),
                legacyClientType
        );
        LegacyPasswordGrantAuthenticationToken token = new LegacyPasswordGrantAuthenticationToken(grantRequest);
        applyCommonFields(token, request, parameters);
        if (AuthorizationGrantType.DEFAULT.equals(grantType)) {
            applyDefaultGrantFields(token, request, parameters);
        }
        request.setAttribute(PRE_AUTHENTICATION_INSTANCE, token);
        return token;
    }

    private AuthorizationGrantType grantType(HttpServletRequest request) {
        return AuthorizationGrantType.findByCode(request.getParameter(OAuth2ParameterNames.GRANT_TYPE));
    }

    private String password(ParameterSearch parameters, AuthorizationGrantType grantType) {
        Optional<String> value = parameters.getFirst(OAuth2ParameterNames.PASSWORD);
        if (AuthorizationGrantType.DEFAULT.equals(grantType)) {
            return value.map(SecurityUtils.getInstance()::decryptPassword).orElse(null);
        }
        return value.filter(StringUtils::hasText)
                .orElseGet(() -> {
                    throwError(Constants.OAUTH2_ERROR_CODE_INVALID_USER, Constants.OAUTH2_PARAM_NAME_USER_PASSWORD);
                    return null;
                });
    }

    private String clientId(
            Authentication clientPrincipal,
            HttpServletRequest request,
            String appVersion,
            AuthorizationGrantType grantType
    ) {
        String clientId;
        if (clientPrincipal == null || clientPrincipal instanceof AnonymousAuthenticationToken) {
            clientId = AuthorizationGrantType.DEFAULT.equals(grantType)
                    ? defaultClientId(appVersion)
                    : request.getParameter(OAuth2ParameterNames.CLIENT_ID);
        } else {
            clientId = clientPrincipal.getName();
        }
        if (!StringUtils.hasText(clientId)) {
            throwError(OAuth2ErrorCodes.INVALID_CLIENT, OAuth2ParameterNames.CLIENT_ID);
        }
        return clientId;
    }

    private String defaultClientId(String appVersion) {
        if (StringUtils.startsWithIgnoreCase(appVersion, "MB")) {
            return "MB";
        }
        if (StringUtils.startsWithIgnoreCase(appVersion, "SA")) {
            return "SA";
        }
        return "PWA";
    }

    private String requiredSingle(ParameterSearch parameters, String name, String errorParameter) {
        Optional<String> value = parameters.getFirst(name);
        if (value.isEmpty() || !StringUtils.hasText(value.get())) {
            throwError(Constants.OAUTH2_ERROR_CODE_INVALID_USER, errorParameter);
        }
        return value.get();
    }

    private Set<String> scopes(ParameterSearch parameters) {
        return parameters
                .getFirst(OAuth2ParameterNames.SCOPE)
                .filter(org.apache.commons.lang3.StringUtils::isNotBlank)
                .map(s -> new HashSet<>(Arrays.asList(StringUtils.delimitedListToStringArray(s, " "))))
                .orElse(null);
    }

    private void applyCommonFields(
            LegacyPasswordGrantAuthenticationToken token,
            HttpServletRequest request,
            ParameterSearch parameters
    ) {
        token.setAccessParameter(request.getParameter(Constants.OAUTH2_PARAM_NAME_ACCESS_PARAMETER));
        token.setClaimCode(request.getParameter(Constants.OAUTH2_PARAM_NAME_USER_CLAIM));
        token.setClientVersion(request.getParameter(Constants.OAUTH2_PARAM_NAME_CLIENT_VERSION));
        token.setClientSignature(request.getParameter(Constants.OAUTH2_PARAM_NAME_CLIENT_SIGNATURE));
        token.setActivationCode(request.getParameter(Constants.OAUTH2_PARAM_NAME_USER_REGISTER_CODE));
        token.setActivatorTerminal(request.getParameter(Constants.OAUTH2_PARAM_NAME_ACTIVATOR_TERMINAL));
        parameters.getFirst(Constants.APP_VERSION_HEADER).ifPresent(token::setClientVersion);
        parameters.getFirst(Constants.SIGNATURE_HEADER).ifPresent(token::setClientSignature);
        parameters.getFirst(Constants.ACCESS_PARAM_HEADER).ifPresent(token::setAccessParameter);
    }

    private void applyDefaultGrantFields(
            LegacyPasswordGrantAuthenticationToken token,
            HttpServletRequest request,
            ParameterSearch parameters
    ) {
        DefaultGrantPreAuthenticationToken defaultGrantToken = new DefaultGrantPreAuthenticationToken();
        parameters.getFirst(Constants.CHANNEL_HEADER).ifPresent(defaultGrantToken::setChannel);
        parameters.getFirst(Constants.APP_VERSION_HEADER).ifPresent(defaultGrantToken::setAppVersion);
        parameters.getFirst(Constants.SIGNATURE_HEADER).ifPresent(defaultGrantToken::setSignature);
        parameters.getFirst(Constants.ACCESS_PARAM_HEADER).ifPresent(defaultGrantToken::setAccessParam);
        parameters.getFirst(Constants.HASHCODE_HEADER).ifPresent(defaultGrantToken::setHashcode);
        parameters.getFirst(Constants.AGENT_HEADER).ifPresent(defaultGrantToken::setAgent);
        parameters.getFirst(Constants.REGISTRY_TOKEN_HEADER).ifPresent(defaultGrantToken::setRegistryToken);
        parameters.getFirst(Constants.OPERATING_SYSTEM_VERSION_HEADER).ifPresent(defaultGrantToken::setOperationSystemVersion);
        parameters.getFirst(Constants.DEVICE_MODEL_HEADER).ifPresent(defaultGrantToken::setDeviceModel);
        parameters.getFirst(Constants.UUID_HEADER).ifPresent(defaultGrantToken::setUuid);
        parameters.getFirst(Constants.PWA_OTP_CODE_HEADER).ifPresent(defaultGrantToken::setOtpCode);
        parameters.getFirst(Constants.PWA_OTP_CODE_HEADER).ifPresent(token::setClaimCode);
        parameters.getFirst(Constants.PWA_TERMINAL_TYPE_HEADER).ifPresent(defaultGrantToken::setTerminalType);
        defaultGrantToken.setIp(RequestUtils.getOrDefaultRequestIp(null, request));
        token.setDefaultGrantPreAuthToken(defaultGrantToken);
    }

    private static class ParameterSearch {
        private final MultiValueMap<String, String> parameters;

        private ParameterSearch(HttpServletRequest request, boolean includeHeaders) {
            parameters = getParameters(request, includeHeaders);
        }

        private Optional<String> getFirst(String parameterName) {
            return Optional.ofNullable(parameters.getFirst(org.apache.commons.lang3.StringUtils.toRootLowerCase(parameterName)));
        }

        private MultiValueMap<String, String> getParameters(HttpServletRequest request, boolean includeHeaders) {
            Map<String, String[]> parameterMap = request.getParameterMap();
            MultiValueMap<String, String> result = new LinkedMultiValueMap<>(parameterMap.size());
            if (includeHeaders) {
                result.putAll(RequestUtils.getRequestHeaders(request));
            }
            parameterMap.forEach((key, values) -> {
                for (String value : values) {
                    result.add(org.apache.commons.lang3.StringUtils.toRootLowerCase(key), value);
                }
            });
            return result;
        }
    }
}
