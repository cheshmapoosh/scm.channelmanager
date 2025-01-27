package ir.daneshrefah.scm.plugin.sayad.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.message.MessageOutput;
import ir.daneshrefah.scm.common.model.service.HttpContentType;
import ir.daneshrefah.scm.common.model.service.HttpMethod;
import ir.daneshrefah.scm.common.service.ResourceService;
import ir.daneshrefah.scm.common.service.ServiceService;
import ir.daneshrefah.scm.plugin.api.model.service.external.AbstractExternalService;
import ir.daneshrefah.scm.plugin.api.model.service.external.povider.executor.AbstractBaseRestExternalServiceProviderExecutor;
import ir.daneshrefah.scm.utils.string.StringUtils;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-04-08
 */
@Component
public final class SayadServiceProvider extends AbstractBaseRestExternalServiceProviderExecutor {


    public SayadServiceProvider(ObjectMapper objectMapper, ResourceService resourceService, ServiceService serviceService) {
        super(objectMapper, resourceService, serviceService);
    }

    @Override
    protected Optional<Map<String, ?>> extractRequestHeaders(Message message) {
        return Optional.empty();
    }

    @Override
    protected Optional<Map<String, ?>> extractResponseHeaders(Message message) {
        return Optional.empty();
    }

    @Override
    protected HttpMethod extractHttpMethod(Message message) {
        return null;
    }

    @Override
    protected HttpContentType extractContentType(Message message) {
        return null;
    }

    @Override
    @SneakyThrows
    protected String extractTargetUrl(Message message) {
        return getProviderEndpoint().orElse(null);
    }

    @Override
    protected Optional<String> extractQueryString(Message message) {
        return Optional.empty();
    }

    @Override
    protected Object extractServiceParametersResponseBody(Message message, Object body) {
        return null;
    }

    @Override
    protected Object extractServiceParametersRequestBody(Message message, Object body, MessageOutput messageOutput) {
        return null;
    }

//    @Override
//    protected List<AbstractJsonTransformer> prepareRequestTransformers() {
//        return List.of(new SayadChequeInfoRequestTransformer());
//    }
//
//    @Override
//    protected List<AbstractJsonTransformer> prepareResponseTransformers() {
//        return List.of(new SayadChequeInfoResponseTransformer());
//    }

}
