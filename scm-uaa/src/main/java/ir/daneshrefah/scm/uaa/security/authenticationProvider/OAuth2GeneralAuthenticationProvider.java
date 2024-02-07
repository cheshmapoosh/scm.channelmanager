package ir.daneshrefah.scm.uaa.security.authenticationProvider;


import ir.daneshrefah.scm.uaa.common.utils.ErrorUtils;
import ir.daneshrefah.scm.uaa.security.token.GeneralAuthenticationToken;
import ir.daneshrefah.scm.uaa.security.token.PreAuthenticationToken;
import ir.daneshrefah.scm.uaa.security.token.generator.AuthenticationResponseTokenGenerator;
import ir.daneshrefah.scm.uaa.security.token.generator.OAuth2AuthenticationRequestTokenGenerator;
import ir.daneshrefah.scm.uaa.security.userDetails.UserDetailsService;
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
                                               UserDetailsService userDetailsService,
                                               OAuth2AuthenticationRequestTokenGenerator authenticationTokenGenerator,
                                               DelegatorAuthenticationProvider delegatorAuthenticationProvider,
                                               AuthenticationResponseTokenGenerator responseTokenGenerator) {
        super(clientRepository, userCache, userDetailsService, authenticationTokenGenerator, delegatorAuthenticationProvider);
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

        return responseTokenGenerator.getAccessToken(requestAuthentication, preAuthenticationToken.getClientPrincipal(),
                preAuthenticationToken.getRegisteredClient(), authentication);
    }

    @Override
    protected void throwError(String errorCode, String parameterName) {
        ErrorUtils.throwError(errorCode, parameterName);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return PreAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
