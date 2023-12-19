package ir.daneshrefah.scm.uaa.security.authenticationProvider;

import ir.daneshrefah.scm.uaa.security.authenticationDetails.TerminalWebAuthenticationDetails;
import ir.daneshrefah.scm.uaa.security.userDetails.UserDetailsService;
import ir.daneshrefah.scm.uaa.service.ClientService;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-18
 */
@Component
public class GeneralAuthenticationProvider extends AbstractUserDetailsAuthenticationProvider {

    private final ClientService clientService;
    private final UserDetailsService userDetailsService;

    public GeneralAuthenticationProvider(ClientService clientService, UserDetailsService userDetailsService) {
        this.clientService = clientService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails, UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {

    }

    @Override
    protected UserDetails retrieveUser(String username, UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
        String clientId = null;
        String terminalCode = null;
        if (null != authentication.getDetails() && authentication.getDetails() instanceof TerminalWebAuthenticationDetails) {
            clientId = ((TerminalWebAuthenticationDetails) authentication.getDetails()).getClientId();
            terminalCode = clientService.findByClientId(clientId).getTerminalCode();
        }
        try {
            UserDetails loadedUser = userDetailsService.loadUserByUsername(username, terminalCode);
            if (loadedUser == null) {
                throw new InternalAuthenticationServiceException(
                        "UserDetailsService returned null, which is an interface contract violation");
            }
            return loadedUser;
        }
        catch (UsernameNotFoundException ex) {
//            mitigateAgainstTimingAttack(authentication);
            throw ex;
        }
        catch (InternalAuthenticationServiceException ex) {
            throw ex;
        }
        catch (Exception ex) {
            throw new InternalAuthenticationServiceException(ex.getMessage(), ex);
        }
    }

    /*private void mitigateAgainstTimingAttack(UsernamePasswordAuthenticationToken authentication) {
        if (authentication.getCredentials() != null) {
            String presentedPassword = authentication.getCredentials().toString();
            this.passwordEncoder.matches(presentedPassword, this.userNotFoundEncodedPassword);
        }
    }*/
}
