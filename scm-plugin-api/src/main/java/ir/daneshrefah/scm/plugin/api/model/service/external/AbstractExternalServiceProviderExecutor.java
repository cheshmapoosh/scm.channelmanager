package ir.daneshrefah.scm.plugin.api.model.service.external;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.service.AbstractExternalServiceProvider;
import ir.daneshrefah.scm.common.model.service.ExternalServiceRequestBodyType;
import ir.daneshrefah.scm.common.service.ResourceService;
import ir.daneshrefah.scm.plugin.api.model.service.external.parameter.Parameter;
import ir.daneshrefah.scm.plugin.api.service.ParameterDataProvider;
import ir.daneshrefah.scm.utils.string.StringUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.camel.model.RouteDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-02-30
 */
@RequiredArgsConstructor
public abstract class AbstractExternalServiceProviderExecutor implements ExternalServiceProviderExecutor {

    protected final Logger LOGGER = LoggerFactory.getLogger(getClass());
    protected static final String HEADER_ORIGINAL_MESSAGE = "ScmOriginalMessage";

    private final ResourceService resourceService;
    protected final ObjectMapper objectMapper;
    @Getter
    private AbstractExternalServiceProvider providerModel;

    public final void init(AbstractExternalServiceProvider provider) {
        this.providerModel = provider;
    }

    protected String extractProviderEndpoint() {
        if (null == providerModel || null == providerModel.getMetadata() || StringUtils.isEmpty(providerModel.getMetadata().getEndpoint())) {
            return null;
        }
        String result = resourceService.prepareProperties(providerModel.getMetadata().getEndpoint());
        return StringUtils.appendIfMissing(result, "/");
    }

    @Override
    public final void intiEndpointCallRouteDefinition(RouteDefinition routeDefinition) {
        routeDefinition.process(exchange -> {
            Message originalMessage = exchange.getProperty(HEADER_ORIGINAL_MESSAGE, Message.class);
            Object body = exchange.getMessage().getBody();
            AbstractExternalService service = (AbstractExternalService) originalMessage.getHeader().getService();
            switch (service.getRequestBodyType()) {
                case NONE:
                    exchange.getMessage().setBody(null);
                    break;
                case MESSAGE_BODY:
                    exchange.getMessage().setBody(body);
                    break;
                case PARAMETERS:
                    exchange.getMessage().setBody(extractServiceParametersRequestBody(service, body));
                    break;
            }
//            TODO dariush log sending request
        });
        intiEndpointCallRouteDefinitionInternal(routeDefinition);
        routeDefinition.process(exchange -> {
            Message originalMessage = exchange.getProperty(HEADER_ORIGINAL_MESSAGE, Message.class);
            AbstractExternalService service = (AbstractExternalService) originalMessage.getHeader().getService();
//            TODO dariush log receiving response
            if (ExternalServiceRequestBodyType.PARAMETERS.equals(service.getRequestBodyType())) {
                exchange.getMessage().setBody(extractServiceParametersResponseBody(service, exchange.getMessage().getBody()));
            }
        });
    }

    protected Object extractServiceParametersResponseBody(AbstractExternalService service, Object body) {
        return null;
    }

    protected Object extractServiceParametersRequestBody(AbstractExternalService service, Object body) {
        return null;
    }

    protected abstract void intiEndpointCallRouteDefinitionInternal(RouteDefinition routeDefinition);

//    public List<TransformerExecutionWrapper> getRequestTransformers() {
//        return Collections.emptyList();
//    }
//
//    public List<TransformerExecutionWrapper> getResponseTransformers() {
//        return Collections.emptyList();
//    }

    protected Optional<Object> extractParameterValue(Parameter parameter) {
        return ParameterDataProvider.getInstance().extractParameterValue(parameter);
    }

//    public abstract void invokeTargetEndpoint(RouteDefinition routeDefinition);

}
