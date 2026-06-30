package ir.daneshrefah.scm.uaa.security.oauth2.grant.smsotp;

import ir.daneshrefah.scm.uaa.common.core.AuthorizationGrantType;
import ir.daneshrefah.scm.uaa.common.utils.Constants;
import ir.daneshrefah.scm.utils.string.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static ir.daneshrefah.scm.uaa.common.utils.ErrorUtils.throwError;

public class SmsOtpGrantAuthenticationConverter implements AuthenticationConverter {
    @Override
    public Authentication convert(HttpServletRequest request) {
        String grantType = request.getParameter(OAuth2ParameterNames.GRANT_TYPE);
        if (!AuthorizationGrantType.SMS_OTP.getCode().equals(grantType)) {
            return null;
        }

        Authentication clientPrincipal = SecurityContextHolder.getContext().getAuthentication();
        MultiValueMap<String, String> parameters = getParameters(request);

        String mobileNumber = parameters.getFirst(OAuth2ParameterNames.USERNAME);
        if (StringUtils.isBlank(mobileNumber) || parameters.get(OAuth2ParameterNames.USERNAME).size() != 1) {
            throwError(Constants.OAUTH2_ERROR_CODE_INVALID_USER, Constants.OAUTH2_PARAM_NAME_USER_USERNAME);
        }

        String claimCode = parameters.getFirst(OAuth2ParameterNames.PASSWORD);
        if (StringUtils.isBlank(claimCode) || parameters.get(OAuth2ParameterNames.PASSWORD).size() != 1) {
            throwError(Constants.OAUTH2_ERROR_CODE_INVALID_USER, Constants.OAUTH2_PARAM_NAME_USER_PASSWORD);
        }

        Set<String> scopes = scopes(parameters);
        String clientId = clientId(clientPrincipal, request);
        SmsOtpGrantAuthenticationToken authenticationToken = new SmsOtpGrantAuthenticationToken(
                mobileNumber,
                claimCode,
                scopes,
                clientPrincipal
        );
        authenticationToken.setAccessParameter(request.getParameter(Constants.OAUTH2_PARAM_NAME_ACCESS_PARAMETER));
        authenticationToken.setClientVersion(request.getParameter(Constants.OAUTH2_PARAM_NAME_CLIENT_VERSION));
        authenticationToken.setClientSignature(request.getParameter(Constants.OAUTH2_PARAM_NAME_CLIENT_SIGNATURE));
        authenticationToken.setActivationCode(request.getParameter(Constants.OAUTH2_PARAM_NAME_USER_REGISTER_CODE));
        authenticationToken.setClientId(clientId);
        return authenticationToken;
    }

    private Set<String> scopes(MultiValueMap<String, String> parameters) {
        String scope = parameters.getFirst(OAuth2ParameterNames.SCOPE);
        if (StringUtils.isNotBlank(scope) && parameters.get(OAuth2ParameterNames.SCOPE).size() != 1) {
            throwError(OAuth2ErrorCodes.INVALID_REQUEST, OAuth2ParameterNames.SCOPE);
        }
        if (StringUtils.isBlank(scope)) {
            return null;
        }
        return new HashSet<>(Arrays.asList(StringUtils.delimitedListToStringArray(scope, " ")));
    }

    private String clientId(Authentication clientPrincipal, HttpServletRequest request) {
        String clientId;
        if (clientPrincipal == null || clientPrincipal instanceof AnonymousAuthenticationToken) {
            clientId = request.getParameter(OAuth2ParameterNames.CLIENT_ID);
        } else {
            clientId = clientPrincipal.getName();
        }
        if (StringUtils.isBlank(clientId)) {
            throwError(OAuth2ErrorCodes.INVALID_CLIENT, OAuth2ParameterNames.CLIENT_ID);
        }
        return clientId;
    }

    private static MultiValueMap<String, String> getParameters(HttpServletRequest request) {
        Map<String, String[]> parameterMap = request.getParameterMap();
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>(parameterMap.size());
        parameterMap.forEach((key, values) -> {
            if (values.length > 0) {
                for (String value : values) {
                    parameters.add(key, value);
                }
            }
        });
        return parameters;
    }
}
