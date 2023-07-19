package ir.daneshrefah.scm.common.model.terminal;

import ir.daneshrefah.scm.common.model.BaseModel;
import ir.daneshrefah.scm.uaa.common.model.AuthenticationMethod;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-19
 */
public class TerminalServiceAuthenticationAccess extends BaseModel {
    private TerminalServiceAccess terminalServiceAccess;
    private AuthenticationMethod authenticationMethod;

    public TerminalServiceAccess getTerminalServiceAccess() {
        return terminalServiceAccess;
    }

    public void setTerminalServiceAccess(TerminalServiceAccess terminalServiceAccess) {
        this.terminalServiceAccess = terminalServiceAccess;
    }

    public AuthenticationMethod getAuthenticationMethod() {
        return authenticationMethod;
    }

    public void setAuthenticationMethod(AuthenticationMethod authenticationMethod) {
        this.authenticationMethod = authenticationMethod;
    }
}
