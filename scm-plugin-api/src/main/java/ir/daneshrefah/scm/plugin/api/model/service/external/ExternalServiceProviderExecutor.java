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

    void init(AbstractExternalServiceProvider provider);

    void intiEndpointCallRouteDefinition(RouteDefinition routeDefinition);

    AbstractExternalServiceProvider getProviderModel();

//    List<TransformerExecutionWrapper> getRequestTransformers();
//
//    List<TransformerExecutionWrapper> getResponseTransformers();

//    public JsonNode execute(Message message, Service service);

}
