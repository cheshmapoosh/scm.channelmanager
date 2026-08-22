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
        return switch (normalized) {
            case "START_PROCESS" -> TaskWorkflowCommand.START_PROCESS;
            case "TASK_COMPLETE", "COMPLETE_TASK" -> TaskWorkflowCommand.TASK_COMPLETE;
            case "APPROVE_AND_EXECUTE" -> TaskWorkflowCommand.APPROVE_AND_EXECUTE;
            case "REJECT_PROCESS" -> TaskWorkflowCommand.REJECT_PROCESS;
            case "GET_ALL_PROCESS" -> TaskWorkflowCommand.GET_ALL_PROCESS;
            case "GET_ALL_TASK" -> TaskWorkflowCommand.GET_ALL_TASK;
            case "GET_TASK" -> TaskWorkflowCommand.GET_TASK;
            case "UPDATE_DESCRIPTION" -> TaskWorkflowCommand.UPDATE_DESCRIPTION;
            case "DELETE_PROCUREMENT" -> TaskWorkflowCommand.DELETE_PROCUREMENT;
            case "FIND_PROCUREMENT_BY_ACCOUNT" -> TaskWorkflowCommand.FIND_PROCUREMENT_BY_ACCOUNT;
            case "FIND_PROCUREMENT_BY_NATIONAL" -> TaskWorkflowCommand.FIND_PROCUREMENT_BY_NATIONAL;
            case "PROCUREMENT_STATEMENT_INQUIRY" -> TaskWorkflowCommand.PROCUREMENT_STATEMENT_INQUIRY;
            case "WITHDRAW" -> TaskWorkflowCommand.WITHDRAW;
            default -> throw new IllegalStateException(
                    "Unsupported TASK_WORKFLOW inboundAction=" + inboundAction);
        };
    }
}
