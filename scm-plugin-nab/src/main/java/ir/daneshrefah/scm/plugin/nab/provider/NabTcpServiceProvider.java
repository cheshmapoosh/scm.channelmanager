package ir.daneshrefah.scm.plugin.nab.provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.service.ResourceService;
import ir.daneshrefah.scm.plugin.api.model.service.external.AbstractCamelExternalServiceProviderExecutor;
import ir.daneshrefah.scm.plugin.api.transformer.AbstractJsonTransformer;
import ir.daneshrefah.scm.plugin.api.transformer.AbstractTransformer;
import ir.daneshrefah.scm.plugin.nab.transformer.NabRequestTransformer;
import ir.daneshrefah.scm.plugin.nab.transformer.NabResponseTransformer;
import ir.daneshrefah.scm.plugin.nab.transformer.NabTcpRequestTransformer;
import ir.daneshrefah.scm.plugin.nab.transformer.NabTcpResponseTransformer;
import ir.daneshrefah.scm.utils.string.StringUtils;
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

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

    public NabTcpServiceProvider(ProducerTemplate producerTemplate, CamelContext camelContext, ObjectMapper objectMapper, ResourceService resourceService, NabRequestTransformer requestTransformer, NabResponseTransformer responseTransformer) {
        super(producerTemplate, camelContext, resourceService, objectMapper);
    }

    @Override
    protected String extractTargetUrl(Message message) {
        String providerEndpoint = extractProviderEndpoint();
        if (StringUtils.startsWithIgnoreCase(providerEndpoint, TCP_PREFIX)) {
            providerEndpoint = TCP_PREFIX + providerEndpoint;
        }
        return providerEndpoint;
    }

    @Override
    protected List<AbstractTransformer> prepareRequestTransformers() {
        return List.of(new NabTcpRequestTransformer());
    }

    @Override
    protected List<AbstractJsonTransformer> prepareResponseTransformers() {
        return List.of(new NabTcpResponseTransformer());
    }

}
