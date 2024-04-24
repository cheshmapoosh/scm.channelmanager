package ir.daneshrefah.scm.plugin.scm.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.dto.PagedResponseData;
import ir.daneshrefah.scm.common.exception.MissingRequiredInputException;
import ir.daneshrefah.scm.common.exception.NoMatchRecordFoundException;
import ir.daneshrefah.scm.common.model.terminal.Terminal;
import ir.daneshrefah.scm.common.model.terminal.TerminalServiceAccess;
import ir.daneshrefah.scm.common.service.terminal.*;
import ir.daneshrefah.scm.plugin.api.integration.ServiceProducerTemplate;
import ir.daneshrefah.scm.plugin.api.service.AbstractJavaService;
import ir.daneshrefah.scm.process.service.ProcessDefinitionService;
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
//    private final ProcessDefinitionService processDefinitionService;

    public TerminalManagementService(ServiceProducerTemplate producerTemplate, ObjectMapper objectMapper,
                                     TerminalService terminalService/*, ProcessDefinitionService processDefinitionService*/) {
        super(producerTemplate, objectMapper);
        this.terminalService = terminalService;
//        this.processDefinitionService = processDefinitionService;
    }

    public PagedResponseData<Terminal> listTerminal(TerminalFindRequest request) {
//        processDefinitionService.deployProcessDefinition();
        return terminalService.findAllTerminals(request);
    }

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

    public Terminal createTerminal(TerminalCreateRequest request) {
        return terminalService.craeteTerminal(request);
    }

    public Terminal editTerminal(TerminalEditRequest request) {
        return terminalService.editTerminal(request);
    }

    public void deleteTerminal(TerminalDeleteRequest request) {
        terminalService.deleteTerminal(request);
    }

    public TerminalServiceAccess addServiceAssignment(TerminalServiceAssignmentRequest request) {
        return terminalService.assignServiceToTerminal(request);
    }

    public void deleteServiceAssignment(TerminalServiceAssignmentRequest request) {
         terminalService.revokeServiceFromTerminal(request);
    }

    public List<Terminal> findAllTerminalAccessOnService(String serviceId){
        return terminalService.findAllTerminalAccessOnService(serviceId);
    }

}
