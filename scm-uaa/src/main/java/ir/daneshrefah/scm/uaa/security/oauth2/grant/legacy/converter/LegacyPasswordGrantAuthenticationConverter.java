package ir.daneshrefah.scm.uaa.security.oauth2.grant.legacy.converter;

import ir.daneshrefah.scm.uaa.common.core.AuthorizationGrantType;
import ir.daneshrefah.scm.uaa.security.oauth2.grant.legacy.LegacyPasswordGrantAuthenticationToken;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationConverter;

import static ir.daneshrefah.scm.uaa.common.utils.Constants.PRE_AUTHENTICATION_INSTANCE;

/**
 * Legacy password converter kept only for old NIB/PWA/MB compatibility.
 * Remove this class after migration to UAA-hosted login and authorization-code flow is complete.
 * No new feature should be added here unless strictly required for migration safety.
 */
@Deprecated(since = "9.0.0", forRemoval = true)
@SuppressWarnings("removal")
public class LegacyPasswordGrantAuthenticationConverter implements AuthenticationConverter {
    private final LegacyPasswordGrantRequestMapper requestMapper;
    private final LegacyDefaultGrantRequestMapper defaultGrantRequestMapper;

    public LegacyPasswordGrantAuthenticationConverter(LegacyPasswordGrantRequestMapper requestMapper) {
        this.requestMapper = requestMapper;
        this.defaultGrantRequestMapper = new LegacyDefaultGrantRequestMapper();
    }

    @Override
    public Authentication convert(HttpServletRequest request) {
        AuthorizationGrantType grantType = requestMapper.grantType(request);
        if (!AuthorizationGrantType.FIRST_PASSWORD.equals(grantType) && !AuthorizationGrantType.DEFAULT.equals(grantType)) {
            return null;
        }
        LegacyPasswordGrantRequestMapper.ParameterSearch parameters =
                new LegacyPasswordGrantRequestMapper.ParameterSearch(
                        request,
                        AuthorizationGrantType.DEFAULT.equals(grantType)
                );
        LegacyPasswordGrantAuthenticationToken token = new LegacyPasswordGrantAuthenticationToken(
                requestMapper.map(request, parameters, grantType)
        );
        defaultGrantRequestMapper.applyCommonFields(token, request, parameters);
        if (AuthorizationGrantType.DEFAULT.equals(grantType)) {
            defaultGrantRequestMapper.applyDefaultGrantFields(token, request, parameters);
        }
        request.setAttribute(PRE_AUTHENTICATION_INSTANCE, token);
        return token;
    }
}
