package ir.daneshrefah.scm.uaa.security.authenticationProvider;

import ir.daneshrefah.scm.uaa.security.authenticationDetails.TerminalWebAuthenticationDetails;
import ir.daneshrefah.scm.uaa.service.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class GeneralAuthenticationProvider extends AbstractUserDetailsAuthenticationProvider {

    @Autowired
    private ClientService clientService;

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
//        authentication.getDetails()
        return null;
    }

}
