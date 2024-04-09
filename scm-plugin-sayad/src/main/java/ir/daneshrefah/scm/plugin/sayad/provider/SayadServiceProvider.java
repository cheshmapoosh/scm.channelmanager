package ir.daneshrefah.scm.plugin.sayad.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.service.ResourceService;
import ir.daneshrefah.scm.plugin.api.model.service.external.AbstractRestExternalServiceProvider;
import ir.daneshrefah.scm.plugin.api.model.service.external.ExternalService;
import ir.daneshrefah.scm.plugin.api.transformer.AbstractTransformer;
import ir.daneshrefah.scm.plugin.sayad.transformer.SayadChequeInfoRequestTransformer;
import ir.daneshrefah.scm.plugin.sayad.transformer.SayadChequeInfoResponseTransformer;
import ir.daneshrefah.scm.utils.string.StringUtils;
import lombok.SneakyThrows;
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-04-08
 */
@Component
public final class SayadServiceProvider extends AbstractRestExternalServiceProvider {

    public SayadServiceProvider(ProducerTemplate producerTemplate, CamelContext camelContext,
                                  ResourceService resourceService, ObjectMapper objectMapper) {
        super(producerTemplate, camelContext, resourceService, objectMapper);
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
    protected List<AbstractTransformer> prepareRequestTransformers() {
        return List.of(new SayadChequeInfoRequestTransformer());
    }

    @Override
    protected List<AbstractTransformer> prepareResponseTransformers() {
        return List.of(new SayadChequeInfoResponseTransformer());
    }

}
