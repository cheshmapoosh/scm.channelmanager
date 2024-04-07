package ir.daneshrefah.scm.plugin.mock.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.terminal.TerminalServiceAccess;
import ir.daneshrefah.scm.common.service.ServiceService;
import ir.daneshrefah.scm.plugin.api.model.service.external.AbstractCamelExternalServiceProviderExecutor;
import ir.daneshrefah.scm.plugin.api.model.service.external.AbstractPureExternalServiceProviderExecutor;
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.springframework.stereotype.Component;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-08-05
 */
@Component("mockCoreServiceProvider")
public class MockServiceProvider extends AbstractPureExternalServiceProviderExecutor {

    public MockServiceProvider(ProducerTemplate producerTemplate, CamelContext camelContext, ObjectMapper objectMapper) {
        super(producerTemplate, camelContext, objectMapper);
    }

    @Override
    public JsonNode executeEndpoint(Message originalMessage, Object body) {
        TerminalServiceAccess serviceAccess = originalMessage.getHeader().getServiceAccess();
//        serviceAccess.get
        return null;
    }

}
