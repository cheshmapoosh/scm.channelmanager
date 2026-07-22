package ir.daneshrefah.scm.core.integration.service.routing.taskworkflow;

import ir.daneshrefah.scm.common.exception.AccessDeniedException;
import ir.daneshrefah.scm.common.exception.NoMatchRecordFoundException;
import ir.daneshrefah.scm.common.model.error.Error;
import ir.daneshrefah.scm.common.model.error.ScmFault;
import ir.daneshrefah.scm.common.model.gateway.GatewayChannel;
import ir.daneshrefah.scm.common.model.gateway.RoutingStrategy;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.message.MessageStatus;
import ir.daneshrefah.scm.core.integration.runtime.RuntimeRoutePlan;
import ir.daneshrefah.scm.core.integration.runtime.RuntimeRoutePlanProvider;
import ir.daneshrefah.scm.core.integration.runtime.RuntimeServicePlan;
import ir.daneshrefah.scm.core.integration.service.ServiceRouteUriResolver;
import ir.daneshrefah.scm.core.integration.service.lookup.EbServiceLookupService;
import ir.daneshrefah.scm.core.integration.service.lookup.EbServiceSnapshot;
import lombok.RequiredArgsConstructor;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static ir.daneshrefah.scm.common.model.error.ErrorCodes.ERROR_CODE_ACCESS_DENIED;

@Component
@RequiredArgsConstructor
public class TaskWorkflowServiceEntrypointRouteBuilder extends RouteBuilder {
    public static final String ROUTE_URI = "direct:task-workflow-service-entrypoint";
    public static final String ROUTE_ID = "svc.task-workflow.entrypoint";
    public static final String SERVICE_NOT_TASK_WORKFLOW = "SERVICE_NOT_TASK_WORKFLOW";

    private static final String SERVICE_CODE_PATH_VARIABLE = "serviceCode";

    private final EbServiceLookupService ebServiceLookupService;
    private final RuntimeRoutePlanProvider runtimeRoutePlanProvider;
    private final ServiceRouteUriResolver serviceRouteUriResolver;
    private final ProducerTemplate producerTemplate;

    @Override
    public void configure() {
        from(ROUTE_URI)
                .routeId(ROUTE_ID)
                .process(this::execute);
    }

    private void execute(Exchange exchange) {
        String serviceCode = serviceCode(exchange);
        EbServiceSnapshot snapshot = ebServiceLookupService.getActiveServiceByCode(serviceCode);
        if (snapshot == null) {
            throw new NoMatchRecordFoundException(serviceCode);
        }
        validateActive(snapshot);
        if (snapshot.routingStrategy() != RoutingStrategy.TASK_WORKFLOW) {
            exchange.getMessage().setBody(notTaskWorkflowFault(serviceCode));
            return;
        }

        GatewayChannel gatewayChannel = exchange.getProperty(
                Message.GATEWAY_CHANNEL,
                GatewayChannel.class
        );
        if (gatewayChannel == null) {
            throw new IllegalStateException(
                    "Task workflow gateway channel is required for serviceCode=" + serviceCode);
        }
        RuntimeRoutePlan routePlan = runtimeRoutePlanProvider.provide(gatewayChannel);
        RuntimeServicePlan servicePlan = requireServicePlan(routePlan, serviceCode);
        exchange.setProperty(Message.RUNTIME_ROUTE_PLAN, routePlan);
        exchange.setProperty(Message.RUNTIME_SERVICE_PLAN, servicePlan);
        producerTemplate.send(
                serviceRouteUriResolver.resolve(routePlan, servicePlan),
                exchange
        );
    }

    private RuntimeServicePlan requireServicePlan(
            RuntimeRoutePlan routePlan,
            String serviceCode
    ) {
        if (routePlan == null || routePlan.servicePlans() == null) {
            throw new NoMatchRecordFoundException(serviceCode);
        }
        return routePlan.servicePlans().stream()
                .filter(plan -> plan != null && plan.service() != null)
                .filter(plan -> serviceCode.equalsIgnoreCase(plan.service().getCode()))
                .findFirst()
                .orElseThrow(() -> new NoMatchRecordFoundException(serviceCode));
    }

    private String serviceCode(Exchange exchange) {
        String serviceCode = text(exchange.getMessage().getHeader(SERVICE_CODE_PATH_VARIABLE));
        if (serviceCode == null) {
            serviceCode = pathVariable(exchange, SERVICE_CODE_PATH_VARIABLE);
        }
        if (serviceCode == null) {
            throw new IllegalArgumentException(
                    "Task workflow serviceCode path variable is required.");
        }
        return serviceCode;
    }

    private void validateActive(EbServiceSnapshot snapshot) {
        if (snapshot.active()) {
            return;
        }
        throw new AccessDeniedException(
                "service",
                ERROR_CODE_ACCESS_DENIED,
                "Service " + snapshot.code() + " is inactive.");
    }

    private ScmFault notTaskWorkflowFault(String serviceCode) {
        String message = "Service \"" + serviceCode
                + "\" is not configured for task workflow execution.";
        Error error = new Error(
                SERVICE_NOT_TASK_WORKFLOW,
                SERVICE_NOT_TASK_WORKFLOW,
                message,
                null,
                MessageStatus.SC_ERROR_VALIDATION,
                null
        );
        return ScmFault.builder()
                .status(MessageStatus.SC_ERROR_VALIDATION)
                .title(SERVICE_NOT_TASK_WORKFLOW)
                .errors(List.of(error))
                .build();
    }

    @SuppressWarnings("unchecked")
    private String pathVariable(Exchange exchange, String name) {
        Map<String, Object> variables = exchange.getProperty(
                Message.INBOUND_PATH_VARIABLES,
                Map.class
        );
        if (variables == null || variables.isEmpty()) {
            return null;
        }
        return variables.entrySet().stream()
                .filter(entry -> entry.getKey().equalsIgnoreCase(name))
                .map(Map.Entry::getValue)
                .map(this::text)
                .filter(value -> value != null)
                .findFirst()
                .orElse(null);
    }

    private String text(Object value) {
        return value == null ? null : StringUtils.trimToNull(String.valueOf(value));
    }
}
