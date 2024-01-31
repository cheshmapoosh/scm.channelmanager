package ir.daneshrefah.scm.plugin.mock.provider;

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
 * @since 2023-08-05
 */
@Component("mockCoreServiceProvider")
public class MockServiceProvider extends AbstractExternalServiceProvider {

    @Override
    public boolean initServerConfigs() {
        return true;
    }

    @Override
    protected JsonNode executeInternal(Message message, Service service, Object requestBody) {
        return null;
    }

}
