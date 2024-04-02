package ir.daneshrefah.scm.plugin.nab.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.service.ExternalServiceProviderMetadata;
import ir.daneshrefah.scm.common.service.ServiceService;
import ir.daneshrefah.scm.plugin.api.model.service.external.AbstractRestExternalServiceProvider;
import ir.daneshrefah.scm.plugin.api.model.service.external.ExternalService;
import ir.daneshrefah.scm.plugin.api.transformer.AbstractTransformer;
import ir.daneshrefah.scm.plugin.nab.transformer.NabRequestTransformer;
import ir.daneshrefah.scm.plugin.nab.transformer.NabResponseTransformer;
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
 * @since 2023-08-05
 */
@Component("nabCoreServiceProvider")
public final class NabServiceProvider extends AbstractRestExternalServiceProvider {

    private static final String PROVIDER_CODE = "NAB";

    private final NabRequestTransformer requestTransformer;
    private final NabResponseTransformer responseTransformer;

    public NabServiceProvider(ServiceService serviceService, ProducerTemplate producerTemplate, CamelContext camelContext, ObjectMapper objectMapper, NabRequestTransformer requestTransformer, NabResponseTransformer responseTransformer) {
        super(serviceService, producerTemplate, camelContext, objectMapper);
        this.requestTransformer = requestTransformer;
        this.responseTransformer = responseTransformer;
    }

    @Override
    protected List<AbstractTransformer> prepareRequestTransformers() {
        return List.of(requestTransformer);
    }

    @Override
    protected List<AbstractTransformer> prepareResponseTransformers() {
        return List.of(responseTransformer);
    }

    @Override
    @SneakyThrows
    protected String prepareTargetUrl(Message message) {
        ExternalService service = (ExternalService) message.getHeader().getServiceAccess().getService();
        ExternalServiceProviderMetadata providerMetadata = getProvider().getMetadata();
        JsonNode componentMetadata = service.getMetadata();
        String target = StringUtils.appendIfMissing(providerMetadata.getEndpoint(), "/") + componentMetadata.get("serviceName").asText();
        return target;
//        return "https://dummy.restapiexample.com/api/v1/employees";
//            endpointUri = "http://${host}/Service/";
//            Map<String, String> values = new HashMap<>();
//            values.put("host", "scm-core.daneshrefah.ir");
//            StringSubstitutor.replace(endpointUri, values);
    }

    @Override
    public String extractProviderCode() {
        return PROVIDER_CODE;
    }

}
