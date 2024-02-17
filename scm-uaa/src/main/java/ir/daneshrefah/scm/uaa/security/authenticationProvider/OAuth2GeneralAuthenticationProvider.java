package ir.daneshrefah.scm.uaa.security.authenticationProvider;


import ir.daneshrefah.scm.uaa.common.exception.TwoStepAuthenticationRequiredException;
import ir.daneshrefah.scm.uaa.common.security.authenticationDetails.TerminalUserDetails;
import ir.daneshrefah.scm.uaa.common.utils.Constants;
import ir.daneshrefah.scm.uaa.common.utils.ErrorUtils;
import ir.daneshrefah.scm.uaa.security.token.AbstractAuthenticationToken;
import ir.daneshrefah.scm.uaa.security.token.GeneralAuthenticationToken;
import ir.daneshrefah.scm.uaa.security.token.PostAuthenticationToken;
import ir.daneshrefah.scm.uaa.security.token.PreAuthenticationToken;
import ir.daneshrefah.scm.uaa.security.token.generator.AuthenticationResponseTokenGenerator;
import ir.daneshrefah.scm.uaa.security.token.generator.OAuth2AuthenticationRequestTokenGenerator;
import ir.daneshrefah.scm.uaa.security.userDetails.UserDetailsService;
import ir.daneshrefah.scm.uaa.service.ClientService;
import ir.daneshrefah.scm.utils.string.StringUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserCache;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.stereotype.Component;


/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-18
 */
@Component
public class OAuth2GeneralAuthenticationProvider extends BaseGeneralAuthenticationProvider {

    private final AuthenticationResponseTokenGenerator responseTokenGenerator;

    public OAuth2GeneralAuthenticationProvider(RegisteredClientRepository clientRepository, UserCache userCache,
                                               ClientService clientService,
                                               UserDetailsService userDetailsService,
                                               OAuth2AuthenticationRequestTokenGenerator authenticationTokenGenerator,
                                               DelegatorAuthenticationProvider delegatorAuthenticationProvider,
                                               AuthenticationResponseTokenGenerator responseTokenGenerator) {
        super(clientRepository, clientService, userCache, userDetailsService, authenticationTokenGenerator, delegatorAuthenticationProvider);
        this.responseTokenGenerator = responseTokenGenerator;
    }

    @Override
    protected PreAuthenticationToken extractPreAuthenticationToken(Authentication authentication) {
        PreAuthenticationToken preAuthenticationToken = (PreAuthenticationToken) authentication;
        Authentication clientPrincipal = preAuthenticationToken.getClientPrincipal();
        if (null != clientPrincipal && clientPrincipal instanceof OAuth2ClientAuthenticationToken) {
            preAuthenticationToken.setRegisteredClient(((OAuth2ClientAuthenticationToken) clientPrincipal).getRegisteredClient());
        }
        return (PreAuthenticationToken) authentication;
    }

    @Override
    protected Authentication buildResponse(Authentication requestAuthentication,
                                           PreAuthenticationToken preAuthenticationToken, GeneralAuthenticationToken authentication) {
        PostAuthenticationToken.AuthenticationStatus status = ((PostAuthenticationToken) authentication).getAuthenticationStatus();
        if (PostAuthenticationToken.AuthenticationStatus.INCOMPLETE.equals(status)) {
            throwError(authentication, new TwoStepAuthenticationRequiredException(authentication));
        }

        return responseTokenGenerator.getAccessToken(requestAuthentication, preAuthenticationToken.getClientPrincipal(),
                preAuthenticationToken.getRegisteredClient(), authentication);
    }

    @Override
    protected void throwError(Authentication authentication, Exception exception) {
        String loginAuthenticationMethod = authentication instanceof PostAuthenticationToken ?
                ((TerminalUserDetails) authentication.getPrincipal()).getUser().getLoginAuthenticationMethod().getCode() : null;
        String parameterName = extractParameterName(exception);
        String errorCode = StringUtils.isNotEmpty(loginAuthenticationMethod) ? parameterName + ":" + loginAuthenticationMethod : parameterName;
        ErrorUtils.throwError(errorCode, parameterName);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return PreAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
