package ir.daneshrefah.scm.uaa.security.token;

import ir.daneshrefah.scm.uaa.security.userDetails.TerminalUserDetails;

import java.util.Collections;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-23
 */
public abstract class GeneralAuthenticationToken extends AbstractAuthenticationToken {

    private final PreAuthenticationToken preAuthenticationToken;


    protected GeneralAuthenticationToken(TerminalUserDetails user, PreAuthenticationToken preAuthenticationToken) {
        super(Collections.emptyList());
        this.setDetails(user);
        this.preAuthenticationToken = preAuthenticationToken;
    }
}
