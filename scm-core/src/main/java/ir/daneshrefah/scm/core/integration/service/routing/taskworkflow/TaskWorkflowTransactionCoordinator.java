package ir.daneshrefah.scm.core.integration.service.routing.taskworkflow;

import ir.daneshrefah.scm.common.event.ScmEventPublisher;
import ir.daneshrefah.scm.common.event.ScmSafeEventAttributes;
import ir.daneshrefah.scm.common.event.provider.ScmProviderEvent;
import ir.daneshrefah.scm.common.event.provider.ScmProviderEventType;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.taskworkflow.TaskWorkflowStepType;
import ir.daneshrefah.scm.core.integration.service.routing.RoutingDecision;
import ir.daneshrefah.scm.core.integration.service.routing.RoutingStepExecutionResult;
import ir.daneshrefah.scm.core.integration.service.routing.RoutingStepPlan;
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

    public TaskWorkflowTransactionCoordinator(
            ObjectProvider<ScmEventPublisher> eventPublisherProvider
    ) {
        this.eventPublisherProvider = eventPublisherProvider;
    }

    public void beforeStep(Exchange exchange, RoutingStepPlan step) {
        if (step.observationContext().taskWorkflowStepType()
                == TaskWorkflowStepType.BUSINESS_OPERATION) {
            publish(
                    ScmProviderEventType.WORKFLOW_BUSINESS_STARTED,
                    exchange,
                    step,
                    null,
                    "started",
                    null
            );
        }
    }

    public void afterStep(
            Exchange exchange,
            RoutingStepPlan step,
            RoutingStepExecutionResult result
    ) {
        TaskWorkflowStepType stepType =
                step.observationContext().taskWorkflowStepType();
        if (stepType == TaskWorkflowStepType.APPROVE_PROCESS
                && result.decision() == RoutingDecision.SUCCESS) {
            exchange.setProperty(
                    TaskWorkflowExchangeProperties.APPROVE_RESPONSE,
                    result.response()
            );
        }
        if (stepType != TaskWorkflowStepType.BUSINESS_OPERATION) {
            return;
        }

        exchange.setProperty(
                TaskWorkflowExchangeProperties.BUSINESS_RESPONSE,
                result.response()
        );
        exchange.setProperty(
                TaskWorkflowExchangeProperties.BUSINESS_RESULT,
                result.decision()
        );
        exchange.setProperty(
                TaskWorkflowExchangeProperties.BUSINESS_RESULT_STATUS,
                result.decision().name()
        );
        switch (result.decision()) {
            case SUCCESS -> publish(
                    ScmProviderEventType.WORKFLOW_BUSINESS_SUCCEEDED,
                    exchange,
                    step,
                    result,
                    "success",
                    null
            );
            case RETRY_LATER -> publish(
                    ScmProviderEventType.WORKFLOW_BUSINESS_UNKNOWN,
                    exchange,
                    step,
                    result,
                    "unknown",
                    result.failure()
            );
            case FAIL -> publish(
                    ScmProviderEventType.WORKFLOW_BUSINESS_FAILED,
                    exchange,
                    step,
                    result,
                    "failure",
                    result.failure()
            );
        }
    }

    private void publish(
            ScmProviderEventType type,
            Exchange exchange,
            RoutingStepPlan step,
            RoutingStepExecutionResult result,
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
            put(attributes, "scm.task.execution_id",
                    exchange.getProperty(Message.EXECUTION_ID));
            put(attributes, "scm.task.process_id",
                    exchange.getProperty(TaskWorkflowExchangeProperties.PROCESS_ID));
            put(attributes, "scm.task.command",
                    exchange.getProperty(TaskWorkflowExchangeProperties.COMMAND));
            put(attributes, "scm.task.inbound_action",
                    exchange.getProperty(Message.INBOUND_ROUTE_ACTION));
            put(attributes, "scm.task.action_plan_name",
                    exchange.getProperty(
                            TaskWorkflowExchangeProperties.ACTION_PLAN_NAME));
            put(attributes, "scm.task.routing_strategy",
                    exchange.getProperty(
                            TaskWorkflowExchangeProperties.ROUTING_STRATEGY));
            put(attributes, "scm.task.step_type",
                    step.observationContext().taskWorkflowStepType());
            put(attributes, "scm.task.step_id", step.stepId());
            put(attributes, "scm.task.step_index", step.stepIndex());
            put(attributes, "scm.task.routing_decision",
                    result == null ? null : result.decision());
            put(attributes, "scm.task.retryable",
                    result == null ? null
                            : result.decision() == RoutingDecision.RETRY_LATER);
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

    private void put(
            Map<String, Object> attributes,
            String key,
            Object value
    ) {
        if (value != null) {
            attributes.put(key, value);
        }
    }
}
