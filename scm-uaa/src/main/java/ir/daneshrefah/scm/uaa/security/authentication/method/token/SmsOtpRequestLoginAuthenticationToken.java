package ir.daneshrefah.scm.uaa.security.authentication.method.token;

import ir.daneshrefah.scm.uaa.common.security.authenticationDetails.TerminalUserDetails;
import ir.daneshrefah.scm.uaa.security.authentication.token.PreAuthenticationToken;
import ir.daneshrefah.scm.uaa.security.authentication.token.UserLoginAuthenticationToken;

public class SmsOtpRequestLoginAuthenticationToken extends UserLoginAuthenticationToken {
    public SmsOtpRequestLoginAuthenticationToken(TerminalUserDetails user, PreAuthenticationToken preAuthenticationToken) {
        super(user, preAuthenticationToken);
    }
}
