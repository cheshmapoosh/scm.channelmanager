package ir.daneshrefah.scm.plugin.api.model.service.external;

import ir.daneshrefah.scm.common.model.service.AbstractExternalServiceProvider;
import org.apache.camel.Exchange;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.model.TryDefinition;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-03-30
 */
public interface ExternalServiceProviderExecutor {

    String HEADER_ORIGINAL_MESSAGE = "ScmOriginalMessage";
    String HEADER_END_TIME = "ScmProviderEndTime";
    String HEADER_MESSAGE_OUTPUT = "ScmMessageOutput";
    String HEADER_RESPONSE_BODY = "ScmResponseBody";

    void init(AbstractExternalServiceProvider provider);

    void endpointCallRouteDefinition(RouteDefinition routeDefinition);

    AbstractExternalServiceProvider getProviderModel();

}
