package ir.daneshrefah.scm.core.integration.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.ValidationMessage;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.message.Status;
import ir.daneshrefah.scm.common.model.service.Service;
import ir.daneshrefah.scm.common.model.transformer.TransformerRelation;
import ir.daneshrefah.scm.common.model.transformer.TransformerRelationType;
import ir.daneshrefah.scm.core.service.TransformerService;
import ir.daneshrefah.scm.logging.api.EventProducer;
import ir.daneshrefah.scm.logging.domain.event.Event;
import ir.daneshrefah.scm.logging.domain.event.EventType;
import ir.daneshrefah.scm.plugin.api.integration.ErrorHandlerService;
import ir.daneshrefah.scm.plugin.api.transformer.TransformerExecutionWrapper;
import ir.daneshrefah.scm.utils.ClassUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-08-07
 */
public abstract class ServiceExecutor {

    @Autowired
    protected ErrorHandlerService errorHandlerService;
    @Autowired
    protected TransformerService transformerService;
    @Autowired
    protected ObjectMapper objectMapper;

    private final Map<String, ServiceExecutionWrapper> serviceExecutionMap = new HashMap<>();

    public void executeService(Service service, Message message) {

        Instant startTime = Instant.now();
        boolean isSuccessful = true;
        Exception exception = null;
        Object response = null;

        ServiceExecutionWrapper serviceExecutionWrapper = serviceExecutionMap.get(service.getCode());
        if (null == serviceExecutionWrapper) {
            serviceExecutionWrapper = new ServiceExecutionWrapper(service);
            List<TransformerRelation> transformerRelations = transformerService.findAllTransformerRelationsBySource(
                    service.getId());
            serviceExecutionWrapper.setRequestTransformers(transformerRelations.stream().filter(
                            t -> TransformerRelationType.SERVICE_REQUEST.equals(t.getRelationType()))
                    .map(t -> new TransformerExecutionWrapper(t))
                    .collect(Collectors.toList()));
            serviceExecutionWrapper.setResponseTransformers(transformerRelations.stream().filter(
                            t -> TransformerRelationType.SERVICE_RESPONSE.equals(t.getRelationType()))
                    .map(t -> new TransformerExecutionWrapper(t))
                    .collect(Collectors.toList()));
            serviceExecutionMap.put(service.getCode(), serviceExecutionWrapper);
        }

        Optional<Set<ValidationMessage>> errors = serviceExecutionWrapper.validateRequestSchema(message);
        if (!errors.isEmpty()) {
            errorHandlerService.resolveMessageByValidationMessage(message, errors.get());
            logServiceCallEvent(message, service, errors, null, startTime);
//            addServiceCallEvent(message, service, false, startTime, exception, message.getPayload(), response);
            return;
        }

        Object requestPayload = null;
        try {
            requestPayload = transformRequest(serviceExecutionWrapper.getRequestTransformers(), message);
        } catch (Exception e) {
            errorHandlerService.resolveMessageByException(message, e);
            return;
        }


        try {
            response = executeInternal(service, message, requestPayload);
        } catch (Exception e) {
            errorHandlerService.resolveMessageByException(message, e);
            exception = ClassUtils.cloneExceptionWithoutStackTrace(e);
            return;
        } finally {
            logServiceCallEvent(message, service, message.getPayload(), exception, startTime);
        }

        try {
            response = transformResponse(serviceExecutionWrapper.getResponseTransformers(), message, response);
        } catch (Exception e) {
            errorHandlerService.resolveMessageByException(message, e);
            return;
        }
        if (null == response)
            message.nullPayload();
        else if (response.getClass().isAssignableFrom(JsonNode.class)) {
            message.setPayload((JsonNode) response);
        } else {
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
        if (Status.SC_PROCESSING.equals(message.getStatus()) /*&&
                service.getId().equals(message.getHeader().getService().getTerminalServiceAccess().getService().getId())*/) {
            message.setStatus(Status.SC_SUCCESS);
        }
    }

    public Object transformRequest(List<TransformerExecutionWrapper> transformerRelations, Message message) {
        Object payload = message.getPayload();
        for (Iterator<TransformerExecutionWrapper> iterator = transformerRelations.iterator(); iterator.hasNext(); ) {
            TransformerExecutionWrapper transformerExecutionWrapper = iterator.next();
            payload = transformerExecutionWrapper.getTransformerInstance()
                    .transform(payload, message, transformerExecutionWrapper.getTransformerRelation().getMetadata());

        }
        return payload;
    }

    public Object transformRequest2(Service service, Message message) {
        Object payload = message.getPayload();
//        AbstractTransformer requestTransformer = getTransformer(service.getRequestTransformerType(),
//                service.getRequestTransformerClass());
//        if (null != requestTransformer) {
//            payload = requestTransformer.transform(payload, message, service.getRequestTransformMetadata());
//        }
        return payload;
    }

    public Object transformResponse(List<TransformerExecutionWrapper> transformerRelations, Message message, Object payload) {
        for (Iterator<TransformerExecutionWrapper> iterator = transformerRelations.iterator(); iterator.hasNext(); ) {
            TransformerExecutionWrapper transformerExecutionWrapper = iterator.next();
            payload = transformerExecutionWrapper.getTransformerInstance()
                    .transform(payload, message, transformerExecutionWrapper.getTransformerRelation().getMetadata());
        }
        return payload;
    }

    public Object transformResponse2(Service service, Message message, Object payload) {
//        AbstractTransformer responseTransformer = getTransformer(service.getResponseTransformerType(),
//                service.getResponseTransformerClass());
//        if (null != responseTransformer) {
//            payload = responseTransformer.transform(payload, message, service.getResponseTransformMetadata());
//        }
        return payload;
    }

    /*protected AbstractTransformer getTransformer(TransformerType transformerType, String transformerClass) {
        if (TransformerType.JAVA.equals(transformerType)) {
            return ClassLoader.findBeanOrCreateInstanceOfClass(transformerClass, AbstractTransformer.class);
        } else if (TransformerType.EMPTY.equals(transformerType)) {
            return emptyTransformer;
        } else if (TransformerType.DYNAMIC.equals(transformerType)) {
            return dynamicTransformer;
        }
        return null;
    }*/

    protected abstract Object executeInternal(Service service, Message message, Object requestPayload);

    private void logServiceCallEvent(Message message, Service service, Object output, Exception error, Instant startTime) {
        Instant endTime = Instant.now();
        Event event = Event.builder()
                .type(EventType.SERVICE_CALL)
                .status(message.getStatus())
                .correlationId(message.getHeader().getCorrelationId())
                .source(service.getCode())
                .terminalCode(message.getHeader().getServiceAccess().getTerminal().getCode())
                .channelCode(message.getHeader().getChannel().getCode())
                .startTime(startTime)
                .endTime(endTime)
                .durationMillis(Duration.between(startTime, endTime).toMillis())
                .threadName(Thread.currentThread().getName())
                .input(service.getCode())
                .output(output)
                .error(error)
                .sourceClassName(this.getClass().getSimpleName())
                .build();
        EventProducer.getInstance().sendEvent(event);
    }
}
