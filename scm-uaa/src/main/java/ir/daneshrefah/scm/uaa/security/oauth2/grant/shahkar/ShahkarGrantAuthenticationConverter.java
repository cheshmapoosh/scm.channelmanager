package ir.daneshrefah.scm.uaa.security.oauth2.grant.shahkar;

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

import static ir.daneshrefah.scm.uaa.common.utils.Constants.ACCESS_PARAM_HEADER;
import static ir.daneshrefah.scm.uaa.common.utils.Constants.APP_VERSION_HEADER;
import static ir.daneshrefah.scm.uaa.common.utils.Constants.PRE_AUTHENTICATION_INSTANCE;
import static ir.daneshrefah.scm.uaa.common.utils.ErrorUtils.throwError;

@Slf4j
public class ShahkarGrantAuthenticationConverter implements AuthenticationConverter {
    private static final String TOKEN_CACHE_MAP = "scm-uaa:refresh:token-cache";

    private final OtpUserService otpUserService;
    private final ShahkarOwnershipService shahkarOwnershipService;
    private final HazelcastInstance hazelcast;
    private final Long sessionTtl;

    public ShahkarGrantAuthenticationConverter(
            OtpUserService otpUserService,
            ShahkarOwnershipService shahkarOwnershipService,
            HazelcastInstance hazelcast,
            Long sessionTtl
    ) {
        this.otpUserService = otpUserService;
        this.shahkarOwnershipService = shahkarOwnershipService;
        this.hazelcast = hazelcast;
        this.sessionTtl = sessionTtl;
    }

    @Override
    public Authentication convert(HttpServletRequest request) {
        String grantType = request.getParameter(OAuth2ParameterNames.GRANT_TYPE);
        if (!AuthorizationGrantType.SHAHKAR.getCode().equals(grantType)) {
            return null;
        }

        if (StringUtils.isNotEmpty(request.getParameter("refresh_token"))) {
            IMap<String, CachedAccessToken<OAuth2ShahkarAuthenticationToken>> map = hazelcast.getMap(TOKEN_CACHE_MAP);
            CachedAccessToken<OAuth2ShahkarAuthenticationToken> cachedShahkarToken = map.get(request.getParameter("refresh_token"));
            if (cachedShahkarToken == null) {
                throwError(Constants.OAUTH2_ERROR_CODE_IS_EXPIRED, Constants.OAUTH2_ERROR_CODE_IS_EXPIRED);
            }
            OAuth2ShahkarAuthenticationToken authenticationToken = cachedShahkarToken.token();
            Instant now = Instant.now();
            if (cachedShahkarToken.isExpired(System.currentTimeMillis())
                    || now.isAfter(authenticationToken.getLastUsedAt().plusSeconds(900))
                    || now.isAfter(authenticationToken.getCreatedAt().plusMillis(sessionTtl))) {
                authenticationToken.setAuthenticated(false);
                throwError(Constants.OAUTH2_ERROR_CODE_IS_EXPIRED, Constants.OAUTH2_ERROR_CODE_IS_EXPIRED);
            }
            authenticationToken.setLastUsedAt(now);
            request.setAttribute(PRE_AUTHENTICATION_INSTANCE, authenticationToken);
            return authenticationToken;
        }

        Authentication clientPrincipal = SecurityContextHolder.getContext().getAuthentication();
        MultiValueMap<String, String> parameters = getParameters(request);
        String nationalCode = parameters.getFirst(OAuth2ParameterNames.USERNAME);
        if (StringUtils.isBlank(nationalCode) || parameters.get(OAuth2ParameterNames.USERNAME).size() != 1) {
            throwError(Constants.OAUTH2_ERROR_CODE_INVALID_USER, Constants.OAUTH2_PARAM_NAME_USER_USERNAME);
        }

        String mobileNumber = request.getHeader(ACCESS_PARAM_HEADER);
        if (StringUtils.isBlank(mobileNumber) || !StringUtils.isValidPhoneNumber(mobileNumber)) {
            throwError(Constants.OAUTH2_ERROR_CODE_INVALID_USER, Constants.OAUTH2_PARAM_NAME_USER_PASSWORD);
        }

        Set<String> scopes = scopes(parameters);
        String clientId = clientId(clientPrincipal, request);
        String claimCode = request.getParameter(Constants.PWA_OTP_CODE_HEADER);
        if (StringUtils.isEmpty(claimCode)) {
            ShahkarStatus shahkarStatus = shahkarOwnershipService.checkOwnership(nationalCode, mobileNumber);
            if (!ShahkarStatus.OWNED.equals(shahkarStatus)) {
                log.error("Shahkar ownership check failed. status={}, nationalCode={}, mobileNumber={}",
                        shahkarStatus.name(), mask(nationalCode), StringUtils.maskPhoneNumber(mobileNumber));
                throwError(Constants.OAUTH2_ERROR_CODE_INVALID_USER, Constants.SHAHKAR_ERROR);
            }
            OtpSmsBasedNationalCodeRequest otpRequest = new OtpSmsBasedNationalCodeRequest(nationalCode, PersonType.REAL, null);
            otpRequest.setReason(OtpReason.SHAHKAR_AUTHENTICATION);
            otpUserService.sendOtpSmsShahkar(otpRequest, "MB", mobileNumber);
            request.setAttribute("Verify", false);
        } else {
            request.setAttribute("Verify", true);
        }

        ShahkarGrantAuthenticationToken authenticationToken = new ShahkarGrantAuthenticationToken(
                nationalCode,
                mobileNumber,
                mobileNumber,
                scopes,
                clientPrincipal
        );
        authenticationToken.setAccessParameter(request.getParameter(Constants.OAUTH2_PARAM_NAME_ACCESS_PARAMETER));
        authenticationToken.setClientVersion(request.getParameter(Constants.OAUTH2_PARAM_NAME_CLIENT_VERSION));
        authenticationToken.setClientSignature(request.getParameter(Constants.OAUTH2_PARAM_NAME_CLIENT_SIGNATURE));
        authenticationToken.setActivationCode(claimCode != null ? claimCode : "-");
        authenticationToken.setClientId(clientId);
        authenticationToken.setLastUsedAt(Instant.now());
        request.setAttribute(PRE_AUTHENTICATION_INSTANCE, authenticationToken);
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
        return new HashSet<>(Arrays.asList(org.springframework.util.StringUtils.delimitedListToStringArray(scope, " ")));
    }

    private String clientId(Authentication clientPrincipal, HttpServletRequest request) {
        String clientId;
        if (clientPrincipal == null || clientPrincipal instanceof AnonymousAuthenticationToken) {
            clientId = getClientId(request);
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

    private String getClientId(HttpServletRequest request) {
        String appVersion = request.getHeader(APP_VERSION_HEADER);
        if (org.springframework.util.StringUtils.startsWithIgnoreCase(appVersion, "MB")) {
            return "MB";
        }
        if (org.springframework.util.StringUtils.startsWithIgnoreCase(appVersion, "SA")) {
            return "SA";
        }
        return "PWA";
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
