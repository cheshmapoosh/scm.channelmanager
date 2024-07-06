package ir.daneshrefah.scm.plugin.api.model.service.external;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.service.ResourceService;
import ir.daneshrefah.scm.plugin.api.transformer.AbstractJsonTransformer;
import ir.daneshrefah.scm.plugin.api.transformer.AbstractTransformer;
import ir.daneshrefah.scm.utils.string.StringUtils;
import lombok.SneakyThrows;
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;

import java.util.List;

import static javax.swing.text.html.FormSubmitEvent.MethodType.POST;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-06-05
 */
public class DefaultRestServiceProvider extends AbstractRestExternalServiceProvider {

    private List<AbstractJsonTransformer> requestTransformers;
    private List<AbstractJsonTransformer> responseTransformers;

    public DefaultRestServiceProvider(ProducerTemplate producerTemplate, CamelContext camelContext, ResourceService resourceService, ObjectMapper objectMapper) {
        super(producerTemplate, camelContext, resourceService, objectMapper);
    }

    @Override
    protected List<? extends AbstractTransformer> prepareRequestTransformers() {
        return requestTransformers;
    }

    @Override
    protected List<AbstractJsonTransformer> prepareResponseTransformers() {
        return responseTransformers;
    }

    @Override
    @SneakyThrows
    protected String prepareTargetUrl(Message message) {
        ExternalService service = (ExternalService) message.getHeader().getService();
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
