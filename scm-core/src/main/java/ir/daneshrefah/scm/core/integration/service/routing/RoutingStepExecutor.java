package ir.daneshrefah.scm.core.integration.service.routing;

import ir.daneshrefah.scm.common.model.gateway.RoutingStrategy;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.message.MessageStatus;
import ir.daneshrefah.scm.core.integration.observability.CoreObservationTraceSupport;
import lombok.RequiredArgsConstructor;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class RoutingStepExecutor {
    public static final String NORMALIZED_OUTCOME_PROPERTY = "scm.routing.operation.normalized-outcome";

    private final ServiceOperationRouteMetadataSetter metadataSetter;
    private final ProducerTemplate producerTemplate;
    private final DefaultRoutingDecisionPolicy defaultDecisionPolicy;
    private final CoreObservationTraceSupport observationTraceSupport;

    public RoutingStepExecutionResult execute(
            Exchange exchange,
            RoutingStepPlan step,
            RoutingExecutionContext context
    ) {
        return execute(exchange, null, step, context);
    }

    public RoutingStepExecutionResult execute(
            Exchange exchange,
            RoutingStrategy routingStrategy,
            RoutingStepPlan step,
            RoutingExecutionContext context
    ) {
        long startedNanos = System.nanoTime();
        RoutingStepExecutionResult completed = null;
        Object response = null;
        Throwable failure = null;
        RoutingDecisionResult decisionResult = null;
        String normalizedOutcome = null;
        RoutingStepObservationContext observationContext = step.observationContext();
        observationTraceSupport.startRoutingStepCall(
                exchange,
                observationContext.serviceCode(),
                step.serviceOperation().getOperationName(),
                routingStrategy,
                step.stepId(),
                observationContext.stepIndex(),
                observationContext.inboundAction(),
                observationContext.taskWorkflowStepType(),
                observationContext.spanKind()
        );
        try {
            Object request = step.requestFactory().create(exchange, context);
            metadataSetter.apply(exchange, step.serviceOperation());
            if (observationContext.taskWorkflowStepType() != null) {
                exchange.setProperty(
                        Message.TASK_WORKFLOW_STEP_TYPE,
                        observationContext.taskWorkflowStepType()
                );
            }
            exchange.removeProperty(NORMALIZED_OUTCOME_PROPERTY);
            exchange.removeProperty(Exchange.EXCEPTION_CAUGHT);
            exchange.setException(null);
            exchange.getMessage().setBody(request);
            Exchange result;
            try {
                result = producerTemplate.send(step.endpointUri(), exchange);
            } catch (RuntimeException sendFailure) {
                result = exchange;
                failure = sendFailure;
                preserveFailure(exchange, failure);
            }
            if (result != exchange) {
                exchange.getMessage().setBody(result.getMessage().getBody());
                exchange.getMessage().getHeaders().putAll(result.getMessage().getHeaders());
            }
            if (failure == null) {
                failure = result.getException();
            }
            if (failure == null) {
                failure = result.getProperty(Exchange.EXCEPTION_CAUGHT, Throwable.class);
            }
            response = exchange.getMessage().getBody();
            preserveFailure(exchange, failure);

            RoutingDecisionContext decisionContext = new RoutingDecisionContext(
                    exchange, step, context, response, failure);
            decisionResult = decide(step, decisionContext);
            normalizedOutcome = decisionResult.normalizedOutcome();
            if (decisionResult.decision() == RoutingDecision.SUCCESS) {
                context.record(step.stepId(), response);
            }
            completed = new RoutingStepExecutionResult(
                    response,
                    failure,
                    elapsedMs(startedNanos),
                    decisionResult
            );
            exchange.setException(null);
            exchange.removeProperty(Exchange.EXCEPTION_CAUGHT);
            return completed;
        } catch (RuntimeException executionFailure) {
            failure = executionFailure;
            decisionResult = new RoutingDecisionResult(
                    RoutingDecision.FAIL,
                    MessageStatus.SC_ERROR_SYSTEM,
                    "ROUTING_STEP_EXECUTION_ERROR",
                    "Routing step execution failed",
                    normalizedOutcome
            );
            preserveFailure(exchange, executionFailure);
            throw executionFailure;
        } finally {
            long durationMs = completed == null ? elapsedMs(startedNanos) : completed.elapsedMs();
            observationTraceSupport.finishRoutingStepCall(
                    exchange,
                    decisionResult == null
                            ? null
                            : decisionResult.decision().name(),
                    normalizedOutcome,
                    failure,
                    durationMs
            );
            exchange.removeProperty(NORMALIZED_OUTCOME_PROPERTY);
        }
    }

    private RoutingDecisionResult decide(
            RoutingStepPlan step,
            RoutingDecisionContext context
    ) {
        if (step.decisionPolicy() != null) {
            return step.decisionPolicy().decide(context);
        }
        return defaultDecisionPolicy.decide(context);
    }

    private void preserveFailure(Exchange exchange, Throwable failure) {
        if (failure != null) {
            exchange.setProperty(Exchange.EXCEPTION_CAUGHT, failure);
        }
    }

    private long elapsedMs(long startedNanos) {
        return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedNanos);
    }
}
