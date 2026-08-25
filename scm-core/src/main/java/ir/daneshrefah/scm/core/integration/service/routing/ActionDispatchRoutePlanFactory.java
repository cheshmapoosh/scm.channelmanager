package ir.daneshrefah.scm.core.integration.service.routing;

import ir.daneshrefah.scm.common.model.gateway.RoutingStrategy;
import ir.daneshrefah.scm.common.model.gateway.Service;
import ir.daneshrefah.scm.common.model.gateway.ServiceActionNamePolicy;
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
    private final ServiceOperationSelector operationSelector;
    private final ServiceOperationEndpointResolver endpointResolver;
    private final RoutingOperationMetadataResolver operationMetadataResolver;

    public ActionDispatchPlan create(Service service) {
        String serviceCode = requireService(service);
        List<ServiceOperation> activeOperations = operationSelector.activeAll(service);
        if (activeOperations.isEmpty()) {
            throw invalid(serviceCode,
                    "must have at least one active executable action binding.");
        }

        Map<String, RoutingPlan> actionPlans = new LinkedHashMap<>();
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
            String actionName = canonicalAction(serviceCode, serviceOperation.getActionName());
            serviceOperation.setOperationName(operationName);
            serviceOperation.setActionName(actionName);
            if (actionPlans.containsKey(actionName)) {
                throw invalid(serviceCode,
                        "duplicate canonical actionName=" + actionName + ".");
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
                    serviceCode + ":" + actionName,
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
                                    actionName,
                                    null,
                                    operationName,
                                    0,
                                    spanKind
                            )
                    ))
            );
            actionPlans.put(actionName, childPlan);
        }
        return new ActionDispatchPlan(serviceCode, actionPlans);
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

    private String canonicalAction(String serviceCode, String actionName) {
        try {
            return ServiceActionNamePolicy.canonicalize(actionName);
        } catch (IllegalArgumentException exception) {
            throw invalid(serviceCode,
                    "an active binding has a missing or invalid actionName.", exception);
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
