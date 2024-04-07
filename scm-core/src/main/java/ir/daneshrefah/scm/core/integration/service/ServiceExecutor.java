package ir.daneshrefah.scm.core.integration.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.message.MessageStatus;
import ir.daneshrefah.scm.common.model.service.Service;
import ir.daneshrefah.scm.logging.api.EventProducer;
import ir.daneshrefah.scm.logging.domain.event.Event;
import ir.daneshrefah.scm.logging.domain.event.ServiceEvent;
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
        Instant startTime = Instant.now();
        Exception exception = null;
        JsonNode request = null;
        try {
            request = message.getPayload();
            executeServiceInternal(service, message);
        } catch (Exception e) {
            errorHandlerService.resolveMessageByException(message, e);
            exception = ClassUtils.cloneExceptionWithoutStackTrace(e);
            return;
        } finally {
            logServiceCallEvent(message, service, request, exception, startTime);
        }
    }

    private void executeServiceInternal(Service service, Message message) throws Exception {
        for (Iterator<MessageInterceptor> iterator = requestInterceptors.iterator(); iterator.hasNext(); ) {
            MessageInterceptor messageInterceptor = iterator.next();
            message = messageInterceptor.intercept(message);
            if (!MessageUtils.isContinueAllowed(message)) {
                return;
            }
        }

        JsonNode response = null;

        response = executeInternal(service, message);
        message.payload(response);

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
        if (MessageStatus.SC_PROCESSING.equals(message.getStatus())) {
            message.status(MessageStatus.SC_SUCCESS);
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

    protected abstract JsonNode executeInternal(Service service, Message message) throws Exception;

    private void logServiceCallEvent(Message message, Service service, Object input, Exception exception, Instant startTime) {
        Instant endTime = Instant.now();
        String username = MessageUtils.getUsername(message);
        String cspUsername = MessageUtils.getCSPUsername(message);
        String requestBody = null != input ? input.toString() : null;
        String responseBody = null != message.getPayload() ? message.getPayload().toString() : null;
        Event event = ServiceEvent.builder()
                .correlationId(message.getHeader().getCorrelationId())
                .terminalCode(message.getHeader().getTerminalCode())
                .channelCode(message.getHeader().getChannel().getCode())
                .username(username)
                .cspUsername(cspUsername)
                .error(exception)
                .exceptionClassName(null != exception ? exception.getClass().getName() : null)
                .threadName(Thread.currentThread().getName())
                .startTime(startTime)
                .serviceCode(service.getCode())
                .request(requestBody)
                .response(responseBody)
                .endTime(endTime)
                .durationMillis(Duration.between(startTime, endTime).toMillis())
                .status(message.getStatus())
                .build();
        EventProducer.getInstance().sendEvent(event);
    }
}
