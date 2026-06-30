package ir.daneshrefah.scm.uaa.security.oauth2.token;

import ir.daneshrefah.scm.uaa.common.security.authenticationDetails.TerminalUserDetails;
import ir.daneshrefah.scm.uaa.security.authentication.token.UserLoginAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.Optional;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-02-06
 */
public interface AuthenticationRequestTokenGenerator {

     Optional<Class<? extends UserLoginAuthenticationToken>> extractTokenType(Authentication authentication, TerminalUserDetails userDetails);

}
