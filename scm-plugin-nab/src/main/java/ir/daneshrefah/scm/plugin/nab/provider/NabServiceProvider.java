package ir.daneshrefah.scm.plugin.nab.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.service.ResourceService;
import ir.daneshrefah.scm.plugin.api.model.service.external.AbstractExternalService;
import ir.daneshrefah.scm.plugin.api.model.service.external.AbstractRestExternalServiceProviderExecutor;
import ir.daneshrefah.scm.plugin.nab.transformer.NabRequestTransformer;
import ir.daneshrefah.scm.plugin.nab.transformer.NabResponseTransformer;
import ir.daneshrefah.scm.utils.string.StringUtils;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import static javax.swing.text.html.FormSubmitEvent.MethodType.POST;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-08-05
 */
@Component("nabCoreServiceProvider")
public final class NabServiceProvider extends AbstractRestExternalServiceProviderExecutor {

    private final NabRequestTransformer requestTransformer;
    private final NabResponseTransformer responseTransformer;

    public NabServiceProvider(ObjectMapper objectMapper, ResourceService resourceService, NabRequestTransformer requestTransformer, NabResponseTransformer responseTransformer) {
        super(resourceService, objectMapper);
        this.requestTransformer = requestTransformer;
        this.responseTransformer = responseTransformer;
    }

//    @Override
//    protected List<AbstractJsonTransformer> prepareRequestTransformers() {
//        return List.of(requestTransformer);
//    }
//
//    @Override
//    protected List<AbstractJsonTransformer> prepareResponseTransformers() {
//        return List.of(responseTransformer);
//    }

    @Override
    @SneakyThrows
    protected String extractTargetUrl(Message message) {
        AbstractExternalService service = (AbstractExternalService) message.getHeader().getService();
        String providerEndpoint = extractProviderEndpoint();
        JsonNode componentMetadata = service.getMetadata();
        String target = providerEndpoint + StringUtils.removeStart(componentMetadata.get("serviceName").asText(), "/");
        return target;
    }

    @Override
    protected String extractHttpMethod(Message message) {
        return POST.name();
    }
}
