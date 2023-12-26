package ir.daneshrefah.scm.uaa.security.authenticationProvider.providers;

import ir.daneshrefah.scm.uaa.security.CustomMD5Encoder;
import ir.daneshrefah.scm.uaa.security.token.GeneralAuthenticationToken;
import ir.daneshrefah.scm.uaa.security.token.PostAuthenticationToken;
import ir.daneshrefah.scm.uaa.security.userDetails.TerminalUserDetails;
import org.springframework.security.core.Authentication;

public abstract class AbstractStaticRequestAuthenticationProvider extends AbstractStaticAuthenticationProvider{

    protected AbstractStaticRequestAuthenticationProvider(CustomMD5Encoder encoder) {
        super(encoder);
    }

    protected Authentication createSuccessAuthentication(Authentication authentication) {
        // Ensure we return the original credentials the user supplied,
        // so subsequent attempts are successful even with encoded passwords.
        // Also ensure we return the original getDetails(), so that future
        // authentication events after cache expiry contain the details
        PostAuthenticationToken result = PostAuthenticationToken.incomplete(
                (TerminalUserDetails) authentication.getDetails(),
                ((GeneralAuthenticationToken) authentication).getPreAuthenticationToken());
        result.setDetails(authentication.getDetails());
        this.logger.debug("Authenticated user");
        return result;
    }
}
