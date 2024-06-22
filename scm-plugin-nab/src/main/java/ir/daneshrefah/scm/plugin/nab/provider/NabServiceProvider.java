package ir.daneshrefah.scm.plugin.nab.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.service.ResourceService;
import ir.daneshrefah.scm.plugin.api.model.service.external.AbstractRestExternalServiceProvider;
import ir.daneshrefah.scm.plugin.api.model.service.external.ExternalService;
import ir.daneshrefah.scm.plugin.api.transformer.AbstractJsonTransformer;
import ir.daneshrefah.scm.plugin.nab.transformer.NabRequestTransformer;
import ir.daneshrefah.scm.plugin.nab.transformer.NabResponseTransformer;
import ir.daneshrefah.scm.utils.string.StringUtils;
import lombok.SneakyThrows;
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

import static javax.swing.text.html.FormSubmitEvent.MethodType.POST;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-08-05
 */
@Component("nabCoreServiceProvider")
public final class NabServiceProvider extends AbstractRestExternalServiceProvider {

    private final NabRequestTransformer requestTransformer;
    private final NabResponseTransformer responseTransformer;

    public NabServiceProvider(ProducerTemplate producerTemplate, CamelContext camelContext, ObjectMapper objectMapper, ResourceService resourceService, NabRequestTransformer requestTransformer, NabResponseTransformer responseTransformer) {
        super(producerTemplate, camelContext, resourceService, objectMapper);
        this.requestTransformer = requestTransformer;
        this.responseTransformer = responseTransformer;
    }

    @Override
    protected List<AbstractJsonTransformer> prepareRequestTransformers() {
        return List.of(requestTransformer);
    }

    @Override
    protected List<AbstractJsonTransformer> prepareResponseTransformers() {
        return List.of(responseTransformer);
    }

    @Override
    @SneakyThrows
    protected String prepareTargetUrl(Message message) {
        ExternalService service = (ExternalService) message.getHeader().getServiceAccess().getService();
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
