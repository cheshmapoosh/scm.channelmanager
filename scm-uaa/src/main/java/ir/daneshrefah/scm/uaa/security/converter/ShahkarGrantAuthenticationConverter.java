package ir.daneshrefah.scm.uaa.security.converter;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import ir.daneshrefah.scm.common.constant.otp.OtpReason;
import ir.daneshrefah.scm.common.model.person.PersonType;
import ir.daneshrefah.scm.uaa.common.core.AuthorizationGrantType;
import ir.daneshrefah.scm.uaa.common.utils.Constants;
import ir.daneshrefah.scm.uaa.security.token.OAuth2ShahkarAuthenticationToken;
import ir.daneshrefah.scm.uaa.service.otp.dto.OtpSmsBasedNationalCodeRequest;
import ir.daneshrefah.scm.uaa.service.shahkar.ShahkarOwnershipService;
import ir.daneshrefah.scm.uaa.service.shahkar.domain.ShahkarStatus;
import ir.daneshrefah.scm.uaa.service.user.OtpUserService;
import ir.daneshrefah.scm.uaa.utils.CachedAccessToken;
import ir.daneshrefah.scm.utils.string.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.time.Instant;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static ir.daneshrefah.scm.uaa.common.utils.Constants.*;
import static ir.daneshrefah.scm.uaa.common.utils.ErrorUtils.throwError;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-05-26
 */
@Slf4j
public class ShahkarGrantAuthenticationConverter implements AuthenticationConverter {

    private String tokenCacheMap = "scm-uaa:refresh:token-cache";
    private final OtpUserService otpUserService;
    private final ShahkarOwnershipService shahkarOwnershipService;
    private final HazelcastInstance hazelcast;

    private final Long sessionTTL;

    public ShahkarGrantAuthenticationConverter(OtpUserService otpUserService,
                                               ShahkarOwnershipService shahkarOwnershipService,HazelcastInstance  hazelcast,Long sessionTTL) {
        this.otpUserService = otpUserService;
        this.shahkarOwnershipService = shahkarOwnershipService;
        this.hazelcast = hazelcast;
        this.sessionTTL =sessionTTL; //is set from yml(default)
    }

    @Override
    public Authentication convert(HttpServletRequest request) {
        // grant_type (REQUIRED)
        String grantType = request.getParameter(OAuth2ParameterNames.GRANT_TYPE);
        if (!AuthorizationGrantType.SHAHKAR.getCode().equals(grantType)) {
            return null;
        }

        // refresh-token
        if(StringUtils.isNotEmpty(request.getParameter("refresh_token"))) {
        IMap<String, CachedAccessToken<OAuth2ShahkarAuthenticationToken>> map = hazelcast.getMap(tokenCacheMap);
        CachedAccessToken<OAuth2ShahkarAuthenticationToken> cachedShahkarToken = map.get(request.getParameter("refresh_token"));
        OAuth2ShahkarAuthenticationToken authenticationToken = cachedShahkarToken.token();
        Instant now = Instant.now();
        if(cachedShahkarToken.isExpired(System.currentTimeMillis()) ||
                 now.isAfter(authenticationToken.getLastUsedAt().plusSeconds(900)) ||
                now.isAfter(authenticationToken.getCreatedAt().plusMillis(sessionTTL))){
            authenticationToken.setAuthenticated(false);
            throwError(Constants.OAUTH2_ERROR_CODE_IS_EXPIRED, Constants.OAUTH2_ERROR_CODE_IS_EXPIRED);
        }
        else {
            authenticationToken.setLastUsedAt(now);
            request.setAttribute(PRE_AUTHENTICATION_INSTANCE, authenticationToken);
            return authenticationToken;

         }
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
        String password =request.getHeader(ACCESS_PARAM_HEADER);
        if (StringUtils.isBlank(password) ) {
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
            clientId = getClientId(request);
        } else {
            clientId = clientPrincipal.getName();
        }
        if (StringUtils.isBlank(clientId)) {
            throwError(OAuth2ErrorCodes.INVALID_CLIENT, OAuth2ParameterNames.CLIENT_ID);
        }
        String claimCode = request.getParameter(Constants.PWA_OTP_CODE_HEADER);
        if (StringUtils.isEmpty(claimCode)) {
            //throwError(Constants.OAUTH2_ERROR_CODE_REQUIRED_CLAIM, Constants.OAUTH2_PARAM_NAME_USER_CLAIM);
            ShahkarStatus shahkarStatus =  shahkarOwnershipService.checkOwnership(username,password);
            if (!ShahkarStatus.OWNED.equals(shahkarStatus )){
                log.error("Shahkar error with status '{}'. national code {}, mobile number {}",
                        shahkarStatus.name(), mask(username), StringUtils.maskPhoneNumber(password));
                throwError(Constants.OAUTH2_ERROR_CODE_INVALID_USER, Constants.SHAHKAR_ERROR);
            }
            OtpSmsBasedNationalCodeRequest otpSmsBasedNationalCodeRequest = new OtpSmsBasedNationalCodeRequest(username, PersonType.REAL,null);
            otpSmsBasedNationalCodeRequest.setReason(OtpReason.SHAHKAR_AUTHENTICATION);
            otpUserService.sendOtpSmsShahkar(otpSmsBasedNationalCodeRequest,"MB",password);
            request.setAttribute("Verify",false);
        }
        else {
            request.setAttribute("Verify",true);
        }

//        username: nationalCode
//        password: mobileNo
//        claimCode: otp claimCode
        OAuth2ShahkarAuthenticationToken authenticationToken = new OAuth2ShahkarAuthenticationToken(username, password,
                password, scopes, clientPrincipal);

        authenticationToken.setAccessParameter(request.getParameter(Constants.OAUTH2_PARAM_NAME_ACCESS_PARAMETER));
        authenticationToken.setClientVersion(request.getParameter(Constants.OAUTH2_PARAM_NAME_CLIENT_VERSION));
        authenticationToken.setClientSignature(request.getParameter(Constants.OAUTH2_PARAM_NAME_CLIENT_SIGNATURE));
        authenticationToken.setActivationCode(request.getParameter(Constants.PWA_OTP_CODE_HEADER) != null ? request.getParameter(Constants.PWA_OTP_CODE_HEADER) :"-" );
        authenticationToken.setClientId("MB");
        authenticationToken.setLastUsedAt(Instant.now());


        request.setAttribute(PRE_AUTHENTICATION_INSTANCE, authenticationToken);

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


    private String getClientId(HttpServletRequest request) {
        String appVersion = request.getHeader(APP_VERSION_HEADER);
        String clientId = null;
        if (org.springframework.util.StringUtils.startsWithIgnoreCase(appVersion, "MB")) {
            clientId = "MB";
        }
        if (org.springframework.util.StringUtils.startsWithIgnoreCase(appVersion, "SA")) {
            clientId = "SA";
        }
        if (clientId == null || "PWA".equals(appVersion)) {
            clientId = "PWA";
        }
        return clientId;
    }

    private String mask(String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        String text = value.trim();
        if (text.length() <= 4) {
            return "****";
        }
        return text.substring(0, 2) + "***" + text.substring(text.length() - 2);
    }

}
