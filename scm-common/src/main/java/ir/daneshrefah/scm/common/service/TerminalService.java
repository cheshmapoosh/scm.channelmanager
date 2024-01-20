package ir.daneshrefah.scm.common.service;

import ir.daneshrefah.scm.common.model.terminal.Terminal;
import ir.daneshrefah.scm.common.model.terminal.TerminalServiceAccess;

import java.util.List;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-17
 */
public interface TerminalService {

    public List<Terminal> findAllTerminals();

    public List<TerminalServiceAccess> findTerminalServiceAccessByTerminalId(String terminalId);

    public TerminalServiceAccess assignServiceToTerminal(String terminalId, String serviceId);

}
