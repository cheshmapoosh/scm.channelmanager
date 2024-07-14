package ir.daneshrefah.scm.core.integration.provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.service.ResourceService;
import ir.daneshrefah.scm.plugin.api.model.service.external.AbstractRestExternalServiceProviderExecutor;
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-07-14
 */
public class DefaultRestServiceProviderExecutor extends AbstractRestExternalServiceProviderExecutor {

    public DefaultRestServiceProviderExecutor(ProducerTemplate producerTemplate, CamelContext camelContext, ResourceService resourceService, ObjectMapper objectMapper) {
        super(producerTemplate, camelContext, resourceService, objectMapper);
    }

    @Override
    protected String prepareTargetUrl(Message message) {
        message.getHeader().getService();
        return null;
    }

}