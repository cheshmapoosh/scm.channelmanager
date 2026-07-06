package ir.daneshrefah.scm.core.integration.service.routing.taskworkflow;

import ir.daneshrefah.scm.common.model.message.Message;
import org.apache.camel.Exchange;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class TaskWorkflowCommandResolver {

    public TaskWorkflowCommand resolve(Exchange exchange) {
        String inboundAction = exchange == null
                ? null
                : exchange.getProperty(Message.INBOUND_ROUTE_ACTION, String.class);
        return resolve(inboundAction);
    }

    public TaskWorkflowCommand resolve(String inboundAction) {
        String normalized = StringUtils.trimToNull(inboundAction);
        if (normalized == null) {
            throw new IllegalStateException("TASK_WORKFLOW requires exchange property "
                    + Message.INBOUND_ROUTE_ACTION);
        }
        normalized = normalized.toUpperCase(Locale.ROOT)
                .replace('-', '_')
                .replace(' ', '_');
        try {
            return TaskWorkflowCommand.valueOf(normalized);
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException("Unsupported TASK_WORKFLOW inboundAction="
                    + inboundAction, exception);
        }
    }
}
