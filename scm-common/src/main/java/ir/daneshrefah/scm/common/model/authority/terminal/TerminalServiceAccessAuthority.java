package ir.daneshrefah.scm.common.model.authority.terminal;


import ir.daneshrefah.scm.common.model.authentication.AuthenticationMethod;
import ir.daneshrefah.scm.common.model.authority.AuthorityType;
import ir.daneshrefah.scm.common.model.authority.ServiceAccessAuthority;
import ir.daneshrefah.scm.common.model.terminal.Channel;
import ir.daneshrefah.scm.common.model.terminal.Terminal;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-19
 */
public class TerminalServiceAccessAuthority extends ServiceAccessAuthority implements TerminalAuthority {

    private Terminal terminal;
    private Channel channel;
    private AuthenticationMethod authenticationMethod;

    public Terminal getTerminal() {
        return terminal;
    }

    public void setTerminal(Terminal terminal) {
        this.terminal = terminal;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public AuthenticationMethod getAuthenticationMethod() {
        return authenticationMethod;
    }

    public void setAuthenticationMethod(AuthenticationMethod authenticationMethod) {
        this.authenticationMethod = authenticationMethod;
    }

    @Override
    public AuthorityType getType() {
        return AuthorityType.TERMINAL_SERVICE_ACCESS;
    }
}
