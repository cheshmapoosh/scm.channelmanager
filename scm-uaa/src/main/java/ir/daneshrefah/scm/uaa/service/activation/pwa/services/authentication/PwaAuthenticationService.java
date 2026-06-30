package ir.daneshrefah.scm.uaa.service.activation.pwa.services.authentication;

import ir.daneshrefah.scm.common.model.message.TokenType;
import ir.daneshrefah.scm.uaa.security.authentication.token.PreAuthenticationToken;
import ir.daneshrefah.scm.uaa.service.activation.pwa.services.authentication.model.PwaOAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;

public interface PwaAuthenticationService {

    void preAuthenticateCheck(PreAuthenticationToken token) throws OAuth2AuthenticationException;

    PwaOAuth2AccessToken postAuthenticate(PreAuthenticationToken token,
                                          TokenType tokenType,
                                          String jwt,
                                          int expireIn);

    PwaOAuth2AccessToken createTwoPhaseLoginResponse(PreAuthenticationToken token);

    void checkLoginTrails(PreAuthenticationToken preAuthenticationToken);
}
