package ir.daneshrefah.scm.core.integration.service.routing.taskworkflow;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.model.definition.Definition;
import ir.daneshrefah.scm.common.model.definition.DefinitionType;
import ir.daneshrefah.scm.common.model.gateway.RoutingStrategy;
import ir.daneshrefah.scm.common.model.gateway.Service;
import ir.daneshrefah.scm.common.model.gateway.ServiceOperation;
import ir.daneshrefah.scm.common.model.taskworkflow.TaskWorkflowStepType;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Component
public class TaskWorkflowActionPlanParser {
    private static final String INBOUND_ACTION = "inboundAction";
    private static final String ACTION_PLAN = "actionPlan";
    private static final String NAME = "name";
    private static final String ROUTING_STRATEGY = "routingStrategy";
    private static final String STEPS = "steps";
    private static final String STEP_ID = "stepId";
    private static final String STEP_TYPE = "stepType";
    private static final String OPERATION_NAME = "operationName";
    private static final String DECISION_POLICY = "decisionPolicy";
    private static final Set<String> SUPPORTED_ROOT_FIELDS =
            Set.of(INBOUND_ACTION, ACTION_PLAN);
    private static final Set<String> SUPPORTED_ACTION_PLAN_FIELDS =
            Set.of(NAME, ROUTING_STRATEGY, STEPS);
    private static final Set<String> SUPPORTED_STEP_FIELDS =
            Set.of(STEP_ID, STEP_TYPE, OPERATION_NAME, DECISION_POLICY);

    private final ObjectMapper objectMapper;
    private final TaskWorkflowCommandResolver commandResolver;

    public TaskWorkflowActionPlanParser(
            ObjectMapper objectMapper,
            TaskWorkflowCommandResolver commandResolver
    ) {
        this.objectMapper = objectMapper;
        this.commandResolver = commandResolver;
    }

    public TaskWorkflowActionPlanConfig parse(
            Service service,
            ServiceOperation serviceOperation
    ) {
        Definition definition = requireActionPlanDefinition(
                service,
                serviceOperation
        );
        String serviceCode = requiredModelValue(
                service,
                serviceOperation,
                definition,
                "service.code",
                service == null ? null : service.getCode()
        );
        String sourceServiceOperationId = requiredModelValue(
                service,
                serviceOperation,
                definition,
                "serviceOperation.id",
                serviceOperation.getId()
        );
        requiredModelValue(
                service,
                serviceOperation,
                definition,
                "serviceOperation.operationName",
                serviceOperation.getOperationName()
        );
        String definitionId = requiredModelValue(
                service,
                serviceOperation,
                definition,
                "definition.id",
                definition.getId()
        );
        if (StringUtils.isBlank(definition.getDetails())) {
            throw invalid(service, serviceOperation, definition, "details",
                    "ACTION_PLAN Definition.details must not be blank");
        }

        JsonNode root = parseDetails(service, serviceOperation, definition);
        if (!root.isObject()) {
            throw invalid(service, serviceOperation, definition, "details",
                    "Definition.details must be a JSON object");
        }
        validateFields(
                service,
                serviceOperation,
                definition,
                root,
                SUPPORTED_ROOT_FIELDS,
                "root"
        );

        String inboundAction = requiredText(
                service,
                serviceOperation,
                definition,
                root,
                INBOUND_ACTION,
                INBOUND_ACTION
        );
        TaskWorkflowCommand command;
        try {
            command = commandResolver.resolve(inboundAction);
        } catch (RuntimeException exception) {
            throw invalid(
                    service,
                    serviceOperation,
                    definition,
                    INBOUND_ACTION,
                    "unsupported inboundAction=" + inboundAction,
                    exception
            );
        }

        JsonNode actionPlan = root.get(ACTION_PLAN);
        if (actionPlan == null || !actionPlan.isObject()) {
            throw invalid(service, serviceOperation, definition, ACTION_PLAN,
                    "actionPlan is required and must be a JSON object");
        }
        validateFields(
                service,
                serviceOperation,
                definition,
                actionPlan,
                SUPPORTED_ACTION_PLAN_FIELDS,
                ACTION_PLAN
        );

        String actionPlanName = requiredText(
                service,
                serviceOperation,
                definition,
                actionPlan,
                ACTION_PLAN + "." + NAME,
                NAME
        );
        RoutingStrategy routingStrategy = routingStrategy(
                service,
                serviceOperation,
                definition,
                actionPlan
        );
        List<TaskWorkflowActionPlanStepConfig> steps = steps(
                service,
                serviceOperation,
                definition,
                actionPlan
        );

        return new TaskWorkflowActionPlanConfig(
                command,
                serviceCode,
                inboundAction.trim(),
                actionPlanName.trim(),
                sourceServiceOperationId,
                definitionId,
                routingStrategy,
                steps,
                serviceOperation
        );
    }

    private Definition requireActionPlanDefinition(
            Service service,
            ServiceOperation serviceOperation
    ) {
        if (serviceOperation == null) {
            throw invalid(service, null, null, "serviceOperation",
                    "service operation is required");
        }
        Definition definition = serviceOperation.getDefinition();
        if (definition == null) {
            throw invalid(service, serviceOperation, null, "definition",
                    "service operation must reference a Definition");
        }
        if (definition.getType() != DefinitionType.ACTION_PLAN) {
            throw invalid(service, serviceOperation, definition,
                    "definition.type",
                    "definition.type must be ACTION_PLAN");
        }
        return definition;
    }

    private RoutingStrategy routingStrategy(
            Service service,
            ServiceOperation serviceOperation,
            Definition definition,
            JsonNode actionPlan
    ) {
        String value = requiredText(
                service,
                serviceOperation,
                definition,
                actionPlan,
                ACTION_PLAN + "." + ROUTING_STRATEGY,
                ROUTING_STRATEGY
        );
        RoutingStrategy strategy;
        try {
            strategy = RoutingStrategy.valueOf(normalizeEnum(value));
        } catch (IllegalArgumentException exception) {
            throw invalid(service, serviceOperation, definition,
                    ACTION_PLAN + "." + ROUTING_STRATEGY,
                    "unsupported routingStrategy=" + value, exception);
        }
        if (strategy != RoutingStrategy.FIRST
                && strategy != RoutingStrategy.CHAIN_ON_APPROVE) {
            throw invalid(service, serviceOperation, definition,
                    ACTION_PLAN + "." + ROUTING_STRATEGY,
                    "routingStrategy must be FIRST or CHAIN_ON_APPROVE");
        }
        return strategy;
    }

    private List<TaskWorkflowActionPlanStepConfig> steps(
            Service service,
            ServiceOperation serviceOperation,
            Definition definition,
            JsonNode actionPlan
    ) {
        JsonNode stepsNode = actionPlan.get(STEPS);
        if (stepsNode == null || !stepsNode.isArray() || stepsNode.isEmpty()) {
            throw invalid(service, serviceOperation, definition,
                    ACTION_PLAN + "." + STEPS,
                    "steps must be a non-empty array");
        }
        List<TaskWorkflowActionPlanStepConfig> result = new ArrayList<>();
        Set<String> stepIds = new HashSet<>();
        for (int index = 0; index < stepsNode.size(); index++) {
            JsonNode step = stepsNode.get(index);
            String path = ACTION_PLAN + "." + STEPS + "[" + index + "]";
            if (!step.isObject()) {
                throw invalid(service, serviceOperation, definition, path,
                        "step must be a JSON object");
            }
            validateFields(
                    service,
                    serviceOperation,
                    definition,
                    step,
                    SUPPORTED_STEP_FIELDS,
                    path
            );
            String stepId = requiredText(
                    service,
                    serviceOperation,
                    definition,
                    step,
                    path + "." + STEP_ID,
                    STEP_ID
            );
            if (!stepIds.add(stepId.toLowerCase(Locale.ROOT))) {
                throw invalid(service, serviceOperation, definition,
                        path + "." + STEP_ID,
                        "stepId must be unique within the action plan");
            }
            String stepTypeText = requiredText(
                    service,
                    serviceOperation,
                    definition,
                    step,
                    path + "." + STEP_TYPE,
                    STEP_TYPE
            );
            TaskWorkflowStepType stepType;
            try {
                stepType = TaskWorkflowStepType.valueOf(
                        normalizeEnum(stepTypeText));
            } catch (IllegalArgumentException exception) {
                throw invalid(service, serviceOperation, definition,
                        path + "." + STEP_TYPE,
                        "unsupported stepType=" + stepTypeText, exception);
            }
            String operationName = requiredText(
                    service,
                    serviceOperation,
                    definition,
                    step,
                    path + "." + OPERATION_NAME,
                    OPERATION_NAME
            );
            String decisionPolicy = optionalText(
                    service,
                    serviceOperation,
                    definition,
                    step,
                    path + "." + DECISION_POLICY,
                    DECISION_POLICY
            );
            result.add(new TaskWorkflowActionPlanStepConfig(
                    stepId,
                    index,
                    stepType,
                    operationName,
                    decisionPolicy
            ));
        }
        return List.copyOf(result);
    }

    private void validateFields(
            Service service,
            ServiceOperation serviceOperation,
            Definition definition,
            JsonNode node,
            Set<String> supported,
            String path
    ) {
        node.fieldNames().forEachRemaining(field -> {
            if (!supported.contains(field)) {
                throw invalid(service, serviceOperation, definition,
                        path + "." + field,
                        "unsupported action-plan field=" + field);
            }
        });
    }

    private JsonNode parseDetails(
            Service service,
            ServiceOperation serviceOperation,
            Definition definition
    ) {
        try {
            return objectMapper.readTree(definition.getDetails());
        } catch (JsonProcessingException exception) {
            throw invalid(service, serviceOperation, definition, "details",
                    "Definition.details must contain valid JSON", exception);
        }
    }

    private String requiredModelValue(
            Service service,
            ServiceOperation serviceOperation,
            Definition definition,
            String field,
            String value
    ) {
        String normalized = StringUtils.trimToNull(value);
        if (normalized == null) {
            throw invalid(service, serviceOperation, definition, field,
                    field + " must not be blank");
        }
        return normalized;
    }

    private String requiredText(
            Service service,
            ServiceOperation serviceOperation,
            Definition definition,
            JsonNode parent,
            String errorField,
            String jsonField
    ) {
        String value = optionalText(
                service,
                serviceOperation,
                definition,
                parent,
                errorField,
                jsonField
        );
        if (value == null) {
            throw invalid(service, serviceOperation, definition, errorField,
                    jsonField + " is required and must be a non-blank string");
        }
        return value;
    }

    private String optionalText(
            Service service,
            ServiceOperation serviceOperation,
            Definition definition,
            JsonNode parent,
            String errorField,
            String jsonField
    ) {
        JsonNode value = parent.get(jsonField);
        if (value == null || value.isNull()) {
            return null;
        }
        if (!value.isTextual() || StringUtils.isBlank(value.asText())) {
            throw invalid(service, serviceOperation, definition, errorField,
                    jsonField + " must be a non-blank string when supplied");
        }
        return value.asText().trim();
    }

    private String normalizeEnum(String value) {
        return value.trim().toUpperCase(Locale.ROOT)
                .replace('-', '_')
                .replace(' ', '_');
    }

    private IllegalStateException invalid(
            Service service,
            ServiceOperation serviceOperation,
            Definition definition,
            String field,
            String message
    ) {
        return invalid(
                service,
                serviceOperation,
                definition,
                field,
                message,
                null
        );
    }

    private IllegalStateException invalid(
            Service service,
            ServiceOperation serviceOperation,
            Definition definition,
            String field,
            String message,
            Throwable cause
    ) {
        String text = "Invalid TASK_WORKFLOW action plan serviceCode="
                + value(service == null ? null : service.getCode())
                + ", sourceServiceOperationId="
                + value(serviceOperation == null
                        ? null
                        : serviceOperation.getId())
                + ", technicalOperationName="
                + value(serviceOperation == null
                        ? null
                        : serviceOperation.getOperationName())
                + ", definitionId="
                + value(definition == null ? null : definition.getId())
                + ", field=" + field + ": " + message;
        return cause == null
                ? new IllegalStateException(text)
                : new IllegalStateException(text, cause);
    }

    private String value(Object value) {
        return value == null ? "<null>" : String.valueOf(value);
    }
}
