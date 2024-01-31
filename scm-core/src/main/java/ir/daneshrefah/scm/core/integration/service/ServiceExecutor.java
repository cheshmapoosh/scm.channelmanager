package ir.daneshrefah.scm.core.integration.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.message.Status;
import ir.daneshrefah.scm.common.model.service.Service;
import ir.daneshrefah.scm.logging.api.EventProducer;
import ir.daneshrefah.scm.logging.domain.event.Event;
import ir.daneshrefah.scm.logging.domain.event.EventType;
import ir.daneshrefah.scm.plugin.api.inbound.interceptor.MessageInterceptor;
import ir.daneshrefah.scm.plugin.api.integration.ErrorHandlerService;
import ir.daneshrefah.scm.plugin.api.transformer.TransformerExecutionWrapper;
import ir.daneshrefah.scm.utils.ClassUtils;
import ir.daneshrefah.scm.utils.MessageUtils;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Duration;
import java.time.Instant;
import java.util.Iterator;
import java.util.List;

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
    protected ObjectMapper objectMapper;
    @Setter
    private List<MessageInterceptor> requestInterceptors;
    @Setter
    private List<MessageInterceptor> responseInterceptors;

    public void executeService(Service service, Message message) {
        for (Iterator<MessageInterceptor> iterator = requestInterceptors.iterator(); iterator.hasNext(); ) {
            MessageInterceptor messageInterceptor = iterator.next();
            message = messageInterceptor.intercept(message);
            if (!MessageUtils.isContinueAllowed(message)) {
                return;
            }
        }

        Instant startTime = Instant.now();
        boolean isSuccessful = true;
        Exception exception = null;
        JsonNode response = null;

        try {
            response = executeInternal(service, message);
            message.payload(response);
        } catch (Exception e) {
            errorHandlerService.resolveMessageByException(message, e);
            exception = ClassUtils.cloneExceptionWithoutStackTrace(e);
            return;
        } finally {
            logServiceCallEvent(message, service, message.getPayload(), exception, startTime);
        }

        for (Iterator<MessageInterceptor> iterator = responseInterceptors.iterator(); iterator.hasNext(); ) {
            MessageInterceptor messageInterceptor = iterator.next();
            message = messageInterceptor.intercept(message);
            if (!MessageUtils.isContinueAllowed(message)) {
                return;
            }
        }

        if (null == response)
            message.nullPayload();
        else if (response.getClass().isAssignableFrom(JsonNode.class)) {
            message.payload((JsonNode) response);
        } else {
            /*try {
                JsonNode node = null;
                if (response instanceof String) {
                    node = objectMapper.readTree((String) response);
                } else {
                    node = objectMapper.valueToTree(response);
                }
                message.payload(node);
            } catch (JsonProcessingException e) {
                JsonNode node = objectMapper.valueToTree(response);
                message.payload(node);
//                throw new RuntimeException(e);
            }*/
        }
        if (Status.SC_PROCESSING.equals(message.getStatus())) {
            message.status(Status.SC_SUCCESS);
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

    public Object transformResponse(List<TransformerExecutionWrapper> transformerRelations, Message message, Object payload) {
        for (Iterator<TransformerExecutionWrapper> iterator = transformerRelations.iterator(); iterator.hasNext(); ) {
            TransformerExecutionWrapper transformerExecutionWrapper = iterator.next();
            payload = transformerExecutionWrapper.getTransformerInstance()
                    .transform(payload, message, transformerExecutionWrapper.getTransformerRelation().getMetadata());
        }
        return payload;
    }

    protected abstract JsonNode executeInternal(Service service, Message message);

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
