package ir.daneshrefah.scm.common.model.authority.terminal;

import ir.daneshrefah.scm.common.model.authority.Authority;
import ir.daneshrefah.scm.common.model.authority.AuthorityType;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-08-14
 */
public class TerminalServiceAccessAuthority extends Authority {

    private Boolean targetServiceAccessAllow;

    @Override
    public AuthorityType getAuthorityType() {
        return AuthorityType.TERMINAL_SERVICE_ACCESS;
    }

    public Boolean getTargetServiceAccessAllow() {
        return targetServiceAccessAllow;
    }

    public void setTargetServiceAccessAllow(Boolean targetServiceAccessAllow) {
        this.targetServiceAccessAllow = targetServiceAccessAllow;
    }

}
