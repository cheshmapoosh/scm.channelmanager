package ir.daneshrefah.scm.uaa.security.token;

import ir.daneshrefah.scm.uaa.common.security.authenticationDetails.TerminalUserDetails;

public class FirstLvlOtpDeviceRequestAuthenticationToken extends GeneralAuthenticationToken {

    public FirstLvlOtpDeviceRequestAuthenticationToken(TerminalUserDetails user, PreAuthenticationToken preAuthenticationToken) {
        super(user, preAuthenticationToken);
    }
}
