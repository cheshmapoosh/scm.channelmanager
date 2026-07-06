package ir.daneshrefah.scm.core.integration.service.routing.taskworkflow;

import ir.daneshrefah.scm.common.model.gateway.ChannelServiceDefinition;
import ir.daneshrefah.scm.common.model.gateway.ChannelServiceDefinitionType;
import ir.daneshrefah.scm.common.model.gateway.InboundChannelServiceDefinition;
import ir.daneshrefah.scm.common.model.gateway.RoutingStrategy;
import ir.daneshrefah.scm.common.model.gateway.Service;
import ir.daneshrefah.scm.common.model.gateway.ServiceOperation;
import ir.daneshrefah.scm.core.integration.runtime.RouteIdSupport;
import ir.daneshrefah.scm.core.integration.service.routing.ServiceOperationSelector;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class TaskWorkflowRoutePlanFactory {
    private final ServiceOperationSelector operationSelector;
    private final TaskWorkflowOperationRoleConfigExtractor operationRoleExtractor;
    private final TaskWorkflowInboundCommandConfigExtractor inboundCommandExtractor;

    public TaskWorkflowRoutePlanFactory(
            ServiceOperationSelector operationSelector,
            TaskWorkflowOperationRoleConfigExtractor operationRoleExtractor,
            TaskWorkflowInboundCommandConfigExtractor inboundCommandExtractor
    ) {
        this.operationSelector = operationSelector;
        this.operationRoleExtractor = operationRoleExtractor;
        this.inboundCommandExtractor = inboundCommandExtractor;
    }

    public TaskWorkflowRoutePlan create(
            Service service,
            List<ChannelServiceDefinition> routeDefinitions
    ) {
        Map<TaskWorkflowRole, ServiceOperation> operationsByRole = operationsByRole(service);
        validateBusinessOperation(service, operationsByRole);

        EnumMap<TaskWorkflowCommand, TaskWorkflowCommandPlan> commandPlans =
                new EnumMap<>(TaskWorkflowCommand.class);
        List<InboundChannelServiceDefinition> inboundDefinitions =
                inboundDefinitions(service, routeDefinitions);
        for (InboundChannelServiceDefinition inboundDefinition : inboundDefinitions) {
            TaskWorkflowInboundCommandConfig config =
                    inboundCommandExtractor.extract(service, inboundDefinition);
            if (commandPlans.containsKey(config.command())) {
                throw new IllegalStateException("Duplicate TASK_WORKFLOW inboundAction for serviceCode="
                        + serviceCode(service) + ", command=" + config.command()
                        + ", channelServiceDefinitionId=" + inboundDefinition.getId());
            }
            List<TaskWorkflowInboundCommandStepConfig> sortedSteps = config.steps().stream()
                    .sorted(Comparator.comparingInt(
                            TaskWorkflowInboundCommandStepConfig::executionOrder))
                    .toList();
            rejectDuplicateExecutionOrders(service, config, sortedSteps);
            validateCommandShape(service, config, sortedSteps);
            List<TaskWorkflowStepPlan> stepPlans = sortedSteps.stream()
                    .map(step -> toStepPlan(service, config, step, operationsByRole))
                    .toList();
            commandPlans.put(config.command(), new TaskWorkflowCommandPlan(
                    config.command(),
                    stepPlans,
                    inboundDefinition
            ));
        }
        if (commandPlans.isEmpty()) {
            throw new IllegalStateException("TASK_WORKFLOW serviceCode=" + serviceCode(service)
                    + " has no INBOUND command definitions");
        }
        return new TaskWorkflowRoutePlan(service, commandPlans);
    }

    private Map<TaskWorkflowRole, ServiceOperation> operationsByRole(Service service) {
        EnumMap<TaskWorkflowRole, ServiceOperation> operationsByRole =
                new EnumMap<>(TaskWorkflowRole.class);
        List<ServiceOperation> activeOperations = operationSelector.active(service);
        if (activeOperations.isEmpty()) {
            throw new IllegalStateException("Routing strategy " + RoutingStrategy.TASK_WORKFLOW
                    + " requires active operations for serviceCode=" + serviceCode(service));
        }
        for (ServiceOperation operation : activeOperations) {
            if (StringUtils.isBlank(operation.getOperationName())) {
                throw new IllegalStateException("Invalid TASK_WORKFLOW operation for serviceCode="
                        + serviceCode(service) + ", operationName=<blank>: operationName is required");
            }
            TaskWorkflowRole role = operationRoleExtractor.extract(service, operation).role();
            ServiceOperation existing = operationsByRole.putIfAbsent(role, operation);
            if (existing != null) {
                throw new IllegalStateException("Invalid TASK_WORKFLOW operations for serviceCode="
                        + serviceCode(service) + ", role=" + role
                        + ": role is assigned to more than one active operation: "
                        + existing.getOperationName() + ", " + operation.getOperationName());
            }
        }
        return operationsByRole;
    }

    private void validateBusinessOperation(
            Service service,
            Map<TaskWorkflowRole, ServiceOperation> operationsByRole
    ) {
        if (!operationsByRole.containsKey(TaskWorkflowRole.BUSINESS_OPERATION)) {
            throw new IllegalStateException("Invalid TASK_WORKFLOW operations for serviceCode="
                    + serviceCode(service)
                    + ", role=BUSINESS_OPERATION: exactly one active BUSINESS_OPERATION is required");
        }
    }

    private List<InboundChannelServiceDefinition> inboundDefinitions(
            Service service,
            List<ChannelServiceDefinition> routeDefinitions
    ) {
        if (routeDefinitions == null) {
            return List.of();
        }
        return routeDefinitions.stream()
                .filter(definition -> definition != null
                        && definition.getType() == ChannelServiceDefinitionType.INBOUND)
                .map(definition -> {
                    if (definition instanceof InboundChannelServiceDefinition inboundDefinition) {
                        return inboundDefinition;
                    }
                    throw new IllegalStateException("Invalid TASK_WORKFLOW inbound definition for serviceCode="
                            + serviceCode(service) + ", channelServiceDefinitionId="
                            + definition.getId()
                            + ": INBOUND definition has an unsupported model type");
                })
                .toList();
    }

    private void rejectDuplicateExecutionOrders(
            Service service,
            TaskWorkflowInboundCommandConfig config,
            List<TaskWorkflowInboundCommandStepConfig> steps
    ) {
        Set<Integer> orders = new HashSet<>();
        for (TaskWorkflowInboundCommandStepConfig step : steps) {
            if (!orders.add(step.executionOrder())) {
                throw new IllegalStateException("Invalid TASK_WORKFLOW command configuration for serviceCode="
                        + serviceCode(service) + ", command=" + config.command()
                        + ", inboundAction=" + config.inboundAction()
                        + ", field=executionOrder: duplicate executionOrder="
                        + step.executionOrder());
            }
        }
    }

    private void validateCommandShape(
            Service service,
            TaskWorkflowInboundCommandConfig config,
            List<TaskWorkflowInboundCommandStepConfig> steps
    ) {
        if (config.command() != TaskWorkflowCommand.APPROVE_AND_EXECUTE) {
            if (steps.size() != 1) {
                throw new IllegalStateException("Invalid TASK_WORKFLOW command configuration for serviceCode="
                        + serviceCode(service) + ", command=" + config.command()
                        + ": simple commands must configure exactly one step; found "
                        + steps.size());
            }
            TaskWorkflowRole role = steps.getFirst().role();
            if (role == TaskWorkflowRole.COMPLETE_PROCESS) {
                throw new IllegalStateException("Invalid TASK_WORKFLOW command configuration for serviceCode="
                        + serviceCode(service) + ", command=" + config.command()
                        + ", role=COMPLETE_PROCESS: COMPLETE_PROCESS must not be exposed "
                        + "as a direct inbound step");
            }
            if (role == TaskWorkflowRole.BUSINESS_OPERATION) {
                throw new IllegalStateException("Invalid TASK_WORKFLOW command configuration for serviceCode="
                        + serviceCode(service) + ", command=" + config.command()
                        + ", role=BUSINESS_OPERATION: BUSINESS_OPERATION must execute "
                        + "through the coordinated APPROVE_AND_EXECUTE flow");
            }
            return;
        }

        List<TaskWorkflowRole> roles = steps.stream()
                .map(TaskWorkflowInboundCommandStepConfig::role)
                .toList();
        List<TaskWorkflowRole> required = List.of(
                TaskWorkflowRole.APPROVE_PROCESS,
                TaskWorkflowRole.BUSINESS_OPERATION,
                TaskWorkflowRole.COMPLETE_PROCESS
        );
        if (!roles.equals(required)) {
            throw new IllegalStateException("Invalid TASK_WORKFLOW command configuration for serviceCode="
                    + serviceCode(service) + ", command=" + config.command()
                    + ": APPROVE_AND_EXECUTE steps must be ordered exactly as "
                    + required + "; configuredRoles=" + roles);
        }
    }

    private TaskWorkflowStepPlan toStepPlan(
            Service service,
            TaskWorkflowInboundCommandConfig config,
            TaskWorkflowInboundCommandStepConfig step,
            Map<TaskWorkflowRole, ServiceOperation> operationsByRole
    ) {
        ServiceOperation operation = operationsByRole.get(step.role());
        if (operation == null) {
            throw new IllegalStateException("Invalid TASK_WORKFLOW command configuration for serviceCode="
                    + serviceCode(service) + ", command=" + config.command()
                    + ", inboundAction=" + config.inboundAction()
                    + ", role=" + step.role()
                    + ": no matching active ServiceOperation");
        }
        String endpointUri = "direct:"
                + RouteIdSupport.operationRouteId(operation.getOperationName());
        return new TaskWorkflowStepPlan(
                step.role(),
                step.executionOrder(),
                operation,
                endpointUri
        );
    }

    private String serviceCode(Service service) {
        return service == null ? "<null>" : String.valueOf(service.getCode());
    }
}
