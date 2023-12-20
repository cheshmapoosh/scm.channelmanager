package ir.daneshrefah.scm.core.model.condition;

import ir.daneshrefah.scm.common.model.terminal.TerminalServiceAccess;


public class TerminalServiceCondition extends BaseCondition<String> {

    private String id;
    private TerminalServiceAccess terminalServiceAccess;


    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    public TerminalServiceAccess getTerminalServiceAccess() {
        return terminalServiceAccess;
    }

    public void setTerminalServiceAccess(TerminalServiceAccess terminalServiceAccess) {
        this.terminalServiceAccess = terminalServiceAccess;
    }

}
