package ir.daneshrefah.scm.core.integration.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.ValidationMessage;
import ir.daneshrefah.scm.core.service.ErrorMappingService;
import ir.daneshrefah.scm.core.transformer.DynamicTransformer;
import ir.daneshrefah.scm.core.transformer.EmptyTransformer;
import ir.daneshrefah.scm.core.transformer.NullTransformer;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.message.Status;
import ir.daneshrefah.scm.common.model.service.Service;
import ir.daneshrefah.scm.common.model.service.TransformerType;
import ir.daneshrefah.scm.plugin.api.transformer.AbstractTransformer;
import ir.daneshrefah.scm.plugin.api.utils.ClassLoader;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-08-07
 */
public abstract class ServiceExecutor {

    @Autowired
    private ErrorMappingService errorMappingService;
    @Autowired
    private EmptyTransformer emptyTransformer;
    @Autowired
    private NullTransformer nullTransformer;
    @Autowired
    private DynamicTransformer dynamicTransformer;
    private final Map<String, ServiceExecutionWrapper> serviceExecutionMap = new HashMap<>();

    public void executeService(Service service, Message message) {

        ServiceExecutionWrapper serviceExecutionWrapper = null;
        serviceExecutionWrapper = serviceExecutionMap.get(service.getCode());
        if (null == serviceExecutionWrapper) {
            serviceExecutionWrapper = new ServiceExecutionWrapper(service);
            serviceExecutionMap.put(service.getCode(), serviceExecutionWrapper);
        }

        Optional<Set<ValidationMessage>> errors = serviceExecutionWrapper.validateRequestSchema(message);
        if (!errors.isEmpty()) {
            errorMappingService.resolveMessageByValidationMessage(message, errors.get());
            return;
        }

        Object requestPayload = null;
        try {
            requestPayload = transformRequest(service, message);
        } catch (Exception e) {
            errorMappingService.resolveMessageByException(message, e);
            return;
        }


        LocalDateTime startTime = LocalDateTime.now();
        boolean isSuccessful = true;
        Exception exception = null;
        Object response = null;

        try {
            response = executeInternal(service, message, requestPayload);
        } catch (Exception e) {
            errorMappingService.resolveMessageByException(message, e);
            isSuccessful = false;
            exception = ClassLoader.cloneExceptionWithoutStackTrace(e);
            return;
        } finally {
            LocalDateTime endTime = LocalDateTime.now();
            message.addServiceCallEvent(startTime, endTime,
                    service,
                    isSuccessful, exception, requestPayload, response);
        }

        try {
            response = transformResponse(service, message, response);
        } catch (Exception e) {
            errorMappingService.resolveMessageByException(message, e);
            return;
        }
        if (null == response)
            return;
        if (response.getClass().isAssignableFrom(JsonNode.class)) {
            message.setPayload((JsonNode) response);
        } else {
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                JsonNode node = null;
                if (response instanceof String) {
                    node = objectMapper.readTree((String) response);
                } else {
                    node = objectMapper.valueToTree(response);
                }
                message.setPayload(node);
            } catch (JsonProcessingException e) {
                JsonNode node = objectMapper.valueToTree(response);
                message.setPayload(node);
//                throw new RuntimeException(e);
            }
        }
        if (Status.SC_PROCESSING.equals(message.getStatus()) &&
                service.getId().equals(message.getHeader().getService().getTerminalServiceAccess().getService().getId())) {
            message.setStatus(Status.SC_SUCCESS);
        }
    }

    public Object transformRequest(Service service, Message message) {
        Object payload = message.getPayload();
        AbstractTransformer requestTransformer = getTransformer(service.getRequestTransformerType(),
                service.getRequestTransformerClass());
        if (null != requestTransformer) {
            payload = requestTransformer.transform(payload, message, service.getRequestTransformMetadata());
        }
        return payload;
    }

    public Object transformResponse(Service service, Message message, Object payload) {
        AbstractTransformer responseTransformer = getTransformer(service.getResponseTransformerType(),
                service.getResponseTransformerClass());
        if (null != responseTransformer) {
            payload = responseTransformer.transform(payload, message, service.getResponseTransformMetadata());
        }
        return payload;
    }

    protected AbstractTransformer getTransformer(TransformerType transformerType, String transformerClass) {
        if (TransformerType.JAVA.equals(transformerType)) {
            return ClassLoader.findBeanOrCreateInstanceOfClass(transformerClass, AbstractTransformer.class);
        } else if (TransformerType.EMPTY.equals(transformerType)) {
            return emptyTransformer;
        } else if (TransformerType.DYNAMIC.equals(transformerType)) {
            return dynamicTransformer;
        }
        return null;
    }

    protected abstract Object executeInternal(Service service, Message message, Object requestPayload);

}
