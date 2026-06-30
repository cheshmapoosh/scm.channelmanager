package ir.daneshrefah.scm.uaa.security.oauth2.grant.shahkar;

import ir.daneshrefah.scm.uaa.common.core.AuthorizationGrantType;
import ir.daneshrefah.scm.uaa.common.utils.Constants;
import ir.daneshrefah.scm.uaa.security.oauth2.grant.legacy.converter.LegacyRequestParameters;
import ir.daneshrefah.scm.uaa.security.oauth2.grant.shahkar.converter.ShahkarGrantRequestMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@SuppressWarnings("removal")
public class ShahkarGrantAuthenticationConverter implements AuthenticationConverter {
    private final ShahkarGrantRequestMapper requestMapper;

    @Override
    public Authentication convert(HttpServletRequest request) {
        LegacyRequestParameters parameters = new LegacyRequestParameters(request);
        String grantType = parameters.firstParameter(OAuth2ParameterNames.GRANT_TYPE).orElse(null);
        if (!AuthorizationGrantType.SHAHKAR.getCode().equals(grantType)) {
            return null;
        }
        ShahkarGrantAuthenticationToken token = requestMapper.map(request, parameters);
        request.setAttribute(Constants.PRE_AUTHENTICATION_INSTANCE, token);
        return token;
    }
}
