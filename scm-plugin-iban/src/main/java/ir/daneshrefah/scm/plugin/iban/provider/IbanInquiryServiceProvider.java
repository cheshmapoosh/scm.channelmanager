package ir.daneshrefah.scm.plugin.iban.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.message.MessageOutput;
import ir.daneshrefah.scm.common.service.ResourceService;
import ir.daneshrefah.scm.common.service.ServiceService;
import ir.daneshrefah.scm.plugin.api.model.service.external.povider.executor.AbstractPureExternalServiceProviderExecutor;
import org.springframework.stereotype.Component;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-03-18
 */
@Component
public class IbanInquiryServiceProvider extends AbstractPureExternalServiceProviderExecutor {


    public IbanInquiryServiceProvider(ObjectMapper objectMapper, ResourceService resourceService, ServiceService serviceService) {
        super(objectMapper, resourceService, serviceService);
    }

    @Override
    public JsonNode executeEndpoint(Message originalMessage, Object body) {
        return null;
    }

    @Override
    protected MessageOutput buildMessageOutput() {
        return null;
    }
}
