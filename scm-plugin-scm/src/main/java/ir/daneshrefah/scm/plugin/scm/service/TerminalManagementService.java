package ir.daneshrefah.scm.plugin.scm.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.dto.spec.PagedResponseData;
import ir.daneshrefah.scm.common.dto.terminal.*;
import ir.daneshrefah.scm.common.exception.MissingRequiredInputException;
import ir.daneshrefah.scm.common.exception.NoMatchRecordFoundException;
import ir.daneshrefah.scm.common.model.terminal.Terminal;
import ir.daneshrefah.scm.common.model.terminal.TerminalServiceAccess;
import ir.daneshrefah.scm.plugin.api.annotation.JavaService;
import ir.daneshrefah.scm.plugin.api.integration.ServiceProducerTemplate;
import ir.daneshrefah.scm.plugin.api.service.AbstractJavaService;
import ir.daneshrefah.scm.utils.string.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-17
 */
@Service
public class TerminalManagementService extends AbstractJavaService {

    private final TerminalService terminalService;


    public TerminalManagementService(ServiceProducerTemplate producerTemplate, ObjectMapper objectMapper,
                                     TerminalService terminalService) {
        super(producerTemplate, objectMapper);
        this.terminalService = terminalService;
    }

    @JavaService
    public PagedResponseData<Terminal> listTerminal(TerminalFindRequest request) {
        return terminalService.findAllTerminals(request);
    }

    @JavaService
    public Terminal findTerminalById(String terminalId) {
        if (StringUtils.isEmpty(terminalId)) {
            throw new MissingRequiredInputException("terminalId");
        }
        Optional<Terminal> terminal = terminalService.findTerminalById(terminalId);
        if (terminal.isEmpty()) {
            throw new NoMatchRecordFoundException("terminal");
        }
        return terminal.get();
    }

    @JavaService
    public Terminal createTerminal(TerminalCreateRequest request) {
        return terminalService.craeteTerminal(request);
    }

    @JavaService
    public Terminal editTerminal(TerminalEditRequest request) {
        return terminalService.editTerminal(request);
    }

    @JavaService
    public void deleteTerminal(TerminalDeleteRequest request) {
        terminalService.deleteTerminal(request);
    }

    @JavaService
    public TerminalServiceAccess addServiceAssignment(TerminalServiceAssignmentRequest request) {
        return terminalService.assignServiceToTerminal(request);
    }

    @JavaService
    public void deleteServiceAssignment(TerminalServiceAssignmentRequest request) {
         terminalService.revokeServiceFromTerminal(request);
    }

    @JavaService
    public List<Terminal> findAllTerminalAccessOnService(String serviceId){
        return terminalService.findAllTerminalAccessOnService(serviceId);
    }

}
