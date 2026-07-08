package ir.daneshrefah.scm.provider.task.workflow;

import ir.daneshrefah.scm.common.model.message.Message;
import org.apache.camel.Exchange;
import org.springframework.util.StringUtils;

import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;

public class TaskWorkflowRoleResolver {
    private static final Set<TaskWorkflowRole> SUPPORTED_ROLES = EnumSet.allOf(TaskWorkflowRole.class);

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

        throw new IllegalArgumentException("scm-task:" + providerCode
                + " requires TaskWorkflowRole from exchange context.");
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

    private TaskWorkflowRole semanticRole(Object value) {
        String normalized = normalize(value);
        if (normalized == null) {
            return null;
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
}
