package ir.daneshrefah.scm.uaa.security.token;

import ir.daneshrefah.scm.uaa.common.security.authenticationDetails.TerminalUserDetails;

public class FirstLvlPatternAuthenticationToken extends GeneralAuthenticationToken {

    public FirstLvlPatternAuthenticationToken(TerminalUserDetails user, PreAuthenticationToken preAuthenticationToken) {
        super(user, preAuthenticationToken);
    }
}
