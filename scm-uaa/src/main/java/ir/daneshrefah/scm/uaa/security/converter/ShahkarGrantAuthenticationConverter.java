package ir.daneshrefah.scm.uaa.security.converter;

import ir.daneshrefah.scm.uaa.common.core.AuthorizationGrantType;
import ir.daneshrefah.scm.uaa.common.utils.Constants;
import ir.daneshrefah.scm.uaa.security.token.OAuth2ShahkarAuthenticationToken;
import ir.daneshrefah.scm.uaa.security.token.PreAuthenticationToken;
import ir.daneshrefah.scm.utils.string.StringUtils;
import ir.daneshrefah.scm.utils.validation.ValidationUtils;
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

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-05-26
 */
public class ShahkarGrantAuthenticationConverter implements AuthenticationConverter {


    @Override
    public Authentication convert(HttpServletRequest request) {
        // grant_type (REQUIRED)
        String grantType = request.getParameter(OAuth2ParameterNames.GRANT_TYPE);
        if (!AuthorizationGrantType.SHAHKAR.getCode().equals(grantType)) {
            return null;
        }

        Authentication clientPrincipal = SecurityContextHolder.getContext().getAuthentication();
        MultiValueMap<String, String> parameters = getParameters(request);

        /* username must be national code of user */
        String username = parameters.getFirst(OAuth2ParameterNames.USERNAME);
        if (StringUtils.isBlank(username) ||
                parameters.get(OAuth2ParameterNames.USERNAME).size() != 1) {
            throwError(Constants.OAUTH2_ERROR_CODE_INVALID_USER, Constants.OAUTH2_PARAM_NAME_USER_USERNAME);
        }

        /* password must be phone number of user */
        String password = parameters.getFirst(OAuth2ParameterNames.PASSWORD);
        if (StringUtils.isBlank(password) ||
                parameters.get(OAuth2ParameterNames.PASSWORD).size() != 1) {
            throwError(Constants.OAUTH2_ERROR_CODE_INVALID_USER, Constants.OAUTH2_PARAM_NAME_USER_PASSWORD);
        }
        if (!StringUtils.isValidPhoneNumber(password)) {
            throwError(Constants.OAUTH2_ERROR_CODE_INVALID_USER, Constants.OAUTH2_PARAM_NAME_USER_PASSWORD);
        }

        // scope (OPTIONAL)
        Set<String> scopes = null;
        String scope = parameters.getFirst(OAuth2ParameterNames.SCOPE);
        if (StringUtils.isNotBlank(scope) &&
                parameters.get(OAuth2ParameterNames.SCOPE).size() != 1) {
            throwError(OAuth2ErrorCodes.INVALID_REQUEST, OAuth2ParameterNames.SCOPE);
        }
        if (StringUtils.isNotBlank(scope)) {
            scopes = new HashSet<>(
                    Arrays.asList(StringUtils.delimitedListToStringArray(scope, " ")));
        }

        String clientId = null;
        if (null == clientPrincipal || clientPrincipal instanceof AnonymousAuthenticationToken) {
            clientId = request.getParameter(OAuth2ParameterNames.CLIENT_ID);
        } else {
            clientId = clientPrincipal.getName();
        }
        if (StringUtils.isBlank(clientId)) {
            throwError(OAuth2ErrorCodes.INVALID_CLIENT, OAuth2ParameterNames.CLIENT_ID);
        }
        String claimCode = request.getParameter(Constants.OAUTH2_PARAM_NAME_USER_CLAIM);
        if (StringUtils.isEmpty(claimCode)) {
            throwError(Constants.OAUTH2_ERROR_CODE_REQUIRED_CLAIM, Constants.OAUTH2_PARAM_NAME_USER_CLAIM);
        }

//        username: nationalCode
//        password: mobileNo
//        claimCode: otp claimCode
        OAuth2ShahkarAuthenticationToken authenticationToken = new OAuth2ShahkarAuthenticationToken(username, password,
                claimCode, scopes, clientPrincipal);

        authenticationToken.setAccessParameter(request.getParameter(Constants.OAUTH2_PARAM_NAME_ACCESS_PARAMETER));
        authenticationToken.setClientVersion(request.getParameter(Constants.OAUTH2_PARAM_NAME_CLIENT_VERSION));
        authenticationToken.setClientSignature(request.getParameter(Constants.OAUTH2_PARAM_NAME_CLIENT_SIGNATURE));
        authenticationToken.setActivationCode(request.getParameter(Constants.OAUTH2_PARAM_NAME_USER_REGISTER_CODE));
        authenticationToken.setClientId(clientId);
        return authenticationToken;
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
