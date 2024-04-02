package ir.daneshrefah.scm.plugin.scm.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.dto.PagedResponseData;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.terminal.Terminal;
import ir.daneshrefah.scm.common.service.TerminalFindRequest;
import ir.daneshrefah.scm.common.service.TerminalInfoRequest;
import ir.daneshrefah.scm.common.service.TerminalService;
import ir.daneshrefah.scm.plugin.api.integration.ServiceProducerTemplate;
import ir.daneshrefah.scm.plugin.api.service.AbstractJavaService;
import org.springframework.stereotype.Service;

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

    public TerminalManagementService(ServiceProducerTemplate producerTemplate, ObjectMapper objectMapper, TerminalService terminalService) {
        super(producerTemplate, objectMapper);
        this.terminalService = terminalService;
    }

    public PagedResponseData<Terminal> listTerminal(TerminalFindRequest request) {
        return terminalService.findAllTerminals(request);
    }

    public Terminal createTerminal(TerminalInfoRequest request) {
        return null;
    }

    public Terminal editTerminal(TerminalInfoRequest request, String terminalId) {
        return null;
    }

    public Object addService(String terminalId, String serviceId) {
//        String terminalId = message.getPayloadValue("terminalId");
//        String serviceId = message.getPayloadValue("serviceId");
        return terminalService.assignServiceToTerminal(terminalId, serviceId);
    }

}
