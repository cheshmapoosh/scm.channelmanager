package ir.daneshrefah.scm.core.integration.service.routing.taskworkflow;

import ir.daneshrefah.scm.common.model.gateway.Service;
import ir.daneshrefah.scm.common.model.gateway.ServiceOperation;
import ir.daneshrefah.scm.common.model.taskworkflow.TaskWorkflowStepType;
import ir.daneshrefah.scm.core.integration.service.routing.DefaultRoutingDecisionPolicy;
import ir.daneshrefah.scm.core.integration.service.routing.RoutingDecisionPolicy;
import ir.daneshrefah.scm.core.integration.service.routing.RoutingDecisionPolicyRegistry;
import ir.daneshrefah.scm.core.integration.service.routing.RoutingOperationMetadataResolver;
import ir.daneshrefah.scm.core.integration.service.routing.RoutingPlan;
import ir.daneshrefah.scm.core.integration.service.routing.RoutingPlanIdentity;
import ir.daneshrefah.scm.core.integration.service.routing.RoutingStepObservationContext;
import ir.daneshrefah.scm.core.integration.service.routing.RoutingStepPlan;
import ir.daneshrefah.scm.core.integration.service.routing.ServiceOperationDefinitionClassifier;
import ir.daneshrefah.scm.core.integration.service.routing.ServiceOperationEndpointResolver;
import ir.daneshrefah.scm.core.integration.service.routing.ServiceOperationSelector;
import ir.daneshrefah.scm.provider.task.workflow.TaskWorkflowProviderCapabilityRegistry;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@RequiredArgsConstructor
public class TaskWorkflowRoutePlanFactory {
    private final ServiceOperationSelector operationSelector;
    private final ServiceOperationEndpointResolver endpointResolver;
    private final TaskWorkflowActionPlanParser actionPlanParser;
    private final TaskWorkflowCommandPlanValidator validator;
    private final RoutingDecisionPolicyRegistry policyRegistry;
    private final TaskWorkflowPayloadMapper payloadMapper;
    private final RoutingOperationMetadataResolver operationMetadataResolver;
    private final TaskWorkflowPlanFingerprint fingerprint;
    private final ObjectProvider<TaskWorkflowProviderCapabilityRegistry>
            providerCapabilities;

    public TaskWorkflowRoutePlan create(Service service) {
        Map<String, ServiceOperation> executableOperations = operationsByName(service);
        Map<TaskWorkflowActionKey, TaskWorkflowCommandPlan> plans =
                new LinkedHashMap<>();

        for (ServiceOperation actionPlan : operationSelector.activeActionPlans(service)) {
            TaskWorkflowActionPlanConfig config =
                    actionPlanParser.parse(service, actionPlan);
            validator.validate(service, config);
            TaskWorkflowActionKey key = new TaskWorkflowActionKey(
                    config.serviceCode(),
                    config.inboundAction()
            );
            if (plans.containsKey(key)) {
                throw invalid(service, config, -1, null, null, null,
                        "duplicate active action plan");
            }

            List<RoutingStepPlan> steps = config.steps().stream()
                    .map(step -> toStep(
                            service,
                            config,
                            step,
                            executableOperations
                    ))
                    .toList();
            String planFingerprint = fingerprint.create(config);
            RoutingPlanIdentity identity = new RoutingPlanIdentity(
                    config.serviceCode(),
                    config.inboundAction(),
                    config.actionPlanName(),
                    config.definitionId(),
                    planFingerprint
            );
            RoutingPlan routingPlan = new RoutingPlan(
                    config.serviceCode() + ":" + config.inboundAction()
                            + ":" + config.actionPlanName(),
                    identity,
                    config.routingStrategy(),
                    steps
            );
            plans.put(key, new TaskWorkflowCommandPlan(
                    config.command(),
                    config.inboundAction(),
                    config.actionPlanName(),
                    routingPlan,
                    actionPlan
            ));
        }
        if (plans.isEmpty()) {
            throw new IllegalStateException("TASK_WORKFLOW serviceCode=" + code(service)
                    + " has no active ACTION_PLAN service operations");
        }
        return new TaskWorkflowRoutePlan(service, plans);
    }

    private RoutingStepPlan toStep(
            Service service,
            TaskWorkflowActionPlanConfig config,
            TaskWorkflowActionPlanStepConfig step,
            Map<String, ServiceOperation> executableOperations
    ) {
        ServiceOperation operation =
                executableOperations.get(normalize(step.operationName()));
        if (operation == null) {
            throw invalid(service, config, step.stepIndex(), step.stepId(),
                    step.stepType(), step.operationName(),
                    missingOperationReason(service, config, step));
        }
        validateProviderCapability(service, config, step, operation);
        String spanKind = resolveSpanKind(service, config, step, operation);
        RoutingDecisionPolicy decisionPolicy =
                resolvePolicy(service, config, step);
        return new RoutingStepPlan(
                step.stepId(),
                step.stepIndex(),
                operation,
                endpointResolver.resolve(operation.getOperationName()),
                new TaskWorkflowStepRequestFactory(
                        step.stepId(),
                        step.stepType(),
                        payloadMapper
                ),
                decisionPolicy,
                new RoutingStepObservationContext(
                        service.getCode(),
                        config.inboundAction(),
                        step.stepType(),
                        step.stepId(),
                        step.stepIndex(),
                        spanKind
                )
        );
    }

    private void validateProviderCapability(
            Service service,
            TaskWorkflowActionPlanConfig config,
            TaskWorkflowActionPlanStepConfig step,
            ServiceOperation operation
    ) {
        boolean taskProvider = operationMetadataResolver.targetsTaskProvider(
                operation.getOperationName());
        if (step.stepType() == TaskWorkflowStepType.BUSINESS_OPERATION
                && taskProvider) {
            throw invalid(service, config, step.stepIndex(), step.stepId(),
                    step.stepType(), step.operationName(),
                    "BUSINESS_OPERATION must not target an scm-task provider");
        }
        if (step.stepType() != TaskWorkflowStepType.BUSINESS_OPERATION
                && !taskProvider) {
            throw invalid(service, config, step.stepIndex(), step.stepId(),
                    step.stepType(), step.operationName(),
                    "non-business workflow steps must target an scm-task provider");
        }
        if (taskProvider) {
            providerCapabilityRegistry(code(service)).requireSupported(
                    code(service),
                    operationMetadataResolver.providerUri(
                            operation.getOperationName()),
                    step.stepType()
            );
            payloadMapper.requireProviderRequestFactory(
                    code(service),
                    step.stepType()
            );
        }
    }

    private TaskWorkflowProviderCapabilityRegistry providerCapabilityRegistry(
            String serviceCode
    ) {
        List<TaskWorkflowProviderCapabilityRegistry> registries =
                providerCapabilities.orderedStream().toList();
        if (registries.size() != 1) {
            throw new IllegalStateException(
                    "Active TASK_WORKFLOW serviceCode=" + serviceCode
                            + " requires exactly one "
                            + "TaskWorkflowProviderCapabilityRegistry; found "
                            + registries.size()
            );
        }
        return registries.getFirst();
    }

    private String resolveSpanKind(
            Service service,
            TaskWorkflowActionPlanConfig config,
            TaskWorkflowActionPlanStepConfig step,
            ServiceOperation operation
    ) {
        try {
            return operationMetadataResolver.spanKind(
                    operation.getOperationName());
        } catch (RuntimeException exception) {
            throw new IllegalStateException(
                    invalid(service, config, step.stepIndex(), step.stepId(),
                            step.stepType(), step.operationName(),
                            "operation metadata is unavailable").getMessage(),
                    exception
            );
        }
    }

    private RoutingDecisionPolicy resolvePolicy(
            Service service,
            TaskWorkflowActionPlanConfig config,
            TaskWorkflowActionPlanStepConfig step
    ) {
        String policyCode = StringUtils.defaultIfBlank(
                step.decisionPolicy(),
                DefaultRoutingDecisionPolicy.CODE
        );
        try {
            return policyRegistry.getRequired(policyCode);
        } catch (RuntimeException exception) {
            throw new IllegalStateException(
                    invalid(service, config, step.stepIndex(), step.stepId(),
                            step.stepType(), step.operationName(),
                            "invalid decisionPolicy=" + policyCode).getMessage(),
                    exception
            );
        }
    }

    private Map<String, ServiceOperation> operationsByName(Service service) {
        Map<String, ServiceOperation> operations = new LinkedHashMap<>();
        for (ServiceOperation operation : operationSelector.active(service)) {
            if (StringUtils.isBlank(operation.getOperationName())) {
                throw new IllegalStateException("Invalid TASK_WORKFLOW operation "
                        + "for serviceCode=" + code(service)
                        + ", operationName=<blank>: operationName is required");
            }
            ServiceOperation old = operations.putIfAbsent(
                    normalize(operation.getOperationName()),
                    operation
            );
            if (old != null) {
                throw new IllegalStateException("Duplicate active operationName="
                        + operation.getOperationName()
                        + " for serviceCode=" + code(service));
            }
        }
        return Map.copyOf(operations);
    }

    private String missingOperationReason(
            Service service,
            TaskWorkflowActionPlanConfig config,
            TaskWorkflowActionPlanStepConfig step
    ) {
        if (normalize(config.actionPlanOperation().getOperationName())
                .equals(normalize(step.operationName()))) {
            return "an action-plan service operation cannot reference itself "
                    + "as an executable step";
        }
        if (service != null && service.getServiceOperations() != null) {
            for (ServiceOperation candidate : service.getServiceOperations()) {
                if (candidate == null
                        || StringUtils.isBlank(candidate.getOperationName())
                        || !normalize(candidate.getOperationName())
                        .equals(normalize(step.operationName()))) {
                    continue;
                }
                if (ServiceOperationDefinitionClassifier.isActionPlan(candidate)) {
                    return "an ACTION_PLAN service operation cannot be used "
                            + "as an executable step";
                }
                if (!Boolean.TRUE.equals(candidate.getActive())) {
                    return "referenced executable service operation is inactive";
                }
            }
        }
        return "no matching active executable service operation belongs to "
                + "the same service";
    }

    private IllegalStateException invalid(
            Service service,
            TaskWorkflowActionPlanConfig config,
            int index,
            String stepId,
            TaskWorkflowStepType stepType,
            String operationName,
            String reason
    ) {
        return new IllegalStateException("Invalid TASK_WORKFLOW action plan serviceCode="
                + code(service)
                + ", inboundAction=" + config.inboundAction()
                + ", actionPlanName=" + config.actionPlanName()
                + ", definitionId=" + config.definitionId()
                + ", routingStrategy=" + config.routingStrategy()
                + ", stepId=" + stepId
                + ", stepIndex=" + index
                + ", stepType=" + stepType
                + ", operationName=" + operationName
                + ", reason=" + reason);
    }

    private String normalize(String value) {
        return value.trim().toLowerCase(Locale.ROOT);
    }

    private String code(Service service) {
        return service == null ? "<null>" : String.valueOf(service.getCode());
    }
}
