package ir.daneshrefah.scm.common.model.condition;

import ir.daneshrefah.scm.common.BaseModel;
import ir.daneshrefah.scm.common.model.authentication.AuthenticationMethod;
import ir.daneshrefah.scm.common.model.terminal.Terminal;

public class TerminalCondition extends BaseCondition<String> {


    private String id;
    private Terminal terminal;


    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    public Terminal getTerminal() {
        return terminal;
    }

    public void setTerminal(Terminal terminal) {
        this.terminal = terminal;
    }

}
