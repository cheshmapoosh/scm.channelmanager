package ir.daneshrefah.scm.core.integration.service.routing.taskworkflow;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.model.definition.Definition;
import ir.daneshrefah.scm.common.model.gateway.InboundChannelServiceDefinition;
import ir.daneshrefah.scm.common.model.gateway.Service;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
public class TaskWorkflowInboundCommandConfigExtractor {
    private static final String INBOUND_ACTION = "inboundAction";
    private static final String TASK_WORKFLOW = "taskWorkflow";
    private static final String STEPS = "steps";
    private static final String ROLE = "role";
    private static final String EXECUTION_ORDER = "executionOrder";

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
            String roleValue = requiredText(
                    service, inboundDefinition, definition, step,
                    "taskWorkflow.steps[" + index + "]." + ROLE, ROLE);
            TaskWorkflowRole role;
            try {
                role = TaskWorkflowRole.valueOf(normalize(roleValue));
            } catch (IllegalArgumentException exception) {
                throw invalid(service, inboundDefinition, definition,
                        "taskWorkflow.steps[" + index + "]." + ROLE,
                        "invalid role=" + roleValue, exception);
            }
            JsonNode executionOrder = step.get(EXECUTION_ORDER);
            if (executionOrder == null || !executionOrder.isIntegralNumber()
                    || !executionOrder.canConvertToInt()) {
                throw invalid(service, inboundDefinition, definition,
                        "taskWorkflow.steps[" + index + "]." + EXECUTION_ORDER,
                        "executionOrder is required and must be an integer");
            }
            steps.add(new TaskWorkflowInboundCommandStepConfig(
                    role,
                    executionOrder.intValue()
            ));
        }
        return new TaskWorkflowInboundCommandConfig(
                command,
                inboundAction.trim(),
                steps,
                inboundDefinition
        );
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
