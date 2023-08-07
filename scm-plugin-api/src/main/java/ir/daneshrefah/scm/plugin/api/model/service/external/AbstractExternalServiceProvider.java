package ir.daneshrefah.scm.plugin.api.model.service.external;

import com.fasterxml.jackson.databind.JsonNode;
import ir.daneshrefah.scm.plugin.api.model.message.Message;
import ir.daneshrefah.scm.plugin.api.model.message.Status;
import ir.daneshrefah.scm.plugin.api.model.service.Service;
import ir.daneshrefah.scm.plugin.api.model.service.TransformerType;
import ir.daneshrefah.scm.plugin.api.transformer.AbstractTransformer;
import ir.daneshrefah.scm.plugin.api.utils.ClassLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-08-06
 */
public abstract class AbstractExternalServiceProvider<T> {

    protected final Logger LOGGER = LoggerFactory.getLogger(getClass());

    protected T metadata;
    protected ExternalServiceProvider externalServiceProvider;
//    public AbstractExternalServiceProvider(ExternalServiceProvider externalServiceProvider) {
//        this.externalServiceProvider = externalServiceProvider;
//        try {
//            initServerConfigs();
//        } catch (Exception e) {
//            LOGGER.error("error init server configs.", e);
//            throw new RuntimeException(e);
//        }
//
//    }

    public abstract void initServerConfigs();

    public void execute(Message message, Service service) {
        LocalDateTime startTime = LocalDateTime.now();
        boolean isSuccessful = true;
        String errorMessage = null;

        try {
            Object requestBody = transformRequest(message, service);
            Object responseBody = executeServiceComponent(message, requestBody, service);
            JsonNode response = transformResponse(message, responseBody, service);
            message.setPayload(response);
            message.setStatus(Status.SC_SUCCESS);
        } catch (Exception e) {
            isSuccessful = false;
            errorMessage = e.getMessage();
            throw e;
        } finally {
            LocalDateTime endTime = LocalDateTime.now();
            message.addServiceCallEvent(startTime, endTime,
                    service.getCode(),
                    externalServiceProvider.getCode(),
                    isSuccessful, errorMessage);
        }
    }

    protected JsonNode transformResponse(Message message, Object responseBody, Service service) {
        Object response = responseBody;
        AbstractTransformer responseTransformer = null;
        if (TransformerType.JAVA.equals(service.getResponseTransformerType())) {
            String className = service.getResponseTransformerClass();
            responseTransformer = ClassLoader.findBeanOrCreateInstanceOfClass(className, AbstractTransformer.class);

        }
        if (null != responseTransformer) {
            response = responseTransformer.transform(responseBody, message, service.getResponseTransformMetadata());
        }
        return (JsonNode) response;
    }

    protected abstract Object executeServiceComponent(Message message, Object requestBody, Service service);

    protected Object transformRequest(Message message, Service service) {
        Object request = message.getPayload();
        AbstractTransformer requestTransformer = TransformerType.JAVA.equals(service.getRequestTransformerType()) ?
                ClassLoader.createInstanceOfClass(service.getRequestTransformerClass(), AbstractTransformer.class) :
                null;
        if (null != requestTransformer) {
            request = requestTransformer.transform(message.getPayload(), message, service.getRequestTransformMetadata());
        }
        return request;
    }

    public void setExternalServiceProvider(ExternalServiceProvider externalServiceProvider) {
        this.externalServiceProvider = externalServiceProvider;
    }

}
