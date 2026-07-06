package ir.daneshrefah.scm.task.event;

import ir.daneshrefah.scm.common.event.ScmEventPublisher;
import ir.daneshrefah.scm.common.event.ScmSafeEventAttributes;
import ir.daneshrefah.scm.common.event.provider.ScmProviderEvent;
import ir.daneshrefah.scm.common.event.provider.ScmProviderEventType;
import ir.daneshrefah.scm.common.model.message.Message;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

import static ir.daneshrefah.scm.utils.constant.Constants.SCM_PARAMETER_CLIENT_CORRELATION_ID;

@Component
@Slf4j
public class TaskProviderEventPublisher {
    private final ObjectProvider<ScmEventPublisher> eventPublisherProvider;

    public TaskProviderEventPublisher(ObjectProvider<ScmEventPublisher> eventPublisherProvider) {
        this.eventPublisherProvider = eventPublisherProvider;
    }

    public void publishProcess(
            ScmProviderEventType type,
            Exchange exchange,
            Long processId,
            Object processCode,
            Object status,
            String operationName,
            Throwable error
    ) {
        Map<String, Object> attributes = baseAttributes(exchange, operationName);
        put(attributes, "scm.task.process_id", processId);
        put(attributes, "scm.task.process_code", processCode);
        put(attributes, "scm.task.status", status);
        putError(attributes, error);
        publish(type, attributes);
    }

    public void publishTask(
            ScmProviderEventType type,
            Exchange exchange,
            Long taskId,
            Long processId,
            Object status,
            String operationName,
            Throwable error
    ) {
        Map<String, Object> attributes = baseAttributes(exchange, operationName);
        put(attributes, "scm.task.task_id", taskId);
        put(attributes, "scm.task.process_id", processId);
        put(attributes, "scm.task.status", status);
        putError(attributes, error);
        publish(type, attributes);
    }

    private Map<String, Object> baseAttributes(Exchange exchange, String operationName) {
        Map<String, Object> attributes = new LinkedHashMap<>();
        put(attributes, "scm.task.correlation_id", correlationId(exchange));
        put(attributes, "scm.task.operation_name", operationName);
        return attributes;
    }

    private String correlationId(Exchange exchange) {
        if (exchange == null) {
            return null;
        }
        String correlationId = exchange.getProperty(Message.CORRELATION_ID, String.class);
        if (correlationId != null && !correlationId.isBlank()) {
            return correlationId;
        }
        return exchange.getMessage().getHeader(SCM_PARAMETER_CLIENT_CORRELATION_ID, String.class);
    }

    private void putError(Map<String, Object> attributes, Throwable error) {
        if (error == null) {
            return;
        }
        put(attributes, "error.type", error.getClass().getSimpleName());
        put(attributes, "error.message", ScmSafeEventAttributes.sanitizeMessage(error.getMessage()));
    }

    private void put(Map<String, Object> attributes, String key, Object value) {
        if (value != null) {
            attributes.put(key, value);
        }
    }

    private void publish(ScmProviderEventType type, Map<String, Object> attributes) {
        ScmEventPublisher publisher = eventPublisherProvider.getIfAvailable();
        if (publisher != null && type != null) {
            try {
                publisher.publish(ScmProviderEvent.of(type, attributes));
            } catch (RuntimeException exception) {
                log.warn("event=TASK_PROVIDER_EVENT_PUBLISH_FAILED providerEventType={} "
                                + "outcome=ignored failureType={} failureMessage={}",
                        type.code(),
                        exception.getClass().getSimpleName(),
                        ScmSafeEventAttributes.sanitizeMessage(exception.getMessage()));
            }
        }
    }
}
