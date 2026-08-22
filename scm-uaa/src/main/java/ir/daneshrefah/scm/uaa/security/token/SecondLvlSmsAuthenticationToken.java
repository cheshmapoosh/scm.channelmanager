package ir.daneshrefah.scm.uaa.security.token;

import ir.daneshrefah.scm.uaa.common.security.authenticationDetails.TerminalUserDetails;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-28
 */
public class SecondLvlSmsAuthenticationToken extends GeneralAuthenticationToken {

    public SecondLvlSmsAuthenticationToken(TerminalUserDetails user, PreAuthenticationToken authenticationToken) {
        super(user, authenticationToken);
    }

}
