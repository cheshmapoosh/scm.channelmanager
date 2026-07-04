package ir.daneshrefah.scm.core.integration.service.routing;

import com.fasterxml.jackson.databind.JsonNode;
import ir.daneshrefah.scm.common.model.gateway.RoutingStrategy;
import ir.daneshrefah.scm.common.model.message.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.model.FilterDefinition;
import org.apache.camel.model.ProcessorDefinition;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChainOnApproveServiceTargetRoutingHandler implements ServiceTargetRoutingHandler {
    private final ChainOnApproveRoutePlanFactory routePlanFactory;
    private final ServiceOperationRouteMetadataSetter metadataSetter;

    @Override
    public RoutingStrategy strategy() {
        return RoutingStrategy.CHAIN_ON_APPROVE;
    }

    @Override
    public void buildTarget(ServiceTargetRouteContext context) {
        ChainOnApproveRoutePlan plan = routePlanFactory.create(context.service());
        log.debug("Building CHAIN_ON_APPROVE service target routeId={} serviceCode={} operationCount={}",
                context.route().getRouteId(), context.service().getCode(), plan.steps().size());
        appendStep(context.route(), plan, 0);
    }

    private void appendStep(
            ProcessorDefinition<?> route,
            ChainOnApproveRoutePlan plan,
            int index
    ) {
        ChainOnApproveStepPlan currentStep = plan.steps().get(index);
        metadataSetter.apply(route, currentStep.serviceOperation());
        route.to(currentStep.operationEndpointUri());

        if (index + 1 >= plan.steps().size()) {
            return;
        }

        FilterDefinition approved = route.filter(exchange -> isApproved(exchange, plan, currentStep));
        appendStep(approved, plan, index + 1);
        approved.end();
    }

    private boolean isApproved(
            Exchange exchange,
            ChainOnApproveRoutePlan plan,
            ChainOnApproveStepPlan step
    ) {
        Message message = exchange.getMessage().getBody(Message.class);
        if (message == null) {
            throw new IllegalStateException("CHAIN_ON_APPROVE requires Message body after operation "
                    + step.serviceOperation().getOperationName());
        }
        return step.approvalPolicy().isApproved(new OperationApprovalContext(
                plan.service(),
                step.serviceOperation(),
                step.approvalDefinition(),
                message
        ));
    }
}
