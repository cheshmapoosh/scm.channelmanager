package ir.daneshrefah.scm.uaa.security.authenticationProvider.providers;

import ir.daneshrefah.scm.uaa.common.security.authenticationDetails.TerminalUserDetails;
import ir.daneshrefah.scm.uaa.security.CustomMD5Encoder;
import ir.daneshrefah.scm.uaa.security.token.GeneralAuthenticationToken;
import ir.daneshrefah.scm.uaa.service.user.UserService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-23
 */
public abstract class AbstractStaticAuthenticationProvider extends AbstractAuthenticationProvider {

    protected final CustomMD5Encoder encoder;

    protected AbstractStaticAuthenticationProvider(UserService userService, CustomMD5Encoder encoder) {
        super(userService);
        this.encoder = encoder;
    }


    @Override
    protected void additionalAuthenticationChecks(TerminalUserDetails userDetails,
                                                  GeneralAuthenticationToken authentication) throws AuthenticationException {
        if (authentication.getCredentials() == null) {
            this.logger.debug("Failed to authenticate since no credentials provided");
            throw new BadCredentialsException("AbstractStaticAuthenticationProvider.badCredentials");
        }
        String presentedPassword = authentication.getCredentials().toString();
        String encodedPassword = encoder.encodePassword(presentedPassword, userDetails.getUser().getPerson().getUsername());
        String currentEncodePassword = extractCurrentPassword(userDetails);
        if (null == currentEncodePassword || !currentEncodePassword.trim().equals(encodedPassword)) {
            this.logger.debug("Failed to authenticate since password does not match stored value. currentEncodePassword: " + currentEncodePassword + ",  encodedPassword: " + encodedPassword);
            throw new BadCredentialsException("AbstractStaticAuthenticationProvider.badCredentials");
        }
    }

    public abstract String extractCurrentPassword(TerminalUserDetails userDetails);
}
