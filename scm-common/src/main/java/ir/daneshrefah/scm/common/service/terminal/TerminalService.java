package ir.daneshrefah.scm.common.service.terminal;

import ir.daneshrefah.scm.common.dto.PagedResponseData;
import ir.daneshrefah.scm.common.model.service.Service;
import ir.daneshrefah.scm.common.model.terminal.Terminal;
import ir.daneshrefah.scm.common.model.terminal.TerminalServiceAccess;
import ir.daneshrefah.scm.common.service.ServiceAccessFindRequest;

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

    List<Terminal> findAllTerminals();

    Optional<Terminal> findTerminalById(String id);

    Optional<Terminal> findTerminalByCode(String code);

    PagedResponseData<Terminal> findAllTerminals(TerminalFindRequest request);

    List<TerminalServiceAccess> findTerminalServiceAccessByTerminalId(String terminalId);

    Optional<TerminalServiceAccess> findTerminalServiceAccessByTerminalCodeAndServiceCode(String terminalCode, String serviceCode);

    TerminalServiceAccess assignServiceToTerminal(TerminalServiceAssignmentRequest request);

    Terminal craeteTerminal(TerminalCreateRequest request);

    void deleteTerminal(TerminalDeleteRequest request);

    Terminal editTerminal(TerminalEditRequest request);

    void revokeServiceFromTerminal(TerminalServiceAssignmentRequest request);

    List<Terminal> findAllTerminalAccessOnService(String serviceId);
    List<TerminalServiceAccess> findAllTerminalServiceAccesses();

}
