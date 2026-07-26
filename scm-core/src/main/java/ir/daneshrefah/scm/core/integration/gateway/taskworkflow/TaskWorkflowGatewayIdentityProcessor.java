package ir.daneshrefah.scm.core.integration.gateway.taskworkflow;

import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.protocol.ProtocolType;
import ir.daneshrefah.scm.core.integration.runtime.RuntimeRoutePlan;
import ir.daneshrefah.scm.core.integration.runtime.RuntimeServicePlan;
import ir.daneshrefah.scm.core.integration.service.ServiceRouteUriResolver;
import lombok.RequiredArgsConstructor;
import org.apache.camel.Exchange;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TaskWorkflowGatewayIdentityProcessor {
    private final TaskWorkflowRouteIdentityResolverRegistry resolverRegistry;
    private final ServiceRouteUriResolver serviceRouteUriResolver;

    public void resolve(
            Exchange exchange,
            TaskWorkflowRuntimeServiceRegistry serviceRegistry
    ) {
        ProtocolType protocol = exchange.getProperty(
                Message.GATEWAY_CHANNEL_PROTOCOL, ProtocolType.class);
        TaskWorkflowRouteIdentity identity = resolverRegistry
                .getRequired(protocol)
                .resolve(exchange);
        String serviceVersion = exchange.getProperty(
                Message.SERVICE_VERSION, String.class);
        if (serviceVersion == null || serviceVersion.isBlank()) {
            throw new TaskWorkflowRouteIdentityException(
                    "TASK_WORKFLOW gateway route version is unavailable"
            );
        }
        RuntimeRoutePlan routePlan = exchange.getProperty(
                Message.RUNTIME_ROUTE_PLAN, RuntimeRoutePlan.class);
        RuntimeServicePlan servicePlan = serviceRegistry.resolve(identity);

        exchange.setProperty(Message.SERVICE_CODE, identity.serviceCode());
        exchange.setProperty(Message.INBOUND_ROUTE_ACTION, identity.inboundAction());
        exchange.setProperty(Message.RUNTIME_SERVICE_PLAN, servicePlan);
        exchange.setProperty(Message.SERVICE, servicePlan.service());
        exchange.setProperty(Message.CHANNEL_SERVICE_ACCESS,
                servicePlan.channelServiceAccess());
        exchange.setProperty(Message.SERVICE_ROUTE_URI,
                serviceRouteUriResolver.resolve(routePlan, servicePlan));
    }
}
