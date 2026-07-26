package ir.daneshrefah.scm.core.integration.service.routing;

import ir.daneshrefah.scm.common.model.error.ScmFault;
import ir.daneshrefah.scm.common.model.gateway.RoutingStrategy;
import ir.daneshrefah.scm.common.model.message.Message;
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
    private final RoutingResultClassifier resultClassifier;
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
        ChainStepDecision decision = null;
        String normalizedOutcome = null;
        RoutingStepObservationContext observationContext = step.observationContext();
        observationTraceSupport.startRoutingStepCall(
                exchange,
                observationContext.serviceCode(),
                step.serviceOperation().getOperationName(),
                routingStrategy,
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

            ChainStepDecisionContext decisionContext = new ChainStepDecisionContext(
                    exchange, step, context, response, failure);
            decision = decide(routingStrategy, step, decisionContext);
            if (failure != null && decision == ChainStepDecision.CONTINUE) {
                decision = ChainStepDecision.FAIL;
            }
            normalizedOutcome = normalizedOutcome(step, decisionContext, decision, exchange);
            if (failure == null && decision == ChainStepDecision.CONTINUE) {
                context.record(step.serviceOperation().getOperationName(), response);
            }
            completed = new RoutingStepExecutionResult(
                    response,
                    failure,
                    elapsedMs(startedNanos),
                    decision,
                    normalizedOutcome
            );
            return completed;
        } catch (RuntimeException executionFailure) {
            failure = executionFailure;
            decision = ChainStepDecision.FAIL;
            preserveFailure(exchange, executionFailure);
            throw executionFailure;
        } finally {
            long durationMs = completed == null ? elapsedMs(startedNanos) : completed.elapsedMs();
            observationTraceSupport.finishRoutingStepCall(
                    exchange,
                    decision == null ? null : decision.name(),
                    normalizedOutcome,
                    failure,
                    durationMs
            );
            exchange.removeProperty(NORMALIZED_OUTCOME_PROPERTY);
        }
    }

    private ChainStepDecision decide(
            RoutingStrategy routingStrategy,
            RoutingStepPlan step,
            ChainStepDecisionContext context
    ) {
        if (step.decisionPolicy() != null) {
            return step.decisionPolicy().decide(context);
        }
        if (routingStrategy == RoutingStrategy.FIRST
                && context.failure() == null
                && !(context.response() instanceof Message)
                && !(context.response() instanceof ScmFault)) {
            return ChainStepDecision.CONTINUE;
        }
        return switch (resultClassifier.classify(context.response(), context.failure())) {
            case SUCCESS -> ChainStepDecision.CONTINUE;
            case TEMPORARY_OR_UNKNOWN -> ChainStepDecision.RETRY_LATER;
            case DEFINITIVE_FAILURE -> ChainStepDecision.FAIL;
        };
    }

    private String normalizedOutcome(
            RoutingStepPlan step,
            ChainStepDecisionContext context,
            ChainStepDecision decision,
            Exchange exchange
    ) {
        Object property = exchange.getProperty(NORMALIZED_OUTCOME_PROPERTY);
        try {
            String policyOutcome = step.decisionPolicy() == null
                    ? null
                    : step.decisionPolicy().normalizedOutcome(context, decision);
            return policyOutcome != null ? policyOutcome : property == null ? null : String.valueOf(property);
        } catch (RuntimeException ignored) {
            return property == null ? null : String.valueOf(property);
        }
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
