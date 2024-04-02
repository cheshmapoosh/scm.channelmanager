package ir.daneshrefah.scm.plugin.iban.provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.service.ServiceService;
import ir.daneshrefah.scm.plugin.api.model.service.external.AbstractCamelExternalServiceProviderExecutor;
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.springframework.stereotype.Component;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-03-18
 */
@Component
public class IbanInquiryServiceProvider extends AbstractCamelExternalServiceProviderExecutor {

    public IbanInquiryServiceProvider(ServiceService serviceService, ProducerTemplate producerTemplate, CamelContext camelContext, ObjectMapper objectMapper) {
        super(serviceService, producerTemplate, camelContext, objectMapper);
    }

    @Override
    protected String extractTargetUrl(Message message) {
        return null;
    }

    @Override
    public String extractProviderCode() {
        return "IBAN";
    }
}
