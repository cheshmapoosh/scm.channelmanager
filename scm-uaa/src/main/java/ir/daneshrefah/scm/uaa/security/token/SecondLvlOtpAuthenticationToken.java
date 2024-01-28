package ir.daneshrefah.scm.uaa.security.token;

import ir.daneshrefah.scm.uaa.common.security.authenticationDetails.TerminalUserDetails;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-28
 */
public class SecondLvlOtpAuthenticationToken extends GeneralAuthenticationToken {

    public SecondLvlOtpAuthenticationToken(TerminalUserDetails user, PreAuthenticationToken authenticationToken) {
        super(user, authenticationToken);
    }

}
