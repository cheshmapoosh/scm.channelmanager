package ir.daneshrefah.scm.plugin.nab.provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.service.ResourceService;
import ir.daneshrefah.scm.plugin.api.model.service.external.AbstractCamelExternalServiceProviderExecutor;
import ir.daneshrefah.scm.plugin.nab.transformer.NabRequestTransformer;
import ir.daneshrefah.scm.plugin.nab.transformer.NabResponseTransformer;
import ir.daneshrefah.scm.utils.string.StringUtils;
import org.springframework.stereotype.Component;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-06-22
 */
@Component
public final class NabTcpServiceProvider extends AbstractCamelExternalServiceProviderExecutor {

    private static final String TCP_PREFIX = "tcp://";

    public NabTcpServiceProvider(ObjectMapper objectMapper, ResourceService resourceService, NabRequestTransformer requestTransformer, NabResponseTransformer responseTransformer) {
        super(resourceService, objectMapper);
    }

    @Override
    protected String extractTargetEndpointUrl(Message message) {
        String providerEndpoint = extractProviderEndpoint();
        if (StringUtils.startsWithIgnoreCase(providerEndpoint, TCP_PREFIX)) {
            providerEndpoint = TCP_PREFIX + providerEndpoint;
        }
        return providerEndpoint;
    }

//    @Override
//    protected List<AbstractTransformer> prepareRequestTransformers() {
//        return List.of(new NabTcpRequestTransformer());
//    }
//
//    @Override
//    protected List<AbstractJsonTransformer> prepareResponseTransformers() {
//        return List.of(new NabTcpResponseTransformer());
//    }

}
