package ir.daneshrefah.scm.plugin.api.model.terminal;


import ir.daneshrefah.scm.plugin.api.model.BaseModel;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-19
 */
public class TerminalServiceChannelAccess extends BaseModel<String> {
    private TerminalServiceAccess terminalServiceAccess;
    private Channel channel;

    public TerminalServiceAccess getTerminalServiceAccess() {
        return terminalServiceAccess;
    }

    public void setTerminalServiceAccess(TerminalServiceAccess terminalServiceAccess) {
        this.terminalServiceAccess = terminalServiceAccess;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }
}
