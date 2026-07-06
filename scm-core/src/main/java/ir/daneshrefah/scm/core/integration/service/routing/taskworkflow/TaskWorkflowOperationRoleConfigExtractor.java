package ir.daneshrefah.scm.core.integration.service.routing.taskworkflow;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.model.definition.Definition;
import ir.daneshrefah.scm.common.model.gateway.Service;
import ir.daneshrefah.scm.common.model.gateway.ServiceOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class TaskWorkflowOperationRoleConfigExtractor {
    private static final String TASK_WORKFLOW_ROLE = "taskWorkflowRole";

    private final ObjectMapper objectMapper;

    public TaskWorkflowOperationRoleConfigExtractor(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public TaskWorkflowOperationRoleConfig extract(
            Service service,
            ServiceOperation operation
    ) {
        String operationName = operation == null
                ? null
                : StringUtils.trimToNull(operation.getOperationName());
        if (operationName == null) {
            throw invalid(service, operation, null, "operationName",
                    "active TASK_WORKFLOW operationName must not be blank");
        }
        if (StringUtils.startsWithIgnoreCase(operationName, "op.")) {
            throw invalid(service, operation, null, "operationName",
                    "store the operation code without the generated op. route prefix");
        }
        Definition definition = operation == null ? null : operation.getDefinition();
        if (definition == null) {
            throw invalid(service, operation, null, "definition",
                    "active TASK_WORKFLOW operation must have a Definition");
        }
        if (StringUtils.isBlank(definition.getDetails())) {
            throw invalid(service, operation, definition, "details",
                    "operation Definition.details must not be blank");
        }
        JsonNode details = parse(service, operation, definition);
        if (!details.isObject()) {
            throw invalid(service, operation, definition, "details",
                    "operation Definition.details must be a JSON object");
        }
        JsonNode roleNode = details.get(TASK_WORKFLOW_ROLE);
        if (roleNode == null || !roleNode.isTextual()
                || StringUtils.isBlank(roleNode.asText())) {
            throw invalid(service, operation, definition, TASK_WORKFLOW_ROLE,
                    "taskWorkflowRole is required and must be a non-blank string");
        }
        String roleName = normalize(roleNode.asText());
        try {
            return new TaskWorkflowOperationRoleConfig(TaskWorkflowRole.valueOf(roleName));
        } catch (IllegalArgumentException exception) {
            throw invalid(service, operation, definition, TASK_WORKFLOW_ROLE,
                    "invalid taskWorkflowRole=" + roleNode.asText(), exception);
        }
    }

    private JsonNode parse(
            Service service,
            ServiceOperation operation,
            Definition definition
    ) {
        try {
            return objectMapper.readTree(definition.getDetails());
        } catch (JsonProcessingException exception) {
            throw invalid(service, operation, definition, "details",
                    "operation Definition.details must contain valid JSON", exception);
        }
    }

    private String normalize(String value) {
        return value.trim().toUpperCase(Locale.ROOT)
                .replace('-', '_')
                .replace(' ', '_');
    }

    private IllegalStateException invalid(
            Service service,
            ServiceOperation operation,
            Definition definition,
            String field,
            String message
    ) {
        return invalid(service, operation, definition, field, message, null);
    }

    private IllegalStateException invalid(
            Service service,
            ServiceOperation operation,
            Definition definition,
            String field,
            String message,
            Throwable cause
    ) {
        String text = "Invalid TASK_WORKFLOW operation configuration for serviceCode="
                + value(service == null ? null : service.getCode())
                + ", operationName=" + value(operation == null ? null : operation.getOperationName())
                + ", definitionId=" + value(definition == null ? null : definition.getId())
                + ", definitionName=" + value(definition == null ? null : definition.getName())
                + ", field=" + field + ": " + message;
        return cause == null ? new IllegalStateException(text) : new IllegalStateException(text, cause);
    }

    private String value(Object value) {
        return value == null ? "<null>" : String.valueOf(value);
    }
}
