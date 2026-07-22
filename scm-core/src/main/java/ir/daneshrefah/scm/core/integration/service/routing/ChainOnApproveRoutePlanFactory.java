package ir.daneshrefah.scm.core.integration.service.routing;

import ir.daneshrefah.scm.common.model.gateway.RoutingStrategy;
import ir.daneshrefah.scm.common.model.gateway.Service;
import ir.daneshrefah.scm.common.model.gateway.ServiceOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

@Component
@RequiredArgsConstructor
public class ChainOnApproveRoutePlanFactory {
    private final ServiceOperationSelector operationSelector;
    private final ServiceOperationEndpointResolver endpointResolver;
    private final ChainOnApproveStepConfigExtractor stepConfigExtractor;
    private final ChainStepDecisionPolicyRegistry policyRegistry;
    private final RoutingOperationMetadataResolver operationMetadataResolver;

    public RoutingPlan create(Service service) {
        List<OrderedStep> steps = operationSelector
                .requireAtLeastTwoActive(service, RoutingStrategy.CHAIN_ON_APPROVE)
                .stream()
                .map(operation -> createStep(service, operation))
                .sorted(Comparator.comparingInt(OrderedStep::executionOrder))
                .toList();
        rejectDuplicateExecutionOrders(service, steps);
        return new RoutingPlan(service.getCode(), RoutingStrategy.CHAIN_ON_APPROVE,
                IntStream.range(0, steps.size())
                        .mapToObj(index -> withStepIndex(steps.get(index).step(), index))
                        .toList());
    }

    private OrderedStep createStep(Service service, ServiceOperation serviceOperation) {
        ChainOnApproveStepConfig stepConfig = stepConfigExtractor.extract(service, serviceOperation);
        String policyCode = stepConfig.decisionPolicy() == null
                ? DefaultSuccessChainStepDecisionPolicy.CODE : stepConfig.decisionPolicy();
        ChainStepDecisionPolicy decisionPolicy;
        try {
            decisionPolicy = policyRegistry.getRequired(policyCode);
        } catch (RuntimeException exception) {
            throw new IllegalStateException("Cannot build CHAIN_ON_APPROVE route for serviceCode="
                    + serviceCode(service) + ", operationName="
                    + serviceOperation.getOperationName() + ", decisionPolicy="
                    + policyCode + ": " + exception.getMessage(), exception);
        }
        String spanKind;
        try {
            spanKind = operationMetadataResolver.spanKind(
                    serviceOperation.getOperationName());
        } catch (RuntimeException exception) {
            throw new IllegalStateException("Cannot build CHAIN_ON_APPROVE route for serviceCode="
                    + serviceCode(service) + ", operationName="
                    + serviceOperation.getOperationName()
                    + ": operation metadata is unavailable", exception);
        }
        return new OrderedStep(stepConfig.executionOrder(), new RoutingStepPlan(
                serviceOperation.getOperationName(),
                serviceOperation,
                endpointResolver.resolve(serviceOperation.getOperationName()),
                (exchange, execution) -> exchange.getMessage().getBody(),
                decisionPolicy,
                new RoutingStepObservationContext(
                        service.getCode(),
                        null,
                        null,
                        stepConfig.executionOrder(),
                        spanKind)
        ));
    }

    private void rejectDuplicateExecutionOrders(Service service, List<OrderedStep> steps) {
        OrderedStep previous = null;
        for (OrderedStep current : steps) {
            if (previous != null && previous.executionOrder() == current.executionOrder()) {
                throw new IllegalStateException("Cannot build CHAIN_ON_APPROVE route for serviceCode="
                        + serviceCode(service) + ", field=executionOrder: duplicate executionOrder="
                        + current.executionOrder() + " for " + describe(previous)
                        + " and " + describe(current)
                        + ". Each active service-operation in the same chain must use a unique executionOrder.");
            }
            previous = current;
        }
    }

    private RoutingStepPlan withStepIndex(RoutingStepPlan step, int stepIndex) {
        RoutingStepObservationContext observation = step.observationContext();
        return new RoutingStepPlan(
                step.stepName(),
                step.serviceOperation(),
                step.endpointUri(),
                step.requestFactory(),
                step.decisionPolicy(),
                new RoutingStepObservationContext(
                        observation.serviceCode(),
                        observation.inboundAction(),
                        observation.taskRole(),
                        stepIndex,
                        observation.spanKind())
        );
    }

    private String describe(OrderedStep step) {
        return "operationName=" + step.step().serviceOperation().getOperationName();
    }

    private String serviceCode(Service service) {
        return service == null ? "<null>" : String.valueOf(service.getCode());
    }

    private record OrderedStep(int executionOrder, RoutingStepPlan step) {
    }
}
