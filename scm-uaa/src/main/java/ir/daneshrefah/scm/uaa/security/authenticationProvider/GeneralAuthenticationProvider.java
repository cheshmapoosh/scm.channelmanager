package ir.daneshrefah.scm.uaa.security.authenticationProvider;

import ir.daneshrefah.scm.uaa.common.core.AuthorizationGrantType;
import ir.daneshrefah.scm.uaa.common.exception.TwoStepAuthenticationRequiredException;
import ir.daneshrefah.scm.uaa.common.security.authenticationDetails.TerminalWebAuthenticationDetails;
import ir.daneshrefah.scm.uaa.exception.UnknownAuthenticationException;
import ir.daneshrefah.scm.uaa.security.authentication.token.AuthenticationOutcomeToken;
import ir.daneshrefah.scm.uaa.security.authentication.token.PreAuthenticationToken;
import ir.daneshrefah.scm.uaa.security.authentication.token.UserLoginAuthenticationToken;
import ir.daneshrefah.scm.uaa.security.oauth2.token.OAuth2AuthenticationRequestTokenGenerator;
import ir.daneshrefah.scm.uaa.security.userDetails.UserDetailsService;
import ir.daneshrefah.scm.uaa.service.activation.nib.UserActivationAuthenticationService;
import ir.daneshrefah.scm.uaa.service.activation.pwa.services.authentication.PwaAuthenticationService;
import ir.daneshrefah.scm.uaa.service.client.ClientService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserCache;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-18
 */

@Slf4j
@Component
public class GeneralAuthenticationProvider extends BaseGeneralAuthenticationProvider {

    public GeneralAuthenticationProvider(RegisteredClientRepository clientRepository, UserCache userCache,
                                         ClientService clientService,
                                         UserDetailsService userDetailsService,
                                         OAuth2AuthenticationRequestTokenGenerator authenticationTokenGenerator,
                                         AuthenticationManager authenticationManager,
                                         UserActivationAuthenticationService userActivationAuthenticationService,
                                         PwaAuthenticationService pwaAuthenticationService) {

        super(clientRepository,
                clientService,
                userCache,
                userDetailsService,
                authenticationTokenGenerator,
                authenticationManager,
                userActivationAuthenticationService,
                pwaAuthenticationService);
    }

    @Override
    protected PreAuthenticationToken extractPreAuthenticationToken(Authentication authentication) {
        String clientId = null;
        TerminalWebAuthenticationDetails.Claim claim = null;
        if (null != authentication.getDetails() && authentication.getDetails() instanceof TerminalWebAuthenticationDetails) {
            clientId = ((TerminalWebAuthenticationDetails) authentication.getDetails()).getClientId();
            claim = ((TerminalWebAuthenticationDetails) authentication.getDetails()).getClaim();
        }
        String username = (String) authentication.getPrincipal();
        if (null != claim) {
            username = claim.username();
        }
        String password = (String) authentication.getCredentials();
        PreAuthenticationToken preAuthenticationToken = new PreAuthenticationToken(username, password,
                AuthorizationGrantType.FIRST_PASSWORD,
                null, Set.of("session"), authentication.getDetails()); //TODO scopes mus be set from request
        preAuthenticationToken.setClientId(clientId);
//        preAuthenticationToken.setAccessParameter(request.getParameter(Constants.OAUTH2_PARAM_NAME_ACCESS_PARAMETER));
        preAuthenticationToken.setClaimCode(null != claim ? claim.claimCode() : null);
//        preAuthenticationToken.setClientVersion(request.getParameter(Constants.OAUTH2_PARAM_NAME_CLIENT_VERSION));
//        preAuthenticationToken.setClientSignature(request.getParameter(Constants.OAUTH2_PARAM_NAME_CLIENT_SIGNATURE));
//        preAuthenticationToken.setActivationCode(request.getParameter(Constants.OAUTH2_PARAM_NAME_USER_REGISTER_CODE));

        return preAuthenticationToken;
    }

    @Override
    protected Authentication buildResponse(Authentication requestAuthentication, PreAuthenticationToken preAuthenticationToken, UserLoginAuthenticationToken authentication) {
        AuthenticationOutcomeToken.AuthenticationStatus status = ((AuthenticationOutcomeToken) authentication).getAuthenticationStatus();
        if (AuthenticationOutcomeToken.AuthenticationStatus.INCOMPLETE.equals(status)) {
            throw new TwoStepAuthenticationRequiredException(authentication);
        }
        return authentication;
    }

    @Override
    protected void throwError(Authentication errorCode, Exception exception) throws AuthenticationException {
        if (!(exception instanceof AuthenticationException)) {
            log.error("authentication invalid error.", exception);
            exception = new UnknownAuthenticationException(exception);
        }
        throw (AuthenticationException) exception;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return (UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication));
    }

}
