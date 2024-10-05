package ir.daneshrefah.scm.plugin.nab.provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.exception.InvalidInputException;
import ir.daneshrefah.scm.common.exception.MissingRequiredInputException;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.message.MessageOutput;
import ir.daneshrefah.scm.common.model.message.TcpMessageOutput;
import ir.daneshrefah.scm.common.model.service.parameter.Parameter;
import ir.daneshrefah.scm.common.model.service.parameter.ParameterActionType;
import ir.daneshrefah.scm.common.service.ResourceService;
import ir.daneshrefah.scm.common.service.ServiceService;
import ir.daneshrefah.scm.plugin.api.model.service.external.AbstractCamelExternalServiceProviderExecutor;
import ir.daneshrefah.scm.plugin.api.model.service.external.AbstractExternalService;
import ir.daneshrefah.scm.plugin.api.model.service.external.CustomExternalService;
import ir.daneshrefah.scm.utils.string.StringUtils;
import ir.daneshrefah.scm.utils.validation.ValidationUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
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

    public NabTcpServiceProvider(ObjectMapper objectMapper, ResourceService resourceService, ServiceService serviceService) {
        super(resourceService, serviceService, objectMapper);
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
    protected Object extractServiceParametersRequestBody(Message message, Object body, MessageOutput messageOutput) {
        AbstractExternalService service = (AbstractExternalService) message.getHeader().getService();
        CustomExternalService externalService = (CustomExternalService) service;
        StringBuffer request = new StringBuffer();

        for (MessageHeaderFields headerField : MessageHeaderFields.values()) {
            request.append(adjustValue(headerField.getLength(), true, headerField.name(),
                    prepareMessageHeaderField(headerField, message, body, messageOutput)));
        }

        List<Parameter> providerRequestHeaders = externalService.getServiceProvider().getRequestHeaders();
        List<Parameter> providerRequestBody = externalService.getServiceProvider().getRequestBody();
        List<Parameter> serviceRequestHeaders = externalService.getParameters(ParameterActionType.REQUEST_HEADER);
        List<Parameter> serviceRequestBody = externalService.getParameters(ParameterActionType.REQUEST_BODY);

        appendParameterToRequest(request, message, providerRequestHeaders);
        appendParameterToRequest(request, message, providerRequestBody);
        appendParameterToRequest(request, message, serviceRequestHeaders);
        appendParameterToRequest(request, message, serviceRequestBody);

        Parameter parameter = null;
        Optional parameterValue = extractParameterValue(message, parameter);
        return null;
    }

    private String prepareMessageHeaderField(MessageHeaderFields headerField, Message message, Object body, MessageOutput messageOutput) {
        AbstractExternalService service = (AbstractExternalService) message.getHeader().getService();
        if (Objects.isNull(headerField)) {
            return null;
        }

        Optional<String> value = Optional.empty();
        switch (headerField) {
            case COMMAND:
                value = service.getRequestHeaderStaticValue(headerField.name());
                break;
            case SERVICE:
                value = prepareTerminalCode(service, "99");
                break;
            case DATE_TIME:
                return null;
            case CM_USER_ID:
                return null;
            case CM_PASSWORD:
                return null;
            case RQUID:
                return messageOutput.getExternalCorrelationId();
        }
        return value.orElseThrow(() -> new RuntimeException("Could not set mandatory field: " + headerField.name()));
    }

    private void appendParameterToRequest(StringBuffer request, Message message, List<Parameter> providerHeaders) {
        for (Iterator<Parameter> iterator = providerHeaders.iterator(); iterator.hasNext(); ) {
            Parameter parameter = iterator.next();
            if (!parameter.isInternal()) {
                request.append(adjustValue(parameter, extractParameterValue(message, parameter)));
            }
        }
    }

    @Override
    protected String extractProviderCorrelationId(Message message) {
        return StringUtils.randomNumeric(MessageHeaderFields.RQUID.getLength());
    }

    @Override
    protected MessageOutput buildMessageOutput() {
        return TcpMessageOutput.builder().build();
    }

    private StringBuffer adjustValue(Parameter parameter, Object value) {
        int length = parameter.getDatasource().getLength();
        boolean isRequired = parameter.isRequired();
        String name = parameter.getName();
        ValidationUtils.checkNull(parameter, () -> new MissingRequiredInputException("parameter"));
        Assert.notNull(parameter, "invalid parameter");
        return adjustValue(length, isRequired, name, value);
    }

    private StringBuffer adjustValue(int length, boolean isRequired, String name, Object value) {
        if (isRequired) {
            if (value == null || StringUtils.isEmpty(String.valueOf(value))) {
                throw new MissingRequiredInputException(name);
            }
            if (length < String.valueOf(value).length()) {
                throw new InvalidInputException(name);
            }
//            try {
//                parameter.getDatasource().getConverter().convert(String.valueOf(value));
//            } catch (Exception e) {
//                throw new InvalidInputException(parameter.getName());
//            }
        }

        StringBuffer adjusted = new StringBuffer(String.valueOf(
                value == null ? StringUtils.EMPTY : value));
        if (adjusted.length() >= length) {
            adjusted.delete(length, adjusted.toString().length());
        }
        while (adjusted.length() < length) {
            adjusted.append(StringUtils.SPACE);
        }
        return adjusted;
    }

}
