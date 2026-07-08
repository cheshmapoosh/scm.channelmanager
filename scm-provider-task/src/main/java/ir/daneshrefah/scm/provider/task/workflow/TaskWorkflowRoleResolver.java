package ir.daneshrefah.scm.provider.task.workflow;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.constant.OperationCode;
import ir.daneshrefah.scm.common.model.definition.Definition;
import ir.daneshrefah.scm.common.model.gateway.ServiceOperation;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.operation.Operation;
import ir.daneshrefah.scm.common.model.operation.OperationDefinition;
import org.apache.camel.Exchange;
import org.springframework.util.StringUtils;

import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class TaskWorkflowRoleResolver {
    private static final String TASK_WORKFLOW_ROLE_DETAIL = "taskWorkflowRole";
    private static final Set<TaskWorkflowRole> SUPPORTED_ROLES = EnumSet.allOf(TaskWorkflowRole.class);
    private static final Map<String, TaskWorkflowRole> ROLE_ALIASES = Map.of(
            "FIND_TASKS", TaskWorkflowRole.FIND_ALL_TASK,
            "FIND_PROCESSES", TaskWorkflowRole.FIND_ALL_PROCESS,
            "FIND_TASKS_BY_PROCESS_ID", TaskWorkflowRole.FIND_TASK_BY_PROCESS_ID
    );
    private static final Map<String, TaskWorkflowRole> DEPRECATED_OPERATION_CODE_ALIASES =
            deprecatedAliases();

    private final ObjectMapper objectMapper;

    public TaskWorkflowRoleResolver(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public TaskWorkflowRole resolve(String providerCode, Exchange exchange) {
        TaskWorkflowRole role = roleFromExplicitContext(
                providerCode,
                exchange.getProperty(Message.TASK_WORKFLOW_ROLE),
                Message.TASK_WORKFLOW_ROLE
        );
        if (role != null) {
            return role;
        }

        role = roleFromExplicitContext(
                providerCode,
                exchange.getProperty("scmTaskWorkflowRole"),
                "scmTaskWorkflowRole"
        );
        if (role != null) {
            return role;
        }

        role = roleFromServiceOperation(providerCode, exchange);
        if (role != null) {
            return role;
        }

        role = roleFromOperationDefinitions(providerCode, exchange);
        if (role != null) {
            return role;
        }

        role = roleFromOperationName(exchange.getProperty(Message.OPERATION_NAME));
        if (role != null) {
            return role;
        }

        Object operationValue = exchange.getProperty(Message.OPERATION);
        if (operationValue instanceof Operation operation) {
            role = roleFromOperationName(operation.getName());
            if (role != null) {
                return role;
            }
        }

        role = deprecatedAliasFromOperationContext(exchange);
        if (role != null) {
            return role;
        }

        throw new IllegalArgumentException("scm-task:" + providerCode
                + " requires TaskWorkflowRole from exchange context or operation metadata.");
    }

    public boolean isDeprecatedOperationCodeAlias(String value) {
        String normalized = normalize(value);
        return normalized != null && DEPRECATED_OPERATION_CODE_ALIASES.containsKey(normalized);
    }

    private TaskWorkflowRole roleFromExplicitContext(
            String providerCode,
            Object value,
            String source
    ) {
        if (value == null) {
            return null;
        }
        TaskWorkflowRole role = roleFromContextValue(value);
        if (role == null) {
            throw invalidRole(providerCode, source, value);
        }
        return role;
    }

    private TaskWorkflowRole roleFromContextValue(Object value) {
        if (value instanceof TaskWorkflowRole role) {
            return role;
        }
        if (value instanceof Enum<?> enumValue) {
            return semanticRole(enumValue.name());
        }
        return semanticRole(String.valueOf(value));
    }

    private TaskWorkflowRole roleFromServiceOperation(String providerCode, Exchange exchange) {
        ServiceOperation serviceOperation = exchange.getProperty(
                Message.SERVICE_OPERATION,
                ServiceOperation.class
        );
        if (serviceOperation == null) {
            return null;
        }
        return roleFromDefinition(providerCode, serviceOperation.getDefinition(),
                Message.SERVICE_OPERATION + ".definition");
    }

    private TaskWorkflowRole roleFromOperationDefinitions(String providerCode, Exchange exchange) {
        Operation operation = exchange.getProperty(Message.OPERATION, Operation.class);
        if (operation == null || operation.getDefinitions() == null) {
            return null;
        }
        for (OperationDefinition operationDefinition : operation.getDefinitions()) {
            if (operationDefinition == null) {
                continue;
            }
            TaskWorkflowRole role = roleFromDefinition(
                    providerCode,
                    operationDefinition.getDefinition(),
                    Message.OPERATION + ".definitions"
            );
            if (role != null) {
                return role;
            }
        }
        return null;
    }

    private TaskWorkflowRole roleFromDefinition(
            String providerCode,
            Definition definition,
            String source
    ) {
        if (definition == null || !StringUtils.hasText(definition.getDetails())) {
            return null;
        }
        String detailsText = definition.getDetails();
        if (!detailsText.contains(TASK_WORKFLOW_ROLE_DETAIL)
                && !detailsText.contains(Message.TASK_WORKFLOW_ROLE)) {
            return null;
        }
        JsonNode details = parseDetails(providerCode, definition, source);
        if (!details.isObject()) {
            return null;
        }
        JsonNode roleNode = details.get(TASK_WORKFLOW_ROLE_DETAIL);
        if (roleNode == null) {
            roleNode = details.get(Message.TASK_WORKFLOW_ROLE);
        }
        if (roleNode == null || roleNode.isNull()) {
            return null;
        }
        if (!roleNode.isTextual() || !StringUtils.hasText(roleNode.asText())) {
            throw invalidRole(providerCode, source + "." + TASK_WORKFLOW_ROLE_DETAIL, roleNode);
        }
        TaskWorkflowRole role = semanticRole(roleNode.asText());
        if (role == null) {
            throw invalidRole(providerCode, source + "." + TASK_WORKFLOW_ROLE_DETAIL, roleNode.asText());
        }
        return role;
    }

    private JsonNode parseDetails(
            String providerCode,
            Definition definition,
            String source
    ) {
        try {
            return objectMapper.readTree(definition.getDetails());
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("Invalid TaskWorkflowRole metadata for scm-task:"
                    + providerCode + " from " + source
                    + ": Definition.details must contain valid JSON", exception);
        }
    }

    private TaskWorkflowRole roleFromOperationName(Object value) {
        String normalized = normalize(value);
        if (normalized == null || DEPRECATED_OPERATION_CODE_ALIASES.containsKey(normalized)) {
            return null;
        }
        return semanticRole(normalized);
    }

    private TaskWorkflowRole deprecatedAliasFromOperationContext(Exchange exchange) {
        TaskWorkflowRole role = deprecatedOperationCodeAlias(exchange.getProperty(Message.OPERATION_NAME));
        if (role != null) {
            return role;
        }

        Object operationValue = exchange.getProperty(Message.OPERATION);
        if (operationValue instanceof Operation operation) {
            role = deprecatedOperationCodeAlias(operation.getName());
            if (role != null) {
                return role;
            }
        } else if (operationValue instanceof OperationCode operationCode) {
            role = deprecatedOperationCodeAlias(operationCode.name());
            if (role != null) {
                return role;
            }
        }
        return null;
    }

    private TaskWorkflowRole deprecatedOperationCodeAlias(Object value) {
        String normalized = normalize(value);
        return normalized == null ? null : DEPRECATED_OPERATION_CODE_ALIASES.get(normalized);
    }

    private TaskWorkflowRole semanticRole(Object value) {
        String normalized = normalize(value);
        if (normalized == null) {
            return null;
        }
        TaskWorkflowRole alias = ROLE_ALIASES.get(normalized);
        if (alias != null) {
            return alias;
        }
        try {
            return TaskWorkflowRole.valueOf(normalized);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private String normalize(Object value) {
        if (value == null) {
            return null;
        }
        String text = String.valueOf(value);
        if (!StringUtils.hasText(text)) {
            return null;
        }
        return text.trim()
                .toUpperCase(Locale.ROOT)
                .replace('-', '_')
                .replace(' ', '_');
    }

    private IllegalArgumentException invalidRole(
            String providerCode,
            String source,
            Object value
    ) {
        return new IllegalArgumentException("Invalid TaskWorkflowRole value="
                + value + " from " + source + " for scm-task:" + providerCode
                + "; expected one of " + SUPPORTED_ROLES);
    }

    private static Map<String, TaskWorkflowRole> deprecatedAliases() {
        Map<String, TaskWorkflowRole> aliases = new LinkedHashMap<>();
        aliases.put("SVC_CARTABLE_START_PROCESS", TaskWorkflowRole.START_PROCESS);
        aliases.put("SVC_CARTABLE_APPROVE_PROCESS", TaskWorkflowRole.APPROVE_PROCESS);
        aliases.put("SVC_CARTABLE_COMPLETE_PROCESS", TaskWorkflowRole.COMPLETE_PROCESS);
        aliases.put("SVC_CARTABLE_CANCEL_PROCESS", TaskWorkflowRole.CANCEL_PROCESS);
        aliases.put("SVC_CARTABLE_COMPLTE_TASK", TaskWorkflowRole.COMPLETE_TASK);
        aliases.put("SVC_CARTABLE_GET_ALL_TASK", TaskWorkflowRole.FIND_ALL_TASK);
        aliases.put("SVC_CARTABLE_GET_ALL_PROCESS", TaskWorkflowRole.FIND_ALL_PROCESS);
        aliases.put("SVC_CARTABLE_GET_TASK_BY_PROCESS_ID", TaskWorkflowRole.FIND_TASK_BY_PROCESS_ID);
        aliases.put("SVC_CARTABLE_UPDATE_PROCESS_DESCRIPTION",
                TaskWorkflowRole.UPDATE_PROCESS_DESCRIPTION);
        return Map.copyOf(aliases);
    }
}
