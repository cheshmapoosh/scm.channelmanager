package ir.daneshrefah.scm.plugin.nab.provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.message.MessageOutput;
import ir.daneshrefah.scm.common.model.message.TcpMessageOutput;
import ir.daneshrefah.scm.common.model.service.HttpContentType;
import ir.daneshrefah.scm.common.service.ResourceService;
import ir.daneshrefah.scm.plugin.api.model.service.external.AbstractCamelExternalServiceProviderExecutor;
import ir.daneshrefah.scm.plugin.api.model.service.external.AbstractExternalService;
import ir.daneshrefah.scm.plugin.api.model.service.external.CustomExternalService;
import ir.daneshrefah.scm.common.model.service.parameter.Parameter;
import ir.daneshrefah.scm.plugin.nab.transformer.NabRequestTransformer;
import ir.daneshrefah.scm.plugin.nab.transformer.NabResponseTransformer;
import ir.daneshrefah.scm.utils.string.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-06-22
 */
@Component
public final class NabTcpServiceProvider extends AbstractCamelExternalServiceProviderExecutor {

    private static final String TCP_PREFIX = "netty4:tcp://";

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

    @Override
    protected Object extractServiceParametersRequestBody(AbstractExternalService service, Object body) {
        CustomExternalService externalService = (CustomExternalService) service;
//        HttpContentType contentType = extractContentType(externalService);
//        TODO dariush
        Parameter parameter = null;
        Optional parameterValue = extractParameterValue(parameter);
        return null;
    }

    @Override
    protected MessageOutput buildMessageOutput() {
        return TcpMessageOutput.builder().build();
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
