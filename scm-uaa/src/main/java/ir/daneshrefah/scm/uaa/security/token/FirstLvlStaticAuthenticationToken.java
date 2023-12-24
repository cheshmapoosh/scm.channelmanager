package ir.daneshrefah.scm.uaa.security.token;

import ir.daneshrefah.scm.uaa.security.userDetails.TerminalUserDetails;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-23
 */
public class FirstLvlStaticAuthenticationToken extends GeneralAuthenticationToken {

    public FirstLvlStaticAuthenticationToken(TerminalUserDetails user, PreAuthenticationToken authenticationToken) {
        super(user, authenticationToken);
    }

}
