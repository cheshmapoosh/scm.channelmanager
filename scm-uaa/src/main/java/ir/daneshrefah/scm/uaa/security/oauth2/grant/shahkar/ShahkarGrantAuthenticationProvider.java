package ir.daneshrefah.scm.uaa.security.oauth2.grant.shahkar;

import ir.daneshrefah.scm.uaa.common.core.AuthorizationGrantType;
import ir.daneshrefah.scm.uaa.security.oauth2.grant.OAuth2TokenGrantAuthenticationProvider;
import ir.daneshrefah.scm.uaa.security.oauth2.grant.shahkar.flow.ShahkarAuthenticationFlowService;
import ir.daneshrefah.scm.uaa.security.oauth2.grant.shahkar.session.ShahkarRefreshTokenSessionService;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
import org.springframework.stereotype.Component;

@Component
public class ShahkarGrantAuthenticationProvider extends OAuth2TokenGrantAuthenticationProvider<ShahkarGrantAuthenticationToken> {
    private final ShahkarAuthenticationFlowService authenticationFlowService;
    private final ShahkarRefreshTokenSessionService refreshTokenSessionService;

    public ShahkarGrantAuthenticationProvider(
            RegisteredClientRepository registeredClientRepository,
            OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator,
            ShahkarAuthenticationFlowService authenticationFlowService,
            ShahkarRefreshTokenSessionService refreshTokenSessionService
    ) {
        super(registeredClientRepository, tokenGenerator);
        this.authenticationFlowService = authenticationFlowService;
        this.refreshTokenSessionService = refreshTokenSessionService;
    }

    @Override
    protected AuthorizationGrantType grantType() {
        return AuthorizationGrantType.SHAHKAR;
    }

    @Override
    protected ShahkarGrantAuthenticationToken authenticateToken(ShahkarGrantAuthenticationToken authenticationToken) {
        return authenticationFlowService.authenticate(authenticationToken);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return ShahkarGrantAuthenticationToken.class.isAssignableFrom(authentication);
    }

    @Override
    protected void onTokensGenerated(
            ShahkarGrantAuthenticationToken authenticationToken,
            OAuth2AccessToken accessToken,
            OAuth2RefreshToken refreshToken
    ) {
        if (refreshToken != null) {
            refreshTokenSessionService.store(refreshToken.getTokenValue(), authenticationToken);
        }
    }
}
