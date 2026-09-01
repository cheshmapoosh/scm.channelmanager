package ir.daneshrefah.scm.core.integration.service.routing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.model.definition.Definition;
import ir.daneshrefah.scm.common.model.gateway.InboundActionPolicy;
import ir.daneshrefah.scm.common.model.gateway.RoutingStrategy;
import ir.daneshrefah.scm.common.model.gateway.Service;
import ir.daneshrefah.scm.common.model.gateway.ServiceOperation;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

@Component
@RequiredArgsConstructor
public class ActionDispatchRoutePlanFactory {
    private static final String INBOUND_ACTION = "inboundAction";

    private final ServiceOperationSelector operationSelector;
    private final ServiceOperationEndpointResolver endpointResolver;
    private final RoutingOperationMetadataResolver operationMetadataResolver;
    private final ObjectMapper objectMapper;

    public ActionDispatchPlan create(Service service) {
        String serviceCode = requireService(service);
        List<ServiceOperation> activeOperations = operationSelector.activeAll(service);
        if (activeOperations.isEmpty()) {
            throw invalid(serviceCode,
                    "must have at least one active executable action binding.");
        }

        Map<String, RoutingPlan> plansByInboundAction = new LinkedHashMap<>();
        Set<String> operationNames = new HashSet<>();
        for (ServiceOperation serviceOperation : activeOperations) {
            if (ServiceOperationDefinitionClassifier.isActionPlan(serviceOperation)) {
                throw invalid(serviceCode,
                        "ACTION_PLAN technical bindings cannot be exposed as service actions.");
            }
            String operationName = StringUtils.trimToNull(serviceOperation.getOperationName());
            if (operationName == null) {
                throw invalid(serviceCode, "an active action binding has a blank operationName.");
            }
            String inboundAction = resolveInboundAction(serviceCode, serviceOperation);
            serviceOperation.setOperationName(operationName);
            if (plansByInboundAction.containsKey(inboundAction)) {
                throw invalid(serviceCode,
                        "duplicate Definition.details.inboundAction=" + inboundAction + ".");
            }
            if (!operationNames.add(operationName.toLowerCase(Locale.ROOT))) {
                throw invalid(serviceCode,
                        "multiple actions reference operationName=" + operationName
                                + "; action aliases are not supported.");
            }
            String spanKind;
            try {
                spanKind = operationMetadataResolver.spanKind(operationName);
            } catch (RuntimeException exception) {
                throw invalid(serviceCode,
                        "active Operation metadata is unavailable for operationName="
                                + operationName + ".", exception);
            }
            RoutingPlan childPlan = new RoutingPlan(
                    serviceCode + ":" + inboundAction,
                    RoutingStrategy.ACTION_DISPATCH,
                    List.of(new RoutingStepPlan(
                            operationName,
                            0,
                            serviceOperation,
                            endpointResolver.resolveRegisteredOperation(operationName),
                            (exchange, execution) -> execution.originalRequest(),
                            null,
                            new RoutingStepObservationContext(
                                    serviceCode,
                                    inboundAction,
                                    null,
                                    operationName,
                                    0,
                                    spanKind
                            )
                    ))
            );
            plansByInboundAction.put(inboundAction, childPlan);
        }
        return new ActionDispatchPlan(serviceCode, plansByInboundAction);
    }

    private String requireService(Service service) {
        if (service == null || service.getRoutingStrategy() != RoutingStrategy.ACTION_DISPATCH) {
            throw ServiceActionException.configurationInvalid(
                    "ActionDispatchRoutePlanFactory requires an ACTION_DISPATCH service.");
        }
        String serviceCode = StringUtils.trimToNull(service.getCode());
        if (serviceCode == null) {
            throw ServiceActionException.configurationInvalid(
                    "ACTION_DISPATCH service code must not be blank.");
        }
        return serviceCode;
    }

    private String resolveInboundAction(
            String serviceCode,
            ServiceOperation serviceOperation
    ) {
        Definition definition = serviceOperation.getDefinition();
        if (definition == null) {
            throw invalid(serviceCode,
                    "active service operation=" + serviceOperation.getOperationName()
                            + " must reference a Definition.");
        }
        if (StringUtils.isBlank(definition.getDetails())) {
            throw invalid(serviceCode,
                    "Definition.details is required for operationName="
                            + serviceOperation.getOperationName() + ".");
        }

        JsonNode details;
        try {
            details = objectMapper.readTree(definition.getDetails());
        } catch (JsonProcessingException exception) {
            throw invalid(serviceCode,
                    "Definition.details must contain valid JSON for operationName="
                            + serviceOperation.getOperationName() + ".", exception);
        }
        if (!details.isObject()) {
            throw invalid(serviceCode,
                    "Definition.details must be a JSON object for operationName="
                            + serviceOperation.getOperationName() + ".");
        }
        JsonNode configuredAction = details.get(INBOUND_ACTION);
        if (configuredAction == null || !configuredAction.isTextual()) {
            throw invalid(serviceCode,
                    "Definition.details.inboundAction must be a string for operationName="
                            + serviceOperation.getOperationName() + ".");
        }
        try {
            return InboundActionPolicy.canonicalize(configuredAction.asText());
        } catch (IllegalArgumentException exception) {
            throw invalid(serviceCode,
                    "Definition.details.inboundAction is invalid for operationName="
                            + serviceOperation.getOperationName() + ".", exception);
        }
    }

    private ServiceActionException invalid(String serviceCode, String message) {
        return invalid(serviceCode, message, null);
    }

    private ServiceActionException invalid(
            String serviceCode,
            String message,
            Throwable cause
    ) {
        return ServiceActionException.configurationInvalid(
                "Invalid ACTION_DISPATCH configuration for service=" + serviceCode
                        + ": " + message,
                cause
        );
    }
}
