package ir.daneshrefah.scm.plugin.api.authority.decision;


import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.terminal.TerminalServiceChannelAccess;
import ir.daneshrefah.scm.plugin.api.authority.exception.AuthorityBaseException;
import ir.daneshrefah.scm.plugin.api.exception.AccessDeniedException;

public interface DecisionManager {

    void decide(TerminalServiceChannelAccess terminalServiceChannelAccess, Message message) throws AuthorityBaseException;

}
