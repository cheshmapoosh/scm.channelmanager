package ir.daneshrefah.scm.uaa.security.authentication.method.token;

import ir.daneshrefah.scm.uaa.common.security.authenticationDetails.TerminalUserDetails;
import ir.daneshrefah.scm.uaa.security.authentication.token.PreAuthenticationToken;
import ir.daneshrefah.scm.uaa.security.authentication.token.UserLoginAuthenticationToken;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-23
 */
public class StaticPasswordLoginAuthenticationToken extends UserLoginAuthenticationToken {

    public StaticPasswordLoginAuthenticationToken(TerminalUserDetails user, PreAuthenticationToken authenticationToken) {
        super(user, authenticationToken);
    }

}
