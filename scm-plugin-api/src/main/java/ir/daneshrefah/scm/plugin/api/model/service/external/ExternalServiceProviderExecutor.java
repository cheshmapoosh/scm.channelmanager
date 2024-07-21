package ir.daneshrefah.scm.plugin.api.model.service.external;

import ir.daneshrefah.scm.common.model.service.AbstractExternalServiceProvider;
import ir.daneshrefah.scm.plugin.api.transformer.TransformerExecutionWrapper;
import org.apache.camel.model.RouteDefinition;

import java.util.List;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-03-30
 */
public interface ExternalServiceProviderExecutor {

    String HEADER_ORIGINAL_MESSAGE = "ScmOriginalMessage";
    String HEADER_START_TIME = "ScmProviderStartTime";
    String HEADER_END_TIME = "ScmProviderEndTime";
    String HEADER_REQUEST_BODY = "ScmRequestBody";
    String HEADER_RESPONSE_BODY = "ScmResponseBody";
    String HEADER_TARGET_URL = "ScmTargetUrl";

    void init(AbstractExternalServiceProvider provider);

    void intiEndpointCallRouteDefinition(RouteDefinition routeDefinition);

    AbstractExternalServiceProvider getProviderModel();

//    List<TransformerExecutionWrapper> getRequestTransformers();
//
//    List<TransformerExecutionWrapper> getResponseTransformers();

//    public JsonNode execute(Message message, Service service);

}
