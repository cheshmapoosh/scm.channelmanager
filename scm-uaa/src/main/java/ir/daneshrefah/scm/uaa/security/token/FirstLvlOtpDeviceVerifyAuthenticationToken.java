package ir.daneshrefah.scm.uaa.security.token;

import ir.daneshrefah.scm.uaa.common.security.authenticationDetails.TerminalUserDetails;

public class FirstLvlOtpDeviceVerifyAuthenticationToken extends GeneralAuthenticationToken {

    public FirstLvlOtpDeviceVerifyAuthenticationToken(TerminalUserDetails user, PreAuthenticationToken preAuthenticationToken) {
        super(user, preAuthenticationToken);
    }
}
