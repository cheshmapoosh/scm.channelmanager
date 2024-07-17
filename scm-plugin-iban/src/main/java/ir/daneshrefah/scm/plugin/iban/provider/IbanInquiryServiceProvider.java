package ir.daneshrefah.scm.plugin.iban.provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.service.ResourceService;
import ir.daneshrefah.scm.plugin.api.model.service.external.AbstractCamelExternalServiceProviderExecutor;
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

    public IbanInquiryServiceProvider(ResourceService resourceService, ObjectMapper objectMapper) {
        super(resourceService, objectMapper);
    }

    @Override
    protected String extractTargetEndpointUrl(Message message) {
        return null;
    }

}
