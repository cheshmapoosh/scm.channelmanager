package ir.daneshrefah.scm.uaa.security.token.generator;

import ir.daneshrefah.scm.uaa.common.security.authenticationDetails.TerminalUserDetails;
import ir.daneshrefah.scm.uaa.security.token.GeneralAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-02-06
 */
@Component
public class GeneralAuthenticationRequestTokenGenerator implements AuthenticationRequestTokenGenerator {

    @Override
    public Optional<Class<? extends GeneralAuthenticationToken>> extractTokenType(Authentication authentication, TerminalUserDetails userDetails) {
        return Optional.empty();
    }

}
