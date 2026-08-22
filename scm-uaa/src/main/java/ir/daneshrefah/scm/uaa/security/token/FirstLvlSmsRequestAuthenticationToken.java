package ir.daneshrefah.scm.uaa.security.token;

import ir.daneshrefah.scm.uaa.common.security.authenticationDetails.TerminalUserDetails;

public class FirstLvlSmsRequestAuthenticationToken extends GeneralAuthenticationToken {
    public FirstLvlSmsRequestAuthenticationToken(TerminalUserDetails user, PreAuthenticationToken preAuthenticationToken) {
        super(user, preAuthenticationToken);
    }
}
