package ir.daneshrefah.scm.uaa.security.oauth2.grant.shahkar.converter;

import ir.daneshrefah.scm.uaa.common.core.AuthorizationGrantType;
import ir.daneshrefah.scm.uaa.common.utils.Constants;
import ir.daneshrefah.scm.uaa.security.oauth2.grant.legacy.LegacyClientType;
import ir.daneshrefah.scm.uaa.security.oauth2.grant.legacy.client.LegacyAppVersion;
import ir.daneshrefah.scm.uaa.security.oauth2.grant.legacy.client.LegacyClientIdResolver;
import ir.daneshrefah.scm.uaa.security.oauth2.grant.legacy.client.LegacyClientTypeResolver;
import ir.daneshrefah.scm.uaa.security.oauth2.grant.legacy.converter.LegacyRequestParameters;
import ir.daneshrefah.scm.uaa.security.oauth2.grant.shahkar.ShahkarGrantAuthenticationToken;
import ir.daneshrefah.scm.uaa.security.oauth2.grant.shahkar.ShahkarGrantRequest;
import ir.daneshrefah.scm.uaa.security.oauth2.grant.shahkar.session.ShahkarRefreshTokenSessionService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static ir.daneshrefah.scm.uaa.common.utils.ErrorUtils.throwError;

@Component
@RequiredArgsConstructor
@SuppressWarnings("removal")
public class ShahkarGrantRequestMapper {
    private final LegacyClientIdResolver clientIdResolver;
    private final LegacyClientTypeResolver clientTypeResolver;
    private final ShahkarRefreshTokenSessionService refreshTokenSessionService;

    public ShahkarGrantAuthenticationToken map(
            HttpServletRequest request,
            LegacyRequestParameters parameters
    ) {
        String refreshToken = parameters.firstParameter(OAuth2ParameterNames.REFRESH_TOKEN)
                .filter(StringUtils::hasText)
                .orElse(null);
        if (refreshToken != null) {
            return refreshTokenSessionService.requireValid(refreshToken);
        }

        Authentication clientPrincipal = SecurityContextHolder.getContext().getAuthentication();
        LegacyAppVersion appVersion = new LegacyAppVersion(parameters.appVersion().orElse(null));
        String clientId = clientIdResolver.resolveShahkar(clientPrincipal, parameters, appVersion);
        rejectKnownNonSuperAppClient(clientId, appVersion);

        ShahkarGrantRequest grantRequest = new ShahkarGrantRequest(
                requiredSingle(request, parameters, OAuth2ParameterNames.USERNAME,
                        Constants.OAUTH2_PARAM_NAME_USER_USERNAME),
                mobileNumber(parameters),
                parameters.first(Constants.PWA_OTP_CODE_HEADER).filter(StringUtils::hasText).orElse(null),
                clientId,
                parameters.firstParameter(Constants.OAUTH2_PARAM_NAME_ACCESS_PARAMETER).orElse(null),
                parameters.firstParameter(Constants.OAUTH2_PARAM_NAME_CLIENT_VERSION).orElse(null),
                parameters.firstParameter(Constants.OAUTH2_PARAM_NAME_CLIENT_SIGNATURE).orElse(null),
                scopes(request, parameters),
                clientPrincipal,
                appVersion
        );
        return toToken(grantRequest);
    }

    private ShahkarGrantAuthenticationToken toToken(ShahkarGrantRequest request) {
        ShahkarGrantAuthenticationToken token = new ShahkarGrantAuthenticationToken(
                request.nationalCode(),
                request.mobileNumber(),
                request.mobileNumber(),
                request.scopes(),
                request.clientPrincipal(),
                request.appVersion().rawValue()
        );
        token.setActivationCode(request.otpCode());
        token.setAccessParameter(request.accessParameter());
        token.setClientVersion(request.clientVersion());
        token.setClientSignature(request.clientSignature());
        token.setClientId(request.clientId());
        return token;
    }

    private void rejectKnownNonSuperAppClient(String clientId, LegacyAppVersion appVersion) {
        LegacyClientType clientType = clientTypeResolver.resolve(
                null,
                clientId,
                appVersion,
                AuthorizationGrantType.SHAHKAR
        );
        if (clientType != null && !LegacyClientType.SA.equals(clientType)) {
            throwError(OAuth2ErrorCodes.INVALID_CLIENT, OAuth2ParameterNames.CLIENT_ID);
        }
    }

    private String requiredSingle(
            HttpServletRequest request,
            LegacyRequestParameters parameters,
            String name,
            String errorParameter
    ) {
        String[] values = request.getParameterValues(name);
        String value = parameters.firstParameter(name).orElse(null);
        if (values == null || values.length != 1 || !StringUtils.hasText(value)) {
            throwError(Constants.OAUTH2_ERROR_CODE_INVALID_USER, errorParameter);
        }
        return value.trim();
    }

    private String mobileNumber(LegacyRequestParameters parameters) {
        String mobileNumber = parameters.first(Constants.ACCESS_PARAM_HEADER)
                .or(() -> parameters.firstParameter(Constants.OAUTH2_PARAM_NAME_ACCESS_PARAMETER))
                .filter(StringUtils::hasText)
                .map(String::trim)
                .orElse(null);
        if (mobileNumber == null
                || !ir.daneshrefah.scm.utils.string.StringUtils.isValidPhoneNumber(mobileNumber)) {
            throwError(Constants.OAUTH2_ERROR_CODE_INVALID_USER, Constants.OAUTH2_PARAM_NAME_MOBILE_NUMBER);
        }
        return mobileNumber;
    }

    private Set<String> scopes(HttpServletRequest request, LegacyRequestParameters parameters) {
        String[] values = request.getParameterValues(OAuth2ParameterNames.SCOPE);
        String scope = parameters.firstParameter(OAuth2ParameterNames.SCOPE).orElse(null);
        if (values != null && values.length > 1) {
            throwError(OAuth2ErrorCodes.INVALID_REQUEST, OAuth2ParameterNames.SCOPE);
        }
        if (!StringUtils.hasText(scope)) {
            return Set.of();
        }
        return new HashSet<>(Arrays.asList(StringUtils.delimitedListToStringArray(scope, " ")));
    }
}
