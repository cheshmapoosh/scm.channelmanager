package ir.daneshrefah.scm.common.service;

import ir.daneshrefah.scm.common.dto.PagedResponseData;
import ir.daneshrefah.scm.common.model.terminal.Terminal;
import ir.daneshrefah.scm.common.model.terminal.TerminalServiceAccess;

import java.util.List;
import java.util.Optional;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-17
 */
public interface TerminalService {

    public List<Terminal> findAllTerminals();

    public Optional<Terminal> findTerminalById(String id);

    public Optional<Terminal> findTerminalByCode(String code);

    public PagedResponseData<Terminal> findAllTerminals(TerminalFindRequest request);

    public List<TerminalServiceAccess> findTerminalServiceAccessByTerminalId(String terminalId);

    public Optional<TerminalServiceAccess> findTerminalServiceAccessByTerminalCodeAndServiceCode(String terminalCode, String serviceCode);

    public TerminalServiceAccess assignServiceToTerminal(String terminalId, String serviceId);

}
