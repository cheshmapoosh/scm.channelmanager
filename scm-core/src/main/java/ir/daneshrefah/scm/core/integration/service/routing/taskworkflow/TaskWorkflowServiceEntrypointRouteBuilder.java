package ir.daneshrefah.scm.core.integration.service.routing.taskworkflow;

import ir.daneshrefah.scm.common.dto.asset.ChannelServiceAccess;
import ir.daneshrefah.scm.common.exception.AccessDeniedException;
import ir.daneshrefah.scm.common.exception.NoMatchRecordFoundException;
import ir.daneshrefah.scm.common.model.error.Error;
import ir.daneshrefah.scm.common.model.error.ScmFault;
import ir.daneshrefah.scm.common.model.gateway.GatewayChannel;
import ir.daneshrefah.scm.common.model.gateway.RoutingStrategy;
import ir.daneshrefah.scm.common.model.gateway.Service;
import ir.daneshrefah.scm.common.model.gateway.ServiceOperation;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.message.MessageStatus;
import ir.daneshrefah.scm.core.entity.gateway.ServiceOperationEntity;
import ir.daneshrefah.scm.core.integration.runtime.RuntimeServicePlan;
import ir.daneshrefah.scm.core.integration.service.guard.ChannelServiceAccessGuard;
import ir.daneshrefah.scm.core.integration.service.guard.RuntimeChannelGuard;
import ir.daneshrefah.scm.core.integration.service.lookup.EbServiceLookupService;
import ir.daneshrefah.scm.core.integration.service.lookup.EbServiceSnapshot;
import ir.daneshrefah.scm.core.mapper.gateway.ServiceOperationMapper;
import ir.daneshrefah.scm.core.repository.gateway.ServiceOperationRepository;
import lombok.RequiredArgsConstructor;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
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
    private final ServiceOperationRepository serviceOperationRepository;
    private final ServiceOperationMapper serviceOperationMapper;
    private final RuntimeChannelGuard runtimeChannelGuard;
    private final ChannelServiceAccessGuard channelServiceAccessGuard;
    private final TaskWorkflowOperationRoleConfigExtractor operationRoleExtractor;
    private final TaskWorkflowOperationInvoker operationInvoker;

    @Override
    public void configure() {
        from(ROUTE_URI)
                .routeId(ROUTE_ID)
                .process(this::execute);
    }

    private void execute(Exchange exchange) {
        String serviceCode = serviceCode(exchange);
        TaskWorkflowRole role = role(exchange);
        EbServiceSnapshot snapshot = ebServiceLookupService.getActiveServiceByCode(serviceCode);
        if (snapshot == null) {
            throw new NoMatchRecordFoundException(serviceCode);
        }

        Service service = service(snapshot);
        service.setServiceOperations(loadServiceOperations(snapshot));
        exchange.setProperty(Message.SERVICE, service);
        RuntimeServicePlan servicePlan = new RuntimeServicePlan(
                exchange.getProperty(Message.GATEWAY_CHANNEL, GatewayChannel.class),
                exchange.getProperty(Message.CHANNEL_SERVICE_ACCESS, ChannelServiceAccess.class),
                service,
                List.of()
        );
        exchange.setProperty(Message.RUNTIME_SERVICE_PLAN, servicePlan);

        validateActive(snapshot);
        runtimeChannelGuard.check(exchange, servicePlan);
        channelServiceAccessGuard.check(exchange, servicePlan);
        if (snapshot.routingStrategy() != RoutingStrategy.TASK_WORKFLOW) {
            exchange.getMessage().setBody(notTaskWorkflowFault(serviceCode));
            return;
        }

        ServiceOperation serviceOperation = operationForRole(service, role);
        Object request = requestPayload(exchange);
        exchange.setProperty(Message.SERVICE_LAYER_INVOCATION, true);
        operationInvoker.invoke(exchange, role, serviceOperation, request);
    }

    private String serviceCode(Exchange exchange) {
        String serviceCode = text(exchange.getMessage().getHeader(SERVICE_CODE_PATH_VARIABLE));
        if (serviceCode == null) {
            serviceCode = pathVariable(exchange, SERVICE_CODE_PATH_VARIABLE);
        }
        if (serviceCode == null) {
            throw new IllegalArgumentException("Task workflow serviceCode path variable is required.");
        }
        return serviceCode;
    }

    private TaskWorkflowRole role(Exchange exchange) {
        Object value = exchange.getProperty(Message.TASK_WORKFLOW_ROLE);
        if (value instanceof TaskWorkflowRole role) {
            return role;
        }
        String roleName = text(value);
        if (roleName == null) {
            throw new IllegalArgumentException("Task workflow role is required.");
        }
        try {
            return TaskWorkflowRole.valueOf(roleName.toUpperCase(Locale.ROOT)
                    .replace('-', '_')
                    .replace(' ', '_'));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Unsupported task workflow role: " + roleName, exception);
        }
    }

    private Service service(EbServiceSnapshot snapshot) {
        Service service = new Service();
        service.setId(shortId(snapshot));
        service.setCode(snapshot.code());
        service.setPublish(snapshot.active());
        service.setRoutingStrategy(snapshot.routingStrategy());
        return service;
    }

    private Short shortId(EbServiceSnapshot snapshot) {
        if (snapshot.id() == null) {
            throw new IllegalStateException("EbService id is required for serviceCode=" + snapshot.code());
        }
        int id = Math.toIntExact(snapshot.id());
        if (id < Short.MIN_VALUE || id > Short.MAX_VALUE) {
            throw new IllegalStateException("EbService id is outside Short range for serviceCode="
                    + snapshot.code() + ", id=" + snapshot.id());
        }
        return (short) id;
    }

    private List<ServiceOperation> loadServiceOperations(EbServiceSnapshot snapshot) {
        Short serviceId = shortId(snapshot);
        List<ServiceOperationEntity> entities = serviceOperationRepository.findAllByService_Id(serviceId);
        return entities.stream()
                .map(serviceOperationMapper::toModel)
                .toList();
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

    private ServiceOperation operationForRole(Service service, TaskWorkflowRole role) {
        ServiceOperation selected = null;
        for (ServiceOperation operation : service.getServiceOperations()) {
            if (operation == null || !Boolean.TRUE.equals(operation.getActive())) {
                continue;
            }
            TaskWorkflowRole operationRole = operationRoleExtractor.extract(service, operation).role();
            if (operationRole != role) {
                continue;
            }
            if (selected != null) {
                throw new IllegalStateException("Invalid TASK_WORKFLOW operations for serviceCode="
                        + service.getCode() + ", role=" + role
                        + ": role is assigned to more than one active operation: "
                        + selected.getOperationName() + ", " + operation.getOperationName());
            }
            selected = operation;
        }
        if (selected == null) {
            throw new IllegalStateException("Invalid TASK_WORKFLOW operations for serviceCode="
                    + service.getCode() + ", role=" + role
                    + ": no matching active ServiceOperation");
        }
        return selected;
    }

    private Object requestPayload(Exchange exchange) {
        Object originalBody = exchange.getProperty(Message.ORIGINAL_BODY);
        if (originalBody != null) {
            return originalBody;
        }
        Object internalMessage = exchange.getProperty(Message.INTERNAL_MESSAGE);
        if (internalMessage instanceof Message message) {
            return message.getPayload();
        }
        return exchange.getMessage().getBody();
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
        Map<String, Object> variables = exchange.getProperty(Message.INBOUND_PATH_VARIABLES, Map.class);
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
