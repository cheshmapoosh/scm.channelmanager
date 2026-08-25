package ir.daneshrefah.scm.core.integration.service.routing;

import ir.daneshrefah.scm.common.model.gateway.RoutingStrategy;
import ir.daneshrefah.scm.common.model.gateway.ServiceActionNamePolicy;
import ir.daneshrefah.scm.common.model.message.Message;
import org.apache.camel.Exchange;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Immutable startup-compiled action lookup for one service.
 */
public record ActionDispatchPlan(
        String serviceCode,
        Map<String, RoutingPlan> actionPlans
) {
    public ActionDispatchPlan {
        if (serviceCode == null || serviceCode.isBlank()) {
            throw ServiceActionException.configurationInvalid(
                    "ACTION_DISPATCH service code must not be blank.");
        }
        if (actionPlans == null || actionPlans.isEmpty()) {
            throw ServiceActionException.configurationInvalid(
                    "ACTION_DISPATCH service=" + serviceCode
                            + " must have at least one active executable action binding.");
        }
        Map<String, RoutingPlan> immutable = new LinkedHashMap<>();
        actionPlans.forEach((actionName, childPlan) -> {
            String canonical;
            try {
                canonical = ServiceActionNamePolicy.canonicalize(actionName);
            } catch (IllegalArgumentException exception) {
                throw ServiceActionException.configurationInvalid(
                        "ACTION_DISPATCH service=" + serviceCode
                                + " generated an invalid action key.", exception);
            }
            if (!canonical.equals(actionName)) {
                throw ServiceActionException.configurationInvalid(
                        "ACTION_DISPATCH service=" + serviceCode
                                + " generated a non-canonical action key.");
            }
            validateChildPlan(serviceCode, canonical, childPlan);
            if (immutable.putIfAbsent(canonical, childPlan) != null) {
                throw ServiceActionException.configurationInvalid(
                        "ACTION_DISPATCH service=" + serviceCode
                                + " generated duplicate canonical action=" + canonical + ".");
            }
        });
        actionPlans = Collections.unmodifiableMap(immutable);
    }

    public RoutingPlan requireAction(Exchange exchange) {
        if (exchange == null) {
            throw ServiceActionException.actionRequired();
        }
        Object configuredAction = exchange.getProperty(Message.INBOUND_ROUTE_ACTION);
        if (!(configuredAction instanceof String actionName) || actionName.isBlank()) {
            throw ServiceActionException.actionRequired();
        }
        String canonical;
        try {
            canonical = ServiceActionNamePolicy.canonicalize(actionName);
        } catch (IllegalArgumentException exception) {
            throw ServiceActionException.actionNotFound();
        }
        RoutingPlan selected = actionPlans.get(canonical);
        if (selected == null) {
            throw ServiceActionException.actionNotFound();
        }
        exchange.setProperty(Message.INBOUND_ROUTE_ACTION, canonical);
        return selected;
    }

    public boolean containsAction(String actionName) {
        try {
            return actionPlans.containsKey(ServiceActionNamePolicy.canonicalize(actionName));
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    private static void validateChildPlan(
            String serviceCode,
            String actionName,
            RoutingPlan childPlan
    ) {
        if (childPlan == null
                || childPlan.routingStrategy() != RoutingStrategy.ACTION_DISPATCH
                || childPlan.steps().size() != 1) {
            throw ServiceActionException.configurationInvalid(
                    "ACTION_DISPATCH service=" + serviceCode + ", action=" + actionName
                            + " must produce exactly one executable ACTION_DISPATCH step.");
        }
        RoutingStepPlan step = childPlan.steps().getFirst();
        if (step.serviceOperation() == null
                || step.serviceOperation().getOperationName() == null
                || step.serviceOperation().getOperationName().isBlank()
                || step.endpointUri() == null
                || step.endpointUri().isBlank()) {
            throw ServiceActionException.configurationInvalid(
                    "ACTION_DISPATCH service=" + serviceCode + ", action=" + actionName
                            + " generated an invalid executable step.");
        }
    }
}
