package ir.daneshrefah.scm.plugin.api.model.service.external;

import ir.daneshrefah.scm.common.model.service.AbstractAuditableExternalServiceProvider;
import org.apache.camel.model.RouteDefinition;

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

    void init(AbstractAuditableExternalServiceProvider provider);

    void endpointCallRouteDefinition(RouteDefinition routeDefinition);

    AbstractAuditableExternalServiceProvider getProviderModel();

}
