package ir.daneshrefah.scm.core.integration.service.routing.taskworkflow;

import ir.daneshrefah.scm.common.model.gateway.ServiceOperation;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.core.integration.service.routing.ServiceOperationEndpointResolver;
import ir.daneshrefah.scm.core.integration.service.routing.ServiceOperationRouteMetadataSetter;
import lombok.RequiredArgsConstructor;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TaskWorkflowOperationInvoker {
    private final ServiceOperationEndpointResolver endpointResolver;
    private final ServiceOperationRouteMetadataSetter metadataSetter;
    private final ProducerTemplate producerTemplate;

    public Object invoke(
            Exchange exchange,
            TaskWorkflowRole role,
            ServiceOperation serviceOperation,
            Object request
    ) {
        return invoke(exchange, role, serviceOperation,
                endpointResolver.resolve(serviceOperation.getOperationName()), request);
    }

    public Object invoke(
            Exchange exchange,
            TaskWorkflowStepPlan step,
            Object request
    ) {
        return invoke(exchange, step.role(), step.serviceOperation(),
                step.operationEndpointUri(), request);
    }

    private Object invoke(
            Exchange exchange,
            TaskWorkflowRole role,
            ServiceOperation serviceOperation,
            String operationEndpointUri,
            Object request
    ) {
        metadataSetter.apply(exchange, serviceOperation);
        exchange.setProperty(Message.TASK_WORKFLOW_ROLE, role.name());
        exchange.removeProperty(Exchange.EXCEPTION_CAUGHT);
        exchange.setException(null);
        exchange.getMessage().setBody(request);

        Exchange result = producerTemplate.send(operationEndpointUri, exchange);
        Exception failure = result.getException();
        if (failure == null) {
            failure = result.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
        }
        if (failure != null) {
            if (failure instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            throw new IllegalStateException("TASK_WORKFLOW operation route failed for role="
                    + role + ", operationName="
                    + serviceOperation.getOperationName(), failure);
        }
        if (result != exchange) {
            exchange.getMessage().setBody(result.getMessage().getBody());
            exchange.getMessage().getHeaders().putAll(result.getMessage().getHeaders());
        }
        return exchange.getMessage().getBody();
    }
}
