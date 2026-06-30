package ir.daneshrefah.scm.uaa.security.form;

import ir.daneshrefah.scm.uaa.common.core.AuthorizationGrantType;
import ir.daneshrefah.scm.uaa.common.exception.TwoStepAuthenticationRequiredException;
import ir.daneshrefah.scm.uaa.common.security.authenticationDetails.TerminalWebAuthenticationDetails;
import ir.daneshrefah.scm.uaa.exception.UnknownAuthenticationException;
import ir.daneshrefah.scm.uaa.security.authentication.UaaPasswordAuthenticationFlowService;
import ir.daneshrefah.scm.uaa.security.oauth2.error.OAuth2AuthenticationErrorMapper;
import ir.daneshrefah.scm.uaa.security.token.PostAuthenticationToken;
import ir.daneshrefah.scm.uaa.security.token.PreAuthenticationToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class UaaFormLoginAuthenticationProvider implements AuthenticationProvider {
    private final UaaPasswordAuthenticationFlowService authenticationService;
    private final OAuth2AuthenticationErrorMapper errorMapper;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        try {
            UaaPasswordAuthenticationFlowService.AuthenticationResult result =
                    authenticationService.authenticate(toPreAuthenticationToken(authentication));
            PostAuthenticationToken.AuthenticationStatus status =
                    ((PostAuthenticationToken) result.authentication()).getAuthenticationStatus();
            if (PostAuthenticationToken.AuthenticationStatus.INCOMPLETE.equals(status)) {
                throw new TwoStepAuthenticationRequiredException(result.authentication());
            }
            return result.authentication();
        } catch (AuthenticationException exception) {
            throw exception;
        } catch (Exception exception) {
            log.error("authentication invalid error: {}", errorMapper.safeMessage(exception));
            throw new UnknownAuthenticationException(exception);
        }
    }

    private PreAuthenticationToken toPreAuthenticationToken(Authentication authentication) {
        String clientId = null;
        TerminalWebAuthenticationDetails.Claim claim = null;
        if (authentication.getDetails() instanceof TerminalWebAuthenticationDetails details) {
            clientId = details.getClientId();
            claim = details.getClaim();
        }
        String username = (String) authentication.getPrincipal();
        if (claim != null) {
            username = claim.username();
        }
        String password = (String) authentication.getCredentials();
        PreAuthenticationToken token = new PreAuthenticationToken(
                username,
                password,
                AuthorizationGrantType.FIRST_PASSWORD,
                null,
                Set.of("session"),
                authentication.getDetails()
        );
        token.setClientId(clientId);
        token.setClaimCode(claim != null ? claim.claimCode() : null);
        return token;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
