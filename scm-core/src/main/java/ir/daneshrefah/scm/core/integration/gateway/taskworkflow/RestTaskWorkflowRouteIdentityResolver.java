package ir.daneshrefah.scm.core.integration.gateway.taskworkflow;

import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.protocol.ProtocolType;
import org.apache.camel.Exchange;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class RestTaskWorkflowRouteIdentityResolver
        implements TaskWorkflowRouteIdentityResolver {
    private static final String SERVICE_CODE = "serviceCode";
    private static final String INBOUND_ACTION = "inboundAction";

    @Override
    public boolean supports(ProtocolType protocol) {
        return protocol == ProtocolType.REST;
    }

    @Override
    public TaskWorkflowRouteIdentity resolve(Exchange exchange) {
        if (exchange == null) {
            throw new TaskWorkflowRouteIdentityException(
                    "Cannot resolve TASK_WORKFLOW REST identity without an exchange");
        }
        Object value = exchange.getProperty(Message.INBOUND_PARAMETERS);
        if (!(value instanceof Map<?, ?> parameters)) {
            throw new TaskWorkflowRouteIdentityException(
                    "TASK_WORKFLOW REST route variables are unavailable");
        }
        return new TaskWorkflowRouteIdentity(
                text(parameters, SERVICE_CODE),
                text(parameters, INBOUND_ACTION)
        );
    }

    private String text(Map<?, ?> parameters, String name) {
        Object value = parameters.entrySet().stream()
                .filter(entry -> entry.getKey() instanceof String key
                        && key.equalsIgnoreCase(name))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
        if (value == null || String.valueOf(value).isBlank()) {
            throw new TaskWorkflowRouteIdentityException(
                    "TASK_WORKFLOW REST path variable " + name + " is required");
        }
        return String.valueOf(value).trim();
    }
}
