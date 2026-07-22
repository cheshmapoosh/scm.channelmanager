package ir.daneshrefah.scm.core.integration.service.routing.taskworkflow;

import ir.daneshrefah.scm.common.model.gateway.ChannelServiceDefinition;
import ir.daneshrefah.scm.common.model.gateway.ChannelServiceDefinitionType;
import ir.daneshrefah.scm.common.model.gateway.InboundChannelServiceDefinition;
import ir.daneshrefah.scm.common.model.gateway.RoutingStrategy;
import ir.daneshrefah.scm.common.model.gateway.Service;
import ir.daneshrefah.scm.common.model.gateway.ServiceOperation;
import ir.daneshrefah.scm.core.integration.service.routing.ChainStepDecisionPolicyRegistry;
import ir.daneshrefah.scm.core.integration.service.routing.ChainStepDecisionPolicy;
import ir.daneshrefah.scm.core.integration.service.routing.DefaultSuccessChainStepDecisionPolicy;
import ir.daneshrefah.scm.core.integration.service.routing.RoutingPlan;
import ir.daneshrefah.scm.core.integration.service.routing.RoutingOperationMetadataResolver;
import ir.daneshrefah.scm.core.integration.service.routing.RoutingStepObservationContext;
import ir.daneshrefah.scm.core.integration.service.routing.RoutingStepPlan;
import ir.daneshrefah.scm.core.integration.service.routing.ServiceOperationEndpointResolver;
import ir.daneshrefah.scm.core.integration.service.routing.ServiceOperationSelector;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class TaskWorkflowRoutePlanFactory {
    private final ServiceOperationSelector operationSelector;
    private final ServiceOperationEndpointResolver endpointResolver;
    private final TaskWorkflowInboundCommandConfigExtractor configExtractor;
    private final TaskWorkflowCommandPlanValidator validator;
    private final ChainStepDecisionPolicyRegistry policyRegistry;
    private final TaskWorkflowPayloadMapper payloadMapper;
    private final TaskWorkflowTransactionCoordinator transactionCoordinator;
    private final RoutingOperationMetadataResolver operationMetadataResolver;

    public TaskWorkflowRoutePlanFactory(
            ServiceOperationSelector operationSelector,
            ServiceOperationEndpointResolver endpointResolver,
            TaskWorkflowInboundCommandConfigExtractor configExtractor,
            TaskWorkflowCommandPlanValidator validator,
            ChainStepDecisionPolicyRegistry policyRegistry,
            TaskWorkflowPayloadMapper payloadMapper,
            TaskWorkflowTransactionCoordinator transactionCoordinator,
            RoutingOperationMetadataResolver operationMetadataResolver
    ) {
        this.operationSelector = operationSelector;
        this.endpointResolver = endpointResolver;
        this.configExtractor = configExtractor;
        this.validator = validator;
        this.policyRegistry = policyRegistry;
        this.payloadMapper = payloadMapper;
        this.transactionCoordinator = transactionCoordinator;
        this.operationMetadataResolver = operationMetadataResolver;
    }

    public TaskWorkflowRoutePlan create(Service service, List<ChannelServiceDefinition> definitions) {
        Map<String, ServiceOperation> operations = operationsByName(service);
        EnumMap<TaskWorkflowCommand, TaskWorkflowCommandPlan> plans =
                new EnumMap<>(TaskWorkflowCommand.class);
        for (InboundChannelServiceDefinition inbound : inboundDefinitions(definitions)) {
            TaskWorkflowInboundCommandConfig config = configExtractor.extract(service, inbound);
            validator.validate(service, config);
            if (plans.containsKey(config.command())) {
                throw invalid(service, config, -1, null, null,
                        "duplicate inbound command");
            }
            rejectDuplicateOperations(service, config);
            List<RoutingStepPlan> steps = java.util.stream.IntStream.range(0, config.steps().size())
                    .mapToObj(index -> toStep(service, config, index, operations))
                    .toList();
            RoutingPlan routingPlan = new RoutingPlan(
                    service.getCode() + ":" + config.inboundAction(),
                    config.routingStrategy(), steps);
            plans.put(config.command(), new TaskWorkflowCommandPlan(
                    config.command(), config.inboundAction(), routingPlan, inbound));
        }
        if (plans.isEmpty()) {
            throw new IllegalStateException("TASK_WORKFLOW serviceCode=" + code(service)
                    + " has no INBOUND command definitions");
        }
        return new TaskWorkflowRoutePlan(service, plans);
    }

    private RoutingStepPlan toStep(Service service, TaskWorkflowInboundCommandConfig config,
                                   int index, Map<String, ServiceOperation> operations) {
        TaskWorkflowInboundCommandStepConfig step = config.steps().get(index);
        ServiceOperation operation = operations.get(normalize(step.operationName()));
        if (operation == null) {
            throw invalid(service, config, index, step.role(), step.operationName(),
                    "no matching active ServiceOperation");
        }
        var decisionPolicy = config.routingStrategy() == RoutingStrategy.CHAIN_ON_APPROVE
                ? new TaskWorkflowStepDecisionPolicy(
                        step.role(),
                        resolvePolicy(service, config, index, step),
                        payloadMapper,
                        transactionCoordinator)
                : null;
        return new RoutingStepPlan(step.operationName(), operation,
                endpointResolver.resolve(operation.getOperationName()),
                new TaskWorkflowStepRequestFactory(
                        step.role(), payloadMapper, transactionCoordinator),
                decisionPolicy,
                new RoutingStepObservationContext(service.getCode(), config.inboundAction(),
                        step.role().name(), index,
                        resolveSpanKind(service, config, index, step, operation)));
    }

    private String resolveSpanKind(
            Service service,
            TaskWorkflowInboundCommandConfig config,
            int index,
            TaskWorkflowInboundCommandStepConfig step,
            ServiceOperation operation
    ) {
        try {
            return operationMetadataResolver.spanKind(operation.getOperationName());
        } catch (RuntimeException exception) {
            throw new IllegalStateException(
                    invalid(service, config, index, step.role(), step.operationName(),
                            "operation metadata is unavailable").getMessage(),
                    exception
            );
        }
    }

    private ChainStepDecisionPolicy resolvePolicy(
            Service service,
            TaskWorkflowInboundCommandConfig config,
            int index,
            TaskWorkflowInboundCommandStepConfig step
    ) {
        String policyCode = step.decisionPolicy() == null
                ? DefaultSuccessChainStepDecisionPolicy.CODE
                : step.decisionPolicy();
        try {
            return policyRegistry.getRequired(policyCode);
        } catch (RuntimeException exception) {
            throw new IllegalStateException(
                    invalid(service, config, index, step.role(), step.operationName(),
                            "invalid decisionPolicy=" + policyCode).getMessage(),
                    exception
            );
        }
    }

    private Map<String, ServiceOperation> operationsByName(Service service) {
        Map<String, ServiceOperation> operations = new HashMap<>();
        for (ServiceOperation operation : operationSelector.active(service)) {
            if (StringUtils.isBlank(operation.getOperationName())) {
                throw new IllegalStateException("Invalid TASK_WORKFLOW operation for serviceCode="
                        + code(service) + ", operationName=<blank>: operationName is required");
            }
            ServiceOperation old = operations.putIfAbsent(normalize(operation.getOperationName()), operation);
            if (old != null) {
                throw new IllegalStateException("Duplicate active operationName="
                        + operation.getOperationName() + " for serviceCode=" + code(service));
            }
        }
        return Map.copyOf(operations);
    }

    private void rejectDuplicateOperations(Service service, TaskWorkflowInboundCommandConfig config) {
        Set<String> names = new HashSet<>();
        for (int index = 0; index < config.steps().size(); index++) {
            TaskWorkflowInboundCommandStepConfig step = config.steps().get(index);
            if (!names.add(normalize(step.operationName()))) {
                throw invalid(service, config, index, step.role(), step.operationName(),
                        "duplicate operationName in one inbound action");
            }
        }
    }

    private List<InboundChannelServiceDefinition> inboundDefinitions(
            List<ChannelServiceDefinition> definitions) {
        if (definitions == null) return List.of();
        return definitions.stream()
                .filter(value -> value != null
                        && value.getType() == ChannelServiceDefinitionType.INBOUND)
                .map(value -> {
                    if (value instanceof InboundChannelServiceDefinition inbound) {
                        return inbound;
                    }
                    throw new IllegalStateException(
                            "Invalid TASK_WORKFLOW inbound definition type="
                                    + value.getClass().getName()
                    );
                })
                .toList();
    }

    private IllegalStateException invalid(Service service, TaskWorkflowInboundCommandConfig config,
                                          int index, TaskWorkflowRole role,
                                          String operationName, String reason) {
        return new IllegalStateException("Invalid TASK_WORKFLOW command plan serviceCode=" + code(service)
                + ", inboundAction=" + config.inboundAction()
                + ", routingStrategy=" + config.routingStrategy()
                + ", stepIndex=" + index + ", role=" + role
                + ", operationName=" + operationName + ", reason=" + reason);
    }

    private String normalize(String value) { return value.trim().toLowerCase(java.util.Locale.ROOT); }
    private String code(Service service) { return service == null ? "<null>" : String.valueOf(service.getCode()); }
}
