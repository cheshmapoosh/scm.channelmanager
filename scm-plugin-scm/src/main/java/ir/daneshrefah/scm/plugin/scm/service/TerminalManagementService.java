package ir.daneshrefah.scm.plugin.scm.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.model.message.Message;
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

    public Object listTerminal(Message message, ir.daneshrefah.scm.common.model.service.Service service, Object payload) {
        return terminalService.findAllTerminals();
    }

    public Object addService(Message message, ir.daneshrefah.scm.common.model.service.Service service, Object payload) {
        String terminalId = message.getPayloadValue("terminalId");
        String serviceId = message.getPayloadValue("serviceId");
        return terminalService.assignServiceToTerminal(terminalId, serviceId);
    }

}
