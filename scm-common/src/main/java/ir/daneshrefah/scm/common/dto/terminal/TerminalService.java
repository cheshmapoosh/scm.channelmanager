package ir.daneshrefah.scm.common.dto.terminal;

import ir.daneshrefah.scm.common.dto.spec.PagedResponseData;
import ir.daneshrefah.scm.common.model.terminal.LegacyTerminal;
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
@Deprecated
public abstract class TerminalService {

    public static TerminalService INSTANCE;

    protected TerminalService() {
        INSTANCE = this;
    }

    public abstract List<Terminal> findAllTerminals();

    public abstract Optional<Terminal> findTerminalById(String id);

    public abstract Optional<Terminal> findTerminalByLegacyId(Integer id);

    public abstract Optional<Terminal> findTerminalByCode(String code);

    public abstract PagedResponseData<Terminal> findAllTerminals(TerminalFindRequest request);

    public abstract List<TerminalServiceAccess> findTerminalServiceAccessByTerminalId(String terminalId);

    public abstract Optional<TerminalServiceAccess> findTerminalServiceAccessByTerminalCodeAndServiceCode(String terminalCode, String serviceCode);

    public abstract TerminalServiceAccess assignServiceToTerminal(TerminalServiceAssignmentRequest request);

    public abstract Terminal craeteTerminal(TerminalCreateRequest request);

    public abstract void deleteTerminal(TerminalDeleteRequest request);

    public abstract Terminal editTerminal(TerminalEditRequest request);

    public abstract void revokeServiceFromTerminal(TerminalServiceAssignmentRequest request);

    public abstract List<Terminal> findAllTerminalAccessOnService(String serviceId);

    public abstract List<TerminalServiceAccess> findAllTerminalServiceAccesses();

    public abstract void evictCache();

    public abstract List<LegacyTerminal> findAllLegacyTerminal();
}
