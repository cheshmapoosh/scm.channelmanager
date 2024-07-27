package ir.daneshrefah.scm.plugin.nab.provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.exception.InvalidInputException;
import ir.daneshrefah.scm.common.exception.MissingRequiredInputException;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.message.MessageOutput;
import ir.daneshrefah.scm.common.model.message.TcpMessageOutput;
import ir.daneshrefah.scm.common.model.service.parameter.Parameter;
import ir.daneshrefah.scm.common.service.ResourceService;
import ir.daneshrefah.scm.plugin.api.model.service.external.AbstractCamelExternalServiceProviderExecutor;
import ir.daneshrefah.scm.plugin.api.model.service.external.AbstractExternalService;
import ir.daneshrefah.scm.plugin.api.model.service.external.CustomExternalService;
import ir.daneshrefah.scm.plugin.nab.transformer.NabRequestTransformer;
import ir.daneshrefah.scm.plugin.nab.transformer.NabResponseTransformer;
import ir.daneshrefah.scm.utils.string.StringUtils;
import ir.daneshrefah.scm.utils.validation.ValidationUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.Iterator;
import java.util.List;
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
        List<Parameter> providerHeaders = externalService.getServiceProvider().getRequestHeaders();
        List<Parameter> providerRequestBody = externalService.getServiceProvider().getRequestBody();

        StringBuffer request = new StringBuffer();
        for (Iterator<Parameter> iterator = providerHeaders.iterator(); iterator.hasNext(); ) {
            Parameter parameter = iterator.next();
            request.append(adjustValue(parameter, extractValue(service, parameter)));
        }


        Parameter parameter = null;
        Optional parameterValue = extractParameterValue(parameter);
        return null;
    }

    private Object extractValue(AbstractExternalService service, Parameter parameter) {
        switch (parameter.getDatasource().getValue()) {
            case "COMMAND":
                return service.getRequestHeaderStaticValue("COMMAND");
//            case "SERVICE": // => ParameterDatasourceProperty.PROVIDER_TERMINAL_CODE
//                return StringUtils.randomAlphabetic(5);
            case "CM_USER_ID":
                return StringUtils.randomAlphanumeric(5);
            case "CM_PASSWORD":
                return StringUtils.randomAlphanumeric(5);
        }
        return extractParameterValue(parameter);
    }


    @Override
    protected MessageOutput buildMessageOutput() {
        return TcpMessageOutput.builder().build();
    }

    private StringBuffer adjustValue(Parameter parameter, Object value) {
        ValidationUtils.checkNull(parameter, () -> new MissingRequiredInputException("parameter"));
        Assert.notNull(parameter, "invalid parameter");
        if (parameter.isRequired()) {
            if (value == null || StringUtils.isEmpty(String.valueOf(value))) {
                throw new MissingRequiredInputException(parameter.getName());
            }
            if (parameter.getDatasource().getLength() < String.valueOf(value).length()) {
                throw new InvalidInputException(parameter.getName());
            }
//            try {
//                parameter.getDatasource().getConverter().convert(String.valueOf(value));
//            } catch (Exception e) {
//                throw new InvalidInputException(parameter.getName());
//            }
        }

        StringBuffer adjusted = new StringBuffer(String.valueOf(
                value == null ? StringUtils.EMPTY : value));
        if(adjusted.length() >= parameter.getDatasource().getLength()) {
            adjusted.delete(parameter.getDatasource().getLength(), adjusted.toString().length());
        }
        while (adjusted.length() < parameter.getDatasource().getLength()) {
            adjusted.append(StringUtils.SPACE);
        }
        return adjusted;
    }

}
