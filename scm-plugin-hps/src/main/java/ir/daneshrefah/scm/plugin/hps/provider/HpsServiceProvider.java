package ir.daneshrefah.scm.plugin.hps.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.service.ServiceService;
import ir.daneshrefah.scm.plugin.api.model.service.external.AbstractPureExternalServiceProviderExecutor;
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.springframework.stereotype.Component;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-03-30
 */
@Component
public class HpsServiceProvider extends AbstractPureExternalServiceProviderExecutor {


    public HpsServiceProvider(ServiceService serviceService, ProducerTemplate producerTemplate, CamelContext camelContext, ObjectMapper objectMapper) {
        super(serviceService, producerTemplate, camelContext, objectMapper);
    }

    @Override
    public String extractProviderCode() {
        return "HPS";
    }

    @Override
    public JsonNode executeEndpoint(Message originalMessage, Object body) {
        return null;
    }
}
