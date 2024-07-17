package ir.daneshrefah.scm.plugin.sayad.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.service.ResourceService;
import ir.daneshrefah.scm.plugin.api.model.service.external.AbstractExternalService;
import ir.daneshrefah.scm.plugin.api.model.service.external.AbstractRestExternalServiceProviderExecutor;
import ir.daneshrefah.scm.utils.string.StringUtils;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-04-08
 */
@Component
public final class SayadServiceProvider extends AbstractRestExternalServiceProviderExecutor {

    public SayadServiceProvider(ResourceService resourceService, ObjectMapper objectMapper) {
        super(resourceService, objectMapper);
    }

    @Override
    @SneakyThrows
    protected String extractTargetUrl(Message message) {
        AbstractExternalService service = (AbstractExternalService) message.getHeader().getService();
        String providerEndpoint = extractProviderEndpoint();
        JsonNode componentMetadata = service.getMetadata();
        String target = providerEndpoint + StringUtils.removeStart(componentMetadata.get("serviceName").asText(), "/");
        return target;
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
