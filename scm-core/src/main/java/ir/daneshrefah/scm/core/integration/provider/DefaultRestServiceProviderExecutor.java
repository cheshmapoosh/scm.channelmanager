package ir.daneshrefah.scm.core.integration.provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.service.HttpContentType;
import ir.daneshrefah.scm.common.service.ResourceService;
import ir.daneshrefah.scm.plugin.api.model.service.external.AbstractExternalService;
import ir.daneshrefah.scm.plugin.api.model.service.external.AbstractRestExternalServiceProviderExecutor;
import ir.daneshrefah.scm.plugin.api.model.service.external.parameter.Parameter;
import ir.daneshrefah.scm.plugin.api.model.service.external.rest.RestExternalService;
import ir.daneshrefah.scm.utils.string.StringUtils;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-07-14
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public final class DefaultRestServiceProviderExecutor extends AbstractRestExternalServiceProviderExecutor {

    public DefaultRestServiceProviderExecutor(ResourceService resourceService, ObjectMapper objectMapper) {
        super(resourceService, objectMapper);
    }

    @Override
    protected String extractHttpMethod(Message message) {
        RestExternalService service = extractRestService(message).get();
        if (Objects.nonNull(service.getHttpMethod())) {
            return service.getHttpMethod().getValue();
        }
        if (Objects.nonNull(service.getServiceProvider().getMetadata()) &&
                Objects.nonNull(service.getServiceProvider().getMetadata().getDefaultHttpMethod())) {
            return service.getServiceProvider().getMetadata().getDefaultHttpMethod().getValue();
        }
        return super.extractContentType(message);
    }

    @Override
    protected String extractContentType(Message message) {
        RestExternalService service = extractRestService(message).get();
        HttpContentType contentType = extractContentType(service);
        return null != contentType ? contentType.getValue() : super.extractContentType(message);
    }

    private HttpContentType extractContentType(RestExternalService service) {
        if (Objects.nonNull(service.getRequestContentType())) {
            return service.getRequestContentType();
        }
        if (Objects.nonNull(service.getServiceProvider().getMetadata()) &&
                Objects.nonNull(service.getServiceProvider().getMetadata().getDefaultRequestContentType())) {
            return service.getServiceProvider().getMetadata().getDefaultRequestContentType();
        }
        return null;
    }

    @Override
    protected Map<String, ?> extractAdditionalHeaders(Message message) {
        return super.extractAdditionalHeaders(message);
    }

    @Override
    protected String extractTargetUrl(Message message) {
        Optional<RestExternalService> service = extractRestService(message);
        String providerEndpoint = extractProviderEndpoint();
        return StringUtils.joinWith("/", providerEndpoint, service.get().getPath());
    }

    @Override
    protected Object extractServiceParametersBody(AbstractExternalService service, Object body) {
        RestExternalService restService = (RestExternalService) service;
        HttpContentType contentType = extractContentType(restService);
//        TODO dariush
        Parameter parameter = null;
        Optional parameterValue = extractParameterValue(parameter);
        return null;
    }

    private Optional<RestExternalService> extractRestService(Message message) {
        if (Objects.isNull(message) || Objects.isNull(message.getHeader()) || Objects.isNull(message.getHeader().getService()) ||
                !(message.getHeader().getService() instanceof RestExternalService)) {
            return Optional.empty();
        }
        return Optional.of((RestExternalService) message.getHeader().getService());
    }

}