package ir.daneshrefah.scm.common.model.authority.terminal;

import ir.daneshrefah.scm.common.model.authentication.AuthenticationMethod;
import ir.daneshrefah.scm.common.model.authority.Authority;
import ir.daneshrefah.scm.common.model.terminal.Channel;
import ir.daneshrefah.scm.common.model.terminal.Terminal;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-08-13
 */
public interface TerminalAuthority extends Authority {

    public Terminal getTerminal();
    public Channel getChannel();
    public AuthenticationMethod getAuthenticationMethod();

}
