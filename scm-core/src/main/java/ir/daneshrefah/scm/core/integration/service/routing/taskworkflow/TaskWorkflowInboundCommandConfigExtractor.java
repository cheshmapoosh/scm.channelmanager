package ir.daneshrefah.scm.core.integration.service.routing.taskworkflow;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.model.definition.Definition;
import ir.daneshrefah.scm.common.model.gateway.InboundChannelServiceDefinition;
import ir.daneshrefah.scm.common.model.gateway.Service;
import ir.daneshrefah.scm.common.model.gateway.RoutingStrategy;
import ir.daneshrefah.scm.common.model.taskworkflow.TaskWorkflowStepType;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Component
public class TaskWorkflowInboundCommandConfigExtractor {
    private static final String INBOUND_ACTION = "inboundAction";
    private static final String TASK_WORKFLOW = "taskWorkflow";
    private static final String STEPS = "steps";
    private static final String STEP_TYPE = "stepType";
    private static final String ROUTING_STRATEGY = "routingStrategy";
    private static final String OPERATION_NAME = "operationName";
    private static final String DECISION_POLICY = "decisionPolicy";
    private static final Set<String> SUPPORTED_STEP_FIELDS =
            Set.of(STEP_TYPE, OPERATION_NAME, DECISION_POLICY);

    private final ObjectMapper objectMapper;
    private final TaskWorkflowCommandResolver commandResolver;

    public TaskWorkflowInboundCommandConfigExtractor(
            ObjectMapper objectMapper,
            TaskWorkflowCommandResolver commandResolver
    ) {
        this.objectMapper = objectMapper;
        this.commandResolver = commandResolver;
    }

    public TaskWorkflowInboundCommandConfig extract(
            Service service,
            InboundChannelServiceDefinition inboundDefinition
    ) {
        Definition definition = inboundDefinition == null ? null : inboundDefinition.getDefinition();
        if (definition == null) {
            throw invalid(service, inboundDefinition, null, "definition",
                    "TASK_WORKFLOW INBOUND row must have its own Definition");
        }
        if (StringUtils.isBlank(definition.getDetails())) {
            throw invalid(service, inboundDefinition, definition, "details",
                    "inbound Definition.details must not be blank");
        }
        JsonNode details = parse(service, inboundDefinition, definition);
        if (!details.isObject()) {
            throw invalid(service, inboundDefinition, definition, "details",
                    "inbound Definition.details must be a JSON object");
        }

        String inboundAction = requiredText(
                service, inboundDefinition, definition, details, INBOUND_ACTION);
        if ("COMPLETE_PROCESS".equals(normalize(inboundAction))) {
            throw invalid(service, inboundDefinition, definition, INBOUND_ACTION,
                    "COMPLETE_PROCESS must not be exposed as a direct inbound command");
        }
        TaskWorkflowCommand command;
        try {
            command = commandResolver.resolve(inboundAction);
        } catch (IllegalStateException exception) {
            throw invalid(service, inboundDefinition, definition, INBOUND_ACTION,
                    "invalid inboundAction=" + inboundAction, exception);
        }

        JsonNode taskWorkflow = details.get(TASK_WORKFLOW);
        if (taskWorkflow == null || !taskWorkflow.isObject()) {
            throw invalid(service, inboundDefinition, definition, TASK_WORKFLOW,
                    "taskWorkflow object is required");
        }
        if (taskWorkflow.has("executionStrategy")) {
            throw invalid(service, inboundDefinition, definition,
                    "taskWorkflow.executionStrategy", "executionStrategy is not supported; use routingStrategy");
        }
        String routingStrategyValue = requiredText(service, inboundDefinition, definition,
                taskWorkflow, "taskWorkflow." + ROUTING_STRATEGY, ROUTING_STRATEGY);
        RoutingStrategy routingStrategy;
        try {
            routingStrategy = RoutingStrategy.valueOf(normalize(routingStrategyValue));
        } catch (IllegalArgumentException exception) {
            throw invalid(service, inboundDefinition, definition,
                    "taskWorkflow." + ROUTING_STRATEGY,
                    "invalid routingStrategy=" + routingStrategyValue, exception);
        }
        if (routingStrategy != RoutingStrategy.FIRST
                && routingStrategy != RoutingStrategy.CHAIN_ON_APPROVE) {
            throw invalid(service, inboundDefinition, definition,
                    "taskWorkflow." + ROUTING_STRATEGY,
                    "routingStrategy must be FIRST or CHAIN_ON_APPROVE");
        }
        JsonNode stepsNode = taskWorkflow.get(STEPS);
        if (stepsNode == null || !stepsNode.isArray() || stepsNode.isEmpty()) {
            throw invalid(service, inboundDefinition, definition, "taskWorkflow.steps",
                    "taskWorkflow.steps must be a non-empty array");
        }

        List<TaskWorkflowInboundCommandStepConfig> steps = new ArrayList<>();
        for (int index = 0; index < stepsNode.size(); index++) {
            JsonNode step = stepsNode.get(index);
            if (!step.isObject()) {
                throw invalid(service, inboundDefinition, definition,
                        "taskWorkflow.steps[" + index + "]", "step must be a JSON object");
            }
            validateSupportedStepFields(service, inboundDefinition, definition, step, index);
            String stepTypeValue = requiredText(
                    service, inboundDefinition, definition, step,
                    "taskWorkflow.steps[" + index + "]." + STEP_TYPE, STEP_TYPE);
            TaskWorkflowStepType stepType;
            try {
                stepType = TaskWorkflowStepType.valueOf(normalize(stepTypeValue));
            } catch (IllegalArgumentException exception) {
                throw invalid(service, inboundDefinition, definition,
                        "taskWorkflow.steps[" + index + "]." + STEP_TYPE,
                        "invalid stepType=" + stepTypeValue, exception);
            }
            String operationName = requiredText(service, inboundDefinition, definition, step,
                    "taskWorkflow.steps[" + index + "]." + OPERATION_NAME, OPERATION_NAME);
            JsonNode decisionPolicyNode = step.get(DECISION_POLICY);
            if (decisionPolicyNode != null && !decisionPolicyNode.isNull()
                    && !decisionPolicyNode.isTextual()) {
                throw invalid(service, inboundDefinition, definition,
                        "taskWorkflow.steps[" + index + "]." + DECISION_POLICY,
                        "decisionPolicy must be a string when supplied");
            }
            String decisionPolicy = decisionPolicyNode == null || decisionPolicyNode.isNull()
                    ? null
                    : decisionPolicyNode.asText();
            if (decisionPolicy != null && StringUtils.isBlank(decisionPolicy)) {
                throw invalid(service, inboundDefinition, definition,
                        "taskWorkflow.steps[" + index + "]." + DECISION_POLICY,
                        "decisionPolicy must be a non-blank string when supplied");
            }
            steps.add(new TaskWorkflowInboundCommandStepConfig(
                    stepType,
                    operationName.trim(),
                    decisionPolicy == null ? null : decisionPolicy.trim()
            ));
        }
        return new TaskWorkflowInboundCommandConfig(
                command,
                inboundAction.trim(),
                routingStrategy,
                steps,
                inboundDefinition
        );
    }

    private void validateSupportedStepFields(
            Service service,
            InboundChannelServiceDefinition inboundDefinition,
            Definition definition,
            JsonNode step,
            int index
    ) {
        step.fieldNames().forEachRemaining(field -> {
            if (!SUPPORTED_STEP_FIELDS.contains(field)) {
                throw invalid(service, inboundDefinition, definition,
                        "taskWorkflow.steps[" + index + "]." + field,
                        "unsupported step field=" + field);
            }
        });
    }

    private JsonNode parse(
            Service service,
            InboundChannelServiceDefinition inboundDefinition,
            Definition definition
    ) {
        try {
            return objectMapper.readTree(definition.getDetails());
        } catch (JsonProcessingException exception) {
            throw invalid(service, inboundDefinition, definition, "details",
                    "inbound Definition.details must contain valid JSON", exception);
        }
    }

    private String requiredText(
            Service service,
            InboundChannelServiceDefinition inboundDefinition,
            Definition definition,
            JsonNode parent,
            String field
    ) {
        return requiredText(service, inboundDefinition, definition, parent, field, field);
    }

    private String requiredText(
            Service service,
            InboundChannelServiceDefinition inboundDefinition,
            Definition definition,
            JsonNode parent,
            String errorField,
            String jsonField
    ) {
        JsonNode value = parent.get(jsonField);
        if (value == null || !value.isTextual() || StringUtils.isBlank(value.asText())) {
            throw invalid(service, inboundDefinition, definition, errorField,
                    jsonField + " is required and must be a non-blank string");
        }
        return value.asText();
    }

    private String normalize(String value) {
        return value.trim().toUpperCase(Locale.ROOT)
                .replace('-', '_')
                .replace(' ', '_');
    }

    private IllegalStateException invalid(
            Service service,
            InboundChannelServiceDefinition inboundDefinition,
            Definition definition,
            String field,
            String message
    ) {
        return invalid(service, inboundDefinition, definition, field, message, null);
    }

    private IllegalStateException invalid(
            Service service,
            InboundChannelServiceDefinition inboundDefinition,
            Definition definition,
            String field,
            String message,
            Throwable cause
    ) {
        String text = "Invalid TASK_WORKFLOW inbound command configuration for serviceCode="
                + value(service == null ? null : service.getCode())
                + ", channelServiceDefinitionId="
                + value(inboundDefinition == null ? null : inboundDefinition.getId())
                + ", definitionId=" + value(definition == null ? null : definition.getId())
                + ", definitionName=" + value(definition == null ? null : definition.getName())
                + ", field=" + field + ": " + message;
        return cause == null ? new IllegalStateException(text) : new IllegalStateException(text, cause);
    }

    private String value(Object value) {
        return value == null ? "<null>" : String.valueOf(value);
    }
}
