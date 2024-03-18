package ir.daneshrefah.scm.plugin.iban.provider;

import com.fasterxml.jackson.databind.JsonNode;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.service.Service;
import ir.daneshrefah.scm.plugin.api.model.service.external.AbstractExternalServiceProvider;
import org.springframework.stereotype.Component;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-03-18
 */
@Component
public class IbanInquiryServiceProvider extends AbstractExternalServiceProvider {


    @Override
    protected boolean initServerConfigs() {
        return false;
    }

    @Override
    protected JsonNode executeInternal(Message message, Service service, Object requestBody) {
        return null;
    }

}
