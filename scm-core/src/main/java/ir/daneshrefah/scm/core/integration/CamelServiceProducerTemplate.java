package ir.daneshrefah.scm.core.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
import ir.daneshrefah.scm.common.exception.TerminalNotAssignedServiceException;
import ir.daneshrefah.scm.common.exception.ValidationException;
import ir.daneshrefah.scm.common.model.error.ErrorCodes;
import ir.daneshrefah.scm.common.model.terminal.TerminalServiceAccess;
import ir.daneshrefah.scm.common.service.ServiceService;
import ir.daneshrefah.scm.common.service.TerminalService;
import ir.daneshrefah.scm.plugin.api.inbound.MessageContext;
import ir.daneshrefah.scm.plugin.api.integration.ServiceProducerTemplate;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.utils.MessageUtils;
import lombok.RequiredArgsConstructor;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-08-06
 */
@RequiredArgsConstructor
@Service
public class CamelServiceProducerTemplate implements ServiceProducerTemplate {

    private final ServiceService serviceService;
    private final TerminalService terminalService;

    @Autowired
    private ProducerTemplate producerTemplate;
    @Override
    public Message callService(ir.daneshrefah.scm.common.model.service.Service service, Message message) {
        String serviceUrl = "direct:SVI_" + service.getCode();
        Exchange exchangeResult = producerTemplate.send(serviceUrl, exchange -> {
            exchange.getIn().setBody(message);
        });
        return message;
    }

    @Override
    public Message callService(String serviceCode, JsonNode payload) {
        ir.daneshrefah.scm.common.model.service.Service service = serviceService.findServiceByCode(serviceCode);
        if (null == service) {
            throw new ValidationException(serviceCode, ErrorCodes.ERROR_CODE_VALIDATION_SERVICE_CODE_IS_INVALID, "service not found");
        }
        Message message = MessageContext.getCurrentContext().getMessage();
        String terminalCode = MessageUtils.getTerminalCode(message);
        Optional<TerminalServiceAccess> serviceAccess = terminalService
                .findTerminalServiceAccessByTerminalCodeAndServiceCode(terminalCode, serviceCode);
        if (serviceAccess.isEmpty()) {
            throw new TerminalNotAssignedServiceException(service, terminalCode);
        }
        Message tempMessage = MessageUtils.generateInternalMessage(message, serviceAccess.get(), payload);
        callService(serviceAccess.get().getService(), tempMessage);
        /*if (null != tempMessage.getErrors() && !tempMessage.getErrors().isEmpty()) {
            throw
            message.addErrors(tempMessage.getErrors());
        }*/
        return tempMessage;
    }

    @Override
    public Message callService(String serviceCode) {
        return callService(serviceCode, NullNode.getInstance());
    }

}
