package ir.daneshrefah.scm.uaa.security.token;

import ir.daneshrefah.scm.uaa.common.security.authenticationDetails.TerminalUserDetails;

public class FirstLvlPinAuthenticationToken extends GeneralAuthenticationToken {

    public FirstLvlPinAuthenticationToken(TerminalUserDetails user, PreAuthenticationToken preAuthenticationToken) {
        super(user, preAuthenticationToken);
    }
}
