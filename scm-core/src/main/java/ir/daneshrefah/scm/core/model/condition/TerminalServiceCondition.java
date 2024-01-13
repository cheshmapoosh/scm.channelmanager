package ir.daneshrefah.scm.core.model.condition;

import ir.daneshrefah.scm.common.model.terminal.TerminalServiceAccess;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TerminalServiceCondition extends BaseCondition {

    private TerminalServiceAccess terminalServiceAccess;

}
