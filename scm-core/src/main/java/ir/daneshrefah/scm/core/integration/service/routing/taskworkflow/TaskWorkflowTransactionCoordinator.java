package ir.daneshrefah.scm.core.integration.service.routing.taskworkflow;

import ir.daneshrefah.scm.common.event.ScmEventPublisher;
import ir.daneshrefah.scm.common.event.ScmSafeEventAttributes;
import ir.daneshrefah.scm.common.event.provider.ScmProviderEvent;
import ir.daneshrefah.scm.common.event.provider.ScmProviderEventType;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.core.integration.service.routing.ChainStepDecision;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
@Slf4j
public class TaskWorkflowTransactionCoordinator {
    private final ObjectProvider<ScmEventPublisher> eventPublisherProvider;
    private final TaskWorkflowExecutionStore executionStore;

    public TaskWorkflowTransactionCoordinator(
            ObjectProvider<ScmEventPublisher> eventPublisherProvider,
            ObjectProvider<TaskWorkflowExecutionStore> executionStoreProvider
    ) {
        this.eventPublisherProvider = eventPublisherProvider;
        this.executionStore = executionStoreProvider.getIfAvailable(
                NoopTaskWorkflowExecutionStore::new
        );
    }

    public void beforeApprove(Exchange exchange) {
        record(exchange, TaskWorkflowRole.APPROVE_PROCESS, "requested");
    }

    public void afterApprove(Exchange exchange, Object approveResponse, Long processId) {
        exchange.setProperty(
                TaskWorkflowExchangeProperties.APPROVE_RESPONSE,
                approveResponse
        );
        if (processId != null) {
            exchange.setProperty(TaskWorkflowExchangeProperties.PROCESS_ID, processId);
        }
        record(exchange, TaskWorkflowRole.APPROVE_PROCESS, "succeeded");
    }

    public void beforeBusinessOperation(Exchange exchange) {
        record(exchange, TaskWorkflowRole.BUSINESS_OPERATION, "started");
        publish(ScmProviderEventType.WORKFLOW_BUSINESS_STARTED,
                exchange, "started", null);
    }

    public void afterBusinessOperation(
            Exchange exchange,
            Object businessResponse,
            ChainStepDecision decision,
            Throwable failure
    ) {
        exchange.setProperty(
                TaskWorkflowExchangeProperties.BUSINESS_RESPONSE,
                businessResponse
        );
        switch (decision) {
            case CONTINUE -> {
                exchange.setProperty(
                        TaskWorkflowExchangeProperties.BUSINESS_RESULT,
                        ChainStepDecision.CONTINUE
                );
                exchange.setProperty(
                        TaskWorkflowExchangeProperties.BUSINESS_RESULT_STATUS,
                        ChainStepDecision.CONTINUE.name()
                );
                record(exchange, TaskWorkflowRole.BUSINESS_OPERATION, "succeeded");
                publish(ScmProviderEventType.WORKFLOW_BUSINESS_SUCCEEDED,
                        exchange, "success", null);
            }
            case RETRY_LATER -> handleUnknownBusinessResult(exchange, failure);
            case FAIL -> handleDefinitiveBusinessFailure(exchange, failure);
        }
    }

    public void handleDefinitiveBusinessFailure(Exchange exchange, Throwable error) {
        exchange.setProperty(
                TaskWorkflowExchangeProperties.BUSINESS_RESULT,
                ChainStepDecision.FAIL
        );
        exchange.setProperty(
                TaskWorkflowExchangeProperties.BUSINESS_RESULT_STATUS,
                ChainStepDecision.FAIL.name()
        );
        record(exchange, TaskWorkflowRole.BUSINESS_OPERATION, "failed");
        publish(ScmProviderEventType.WORKFLOW_BUSINESS_FAILED,
                exchange, "failure", error);
    }

    public void beforeCompleteProcess(Exchange exchange) {
        record(exchange, TaskWorkflowRole.COMPLETE_PROCESS,
                "requested:SUCCESS");
    }

    public void afterCompleteProcess(Exchange exchange) {
        record(exchange, TaskWorkflowRole.COMPLETE_PROCESS,
                "completed:SUCCESS");
    }

    public void handleUnknownBusinessResult(Exchange exchange, Throwable error) {
        exchange.setProperty(
                TaskWorkflowExchangeProperties.BUSINESS_RESULT,
                ChainStepDecision.RETRY_LATER
        );
        exchange.setProperty(
                TaskWorkflowExchangeProperties.BUSINESS_RESULT_STATUS,
                ChainStepDecision.RETRY_LATER.name()
        );
        record(exchange, TaskWorkflowRole.BUSINESS_OPERATION, "unknown");
        publish(ScmProviderEventType.WORKFLOW_BUSINESS_UNKNOWN,
                exchange, "unknown", error);
    }

    private void record(Exchange exchange, TaskWorkflowRole role, String state) {
        executionStore.record(
                exchange.getProperty(
                        TaskWorkflowExchangeProperties.COMMAND,
                        TaskWorkflowCommand.class
                ),
                role,
                state,
                correlationId(exchange),
                exchange.getProperty(TaskWorkflowExchangeProperties.PROCESS_ID, Long.class)
        );
    }

    private void publish(
            ScmProviderEventType type,
            Exchange exchange,
            String outcome,
            Throwable error
    ) {
        try {
            ScmEventPublisher publisher = eventPublisherProvider.getIfAvailable();
            if (publisher == null) {
                return;
            }
            Map<String, Object> attributes = new LinkedHashMap<>();
            put(attributes, "scm.task.correlation_id", correlationId(exchange));
            put(attributes, "scm.task.process_id",
                    exchange.getProperty(TaskWorkflowExchangeProperties.PROCESS_ID));
            put(attributes, "scm.task.command",
                    exchange.getProperty(TaskWorkflowExchangeProperties.COMMAND));
            put(attributes, "scm.task.role", TaskWorkflowRole.BUSINESS_OPERATION);
            put(attributes, "scm.task.outcome", outcome);
            if (error != null) {
                put(attributes, "error.type", error.getClass().getSimpleName());
                put(attributes, "error.message",
                        ScmSafeEventAttributes.sanitizeMessage(error.getMessage()));
            }
            publisher.publish(ScmProviderEvent.of(type, attributes));
        } catch (RuntimeException exception) {
            log.warn("event=TASK_WORKFLOW_EVENT_PUBLISH_FAILED providerEventType={} "
                            + "outcome=ignored failureType={} failureMessage={}",
                    type.code(),
                    exception.getClass().getSimpleName(),
                    ScmSafeEventAttributes.sanitizeMessage(exception.getMessage()));
        }
    }

    private String correlationId(Exchange exchange) {
        String correlationId = exchange.getProperty(
                TaskWorkflowExchangeProperties.CORRELATION_ID,
                String.class
        );
        if (correlationId != null) {
            return correlationId;
        }
        return exchange.getProperty(Message.CORRELATION_ID, String.class);
    }

    private void put(Map<String, Object> attributes, String key, Object value) {
        if (value != null) {
            attributes.put(key, value);
        }
    }
}
