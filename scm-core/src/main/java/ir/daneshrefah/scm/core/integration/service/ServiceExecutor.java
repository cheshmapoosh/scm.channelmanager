package ir.daneshrefah.scm.core.integration.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.core.service.ErrorMappingService;
import ir.daneshrefah.scm.plugin.api.exception.BaseException;
import ir.daneshrefah.scm.plugin.api.model.message.Error;
import ir.daneshrefah.scm.plugin.api.model.message.Message;
import ir.daneshrefah.scm.plugin.api.model.message.Status;
import ir.daneshrefah.scm.plugin.api.model.service.Service;
import ir.daneshrefah.scm.plugin.api.model.service.TransformerType;
import ir.daneshrefah.scm.plugin.api.transformer.AbstractTransformer;
import ir.daneshrefah.scm.plugin.api.utils.ClassLoader;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

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

    public void executeService(Service service, Message message) {

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
        AbstractTransformer requestTransformer =  getTransformer(service.getRequestTransformerType(),
                service.getRequestTransformerClass());
        if (null != requestTransformer) {
            payload = requestTransformer.transform(payload, message, service.getRequestTransformMetadata());
        }
        return payload;
    }

    public Object transformResponse(Service service, Message message, Object payload) {
        AbstractTransformer responseTransformer =  getTransformer(service.getResponseTransformerType(),
                service.getResponseTransformerClass());
        if (null != responseTransformer) {
            payload = responseTransformer.transform(payload, message, service.getResponseTransformMetadata());
        }
        return payload;
    }

    public AbstractTransformer getResponseTransformer(Service service) {
        return getTransformer(service.getResponseTransformerType(), service.getResponseTransformerClass());
    }

    private AbstractTransformer getTransformer(TransformerType transformerType, String responseTransformerClass) {
        if (TransformerType.JAVA.equals(transformerType)) {
            String className = responseTransformerClass;
            return ClassLoader.findBeanOrCreateInstanceOfClass(className, AbstractTransformer.class);

        }
        return null;
    }

    protected abstract Object executeInternal(Service service, Message message, Object requestPayload);

}
