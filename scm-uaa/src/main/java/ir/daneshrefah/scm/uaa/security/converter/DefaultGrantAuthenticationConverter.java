package ir.daneshrefah.scm.uaa.security.converter;

import ir.daneshrefah.scm.uaa.common.core.AuthorizationGrantType;
import ir.daneshrefah.scm.uaa.common.utils.Constants;
import ir.daneshrefah.scm.uaa.security.token.PreAuthenticationToken;
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

import java.util.*;

import static ir.daneshrefah.scm.uaa.common.utils.Constants.PRE_AUTHENTICATION_INSTANCE;
import static ir.daneshrefah.scm.uaa.common.utils.ErrorUtils.throwError;

/**
 * Normally used on PWA/MB Authentication
 */
public class DefaultGrantAuthenticationConverter implements AuthenticationConverter {


    @Override
    public Authentication convert(HttpServletRequest request) {
        // grant_type (REQUIRED)
        String grantType = request.getParameter(OAuth2ParameterNames.GRANT_TYPE);
        if (!AuthorizationGrantType.DEFAULT.getCode().equals(grantType)) {
            return null;
        }
        Authentication clientPrincipal = SecurityContextHolder.getContext().getAuthentication();
        ParameterSearch parameters = new ParameterSearch(request);
        String username = parameters.getFirst(OAuth2ParameterNames.USERNAME).orElseGet(() -> {
            throwError(Constants.OAUTH2_ERROR_CODE_INVALID_USER, Constants.OAUTH2_PARAM_NAME_USER_USERNAME);
            return null;
        });
        String password = parameters.getFirst(OAuth2ParameterNames.PASSWORD)
                .map(SecurityUtils.getInstance()::decryptPassword)
                .orElse(null);
        request.getParameter(Constants.CORRELATION_ID_HEADER);
        request.getParameter(Constants.UUID_HEADER);
        return createPreAuthentication(username, password, clientPrincipal, parameters, request);
    }

    private Authentication createPreAuthentication(String username, String password, Authentication clientPrincipal, ParameterSearch parameters, HttpServletRequest request) {
        String appVersion = getClientId(clientPrincipal, parameters.getFirst(Constants.APP_VERSION_HEADER).orElse(null));
        String clientId = null;
        if (StringUtils.startsWithIgnoreCase(appVersion, "MB")) {
            clientId = "MB";
        }
        if (StringUtils.startsWithIgnoreCase(appVersion, "SA")) {
            clientId = "SA";
        }
        if (clientId == null) {
            clientId = "PWA";
        }
        HashSet<String> scopes = geScopes(parameters);
        PreAuthenticationToken preAuthenticationToken = new PreAuthenticationToken(username, password,
                AuthorizationGrantType.DEFAULT,
                clientPrincipal, scopes, null);
        DefaultGrantPreAuthenticationToken defaultGrantPreAuthToken = new DefaultGrantPreAuthenticationToken();
        parameters.getFirst(Constants.CHANNEL_HEADER).ifPresent(defaultGrantPreAuthToken::setChannel);
        parameters.getFirst(Constants.APP_VERSION_HEADER).ifPresent(defaultGrantPreAuthToken::setAppVersion);
        parameters.getFirst(Constants.APP_VERSION_HEADER).ifPresent(preAuthenticationToken::setClientVersion);
        parameters.getFirst(Constants.SIGNATURE_HEADER).ifPresent(defaultGrantPreAuthToken::setSignature);
        parameters.getFirst(Constants.SIGNATURE_HEADER).ifPresent(preAuthenticationToken::setClientSignature);
        parameters.getFirst(Constants.ACCESS_PARAM_HEADER).ifPresent(preAuthenticationToken::setAccessParameter);
        parameters.getFirst(Constants.ACCESS_PARAM_HEADER).ifPresent(defaultGrantPreAuthToken::setAccessParam);
        parameters.getFirst(Constants.HASHCODE_HEADER).ifPresent(defaultGrantPreAuthToken::setHashcode);
        parameters.getFirst(Constants.AGENT_HEADER).ifPresent(defaultGrantPreAuthToken::setAgent);
        parameters.getFirst(Constants.REGISTRY_TOKEN_HEADER).ifPresent(defaultGrantPreAuthToken::setRegistryToken);
        parameters.getFirst(Constants.OPERATING_SYSTEM_VERSION_HEADER).ifPresent(defaultGrantPreAuthToken::setOperationSystemVersion);
        parameters.getFirst(Constants.DEVICE_MODEL_HEADER).ifPresent(defaultGrantPreAuthToken::setDeviceModel);
        parameters.getFirst(Constants.UUID_HEADER).ifPresent(defaultGrantPreAuthToken::setUuid);
        preAuthenticationToken.setClientId(clientId);
        preAuthenticationToken.setRemoteAddress(request.getRemoteAddr());
        parameters.getFirst(Constants.PWA_OTP_CODE_HEADER).ifPresent(defaultGrantPreAuthToken::setOtpCode);
        parameters.getFirst(Constants.PWA_OTP_CODE_HEADER).ifPresent(preAuthenticationToken::setClaimCode);
        parameters.getFirst(Constants.PWA_TERMINAL_TYPE_HEADER).ifPresent(defaultGrantPreAuthToken::setTerminalType);
        defaultGrantPreAuthToken.setIp(RequestUtils.getOrDefaultRequestIp(null,request));
        preAuthenticationToken.setDefaultGrantPreAuthToken(defaultGrantPreAuthToken);
        request.setAttribute(PRE_AUTHENTICATION_INSTANCE,preAuthenticationToken);
        return preAuthenticationToken;
    }

    private HashSet<String> geScopes(ParameterSearch parameters) {
        return parameters
                .getFirst(OAuth2ParameterNames.SCOPE)
                .filter(org.apache.commons.lang3.StringUtils::isNotBlank)
                .map(s -> new HashSet<>(Arrays.asList(StringUtils.delimitedListToStringArray(s, " "))))
                .orElse(null);
    }

    private String getClientId(Authentication clientPrincipal, String appVersion) {
        String clientId;
        if (null == clientPrincipal || clientPrincipal instanceof AnonymousAuthenticationToken) {
            clientId = org.apache.commons.lang3.StringUtils.upperCase(appVersion);
        } else {
            clientId = clientPrincipal.getName();
        }
        if (!StringUtils.hasText(clientId)) {
            throwError(OAuth2ErrorCodes.INVALID_CLIENT, OAuth2ParameterNames.CLIENT_ID);
        }
        return clientId;
    }


    private static class ParameterSearch {
        private final MultiValueMap<String, String> parameters;

        public ParameterSearch(HttpServletRequest request) {
            parameters = getParameters(request);
        }

        public Optional<List<String>> get(String parameterName) {
            return Optional.ofNullable(parameters.get(org.apache.commons.lang3.StringUtils.toRootLowerCase(parameterName)));
        }

        public Optional<String> getFirst(String parameterName) {
            return Optional.ofNullable(parameters.getFirst(org.apache.commons.lang3.StringUtils.toRootLowerCase(parameterName)));
        }

        private MultiValueMap<String, String> getParameters(HttpServletRequest request) {
            Map<String, String[]> parameterMap = request.getParameterMap();
            MultiValueMap<String, String> requestHeaders = RequestUtils.getRequestHeaders(request);
            MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>(parameterMap.size());
            parameters.putAll(requestHeaders);
            parameterMap.forEach((key, values) -> {
                for (String value : values) {
                    parameters.add(org.apache.commons.lang3.StringUtils.toRootLowerCase(key), value);
                }
            });
            return parameters;
        }
    }

}
