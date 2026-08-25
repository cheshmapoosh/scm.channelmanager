package ir.daneshrefah.scm.core.integration.gateway.inbound;

import ir.daneshrefah.scm.common.model.gateway.InboundChannelServiceDefinition;
import ir.daneshrefah.scm.common.model.gateway.RoutingStrategy;
import ir.daneshrefah.scm.common.model.gateway.ServiceActionNamePolicy;
import ir.daneshrefah.scm.common.model.service.HttpMethod;
import ir.daneshrefah.scm.core.integration.gateway.GatewayInboundRouteContext;
import ir.daneshrefah.scm.core.integration.service.routing.ActionDispatchPlan;
import ir.daneshrefah.scm.core.integration.service.routing.ActionDispatchPlanCatalog;
import ir.daneshrefah.scm.core.integration.service.routing.ServiceActionException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Component
public class InboundRouteDefinitionValidator {
    private final InboundRouteActionConfigExtractor actionConfigExtractor;
    private final ActionDispatchPlanCatalog actionDispatchPlanCatalog;

    public InboundRouteDefinitionValidator(
            InboundRouteActionConfigExtractor actionConfigExtractor,
            ActionDispatchPlanCatalog actionDispatchPlanCatalog
    ) {
        this.actionConfigExtractor = actionConfigExtractor;
        this.actionDispatchPlanCatalog = actionDispatchPlanCatalog;
    }

    public Map<InboundChannelServiceDefinition, InboundRouteActionConfig> validate(
            GatewayInboundRouteContext context,
            List<InboundChannelServiceDefinition> definitions
    ) {
        Set<String> methodPaths = new HashSet<>();
        Set<String> definitionIds = new HashSet<>();
        Map<InboundChannelServiceDefinition, InboundRouteActionConfig> configs = new IdentityHashMap<>();
        boolean actionDispatch = context.servicePlan().service() != null
                && context.servicePlan().service().getRoutingStrategy()
                == RoutingStrategy.ACTION_DISPATCH;
        ActionDispatchPlan dispatchPlan = actionDispatch
                ? actionDispatchPlanCatalog.planFor(context.servicePlan().service())
                : null;

        for (InboundChannelServiceDefinition definition : definitions) {
            validatePath(context, definition);
            validateMethodPath(context, definition, methodPaths);
            validateDefinitionId(context, definition, definitionIds);
            InboundRouteActionConfig config;
            try {
                config = actionConfigExtractor.extract(definition);
            } catch (RuntimeException exception) {
                if (actionDispatch) {
                    throw invalidAction(context, definition,
                            "field=inboundAction configuration is invalid", exception);
                }
                throw exception;
            }
            if (config.configured() && config.inboundAction() == null) {
                if (actionDispatch) {
                    throw invalidAction(context, definition,
                            "field=inboundAction must not be blank");
                }
                throw invalid(context, definition,
                        "field=inboundAction must not be blank");
            }
            if (actionDispatch) {
                config = validateActionDispatchConfig(
                        context, definition, config, dispatchPlan);
            }
            configs.put(definition, config);
        }
        return configs;
    }

    private InboundRouteActionConfig validateActionDispatchConfig(
            GatewayInboundRouteContext context,
            InboundChannelServiceDefinition definition,
            InboundRouteActionConfig config,
            ActionDispatchPlan dispatchPlan
    ) {
        if (!config.configured() || config.inboundAction() == null) {
            throw invalidAction(context, definition,
                    "field=inboundAction is required for ACTION_DISPATCH");
        }
        String canonical;
        try {
            canonical = ServiceActionNamePolicy.canonicalize(config.inboundAction());
        } catch (IllegalArgumentException exception) {
            throw invalidAction(context, definition,
                    "field=inboundAction is invalid", exception);
        }
        if (!dispatchPlan.containsAction(canonical)) {
            throw invalidAction(context, definition,
                    "field=inboundAction does not reference a configured service action");
        }
        return new InboundRouteActionConfig(true, canonical);
    }

    private void validatePath(
            GatewayInboundRouteContext context,
            InboundChannelServiceDefinition definition
    ) {
        if (definition == null || StringUtils.isBlank(definition.getPath())) {
            throw invalid(context, definition, "field=path must not be blank");
        }
    }

    private void validateMethodPath(
            GatewayInboundRouteContext context,
            InboundChannelServiceDefinition definition,
            Set<String> methodPaths
    ) {
        HttpMethod method = definition.getMethod() == null ? HttpMethod.POST : definition.getMethod();
        String normalizedPath = StringUtils.strip(definition.getPath().trim(), "/")
                .toLowerCase(Locale.ROOT);
        String key = method.name() + ":" + normalizedPath;
        if (!methodPaths.add(key)) {
            throw invalid(context, definition,
                    "duplicate method + path=" + key + " for the same service and gateway");
        }
    }

    private void validateDefinitionId(
            GatewayInboundRouteContext context,
            InboundChannelServiceDefinition definition,
            Set<String> definitionIds
    ) {
        String definitionId = StringUtils.trimToNull(definition.getId());
        if (definitionId != null && !definitionIds.add(definitionId)) {
            throw invalid(context, definition,
                    "duplicate channelServiceDefinitionId=" + definitionId
                            + " cannot produce a stable unique route id");
        }
    }

    private IllegalStateException invalid(
            GatewayInboundRouteContext context,
            InboundChannelServiceDefinition definition,
            String message
    ) {
        String serviceCode = context.servicePlan().service() == null
                ? "<null>"
                : String.valueOf(context.servicePlan().service().getCode());
        return new IllegalStateException("Invalid inbound route definition for gatewayName="
                + context.gatewayChannel().getName()
                + ", serviceCode=" + serviceCode
                + ", channelServiceDefinitionId="
                + (definition == null ? "<null>" : String.valueOf(definition.getId()))
                + ": " + message);
    }

    private ServiceActionException invalidAction(
            GatewayInboundRouteContext context,
            InboundChannelServiceDefinition definition,
            String message
    ) {
        return invalidAction(context, definition, message, null);
    }

    private ServiceActionException invalidAction(
            GatewayInboundRouteContext context,
            InboundChannelServiceDefinition definition,
            String message,
            Throwable cause
    ) {
        String serviceCode = context.servicePlan().service() == null
                ? "<null>"
                : String.valueOf(context.servicePlan().service().getCode());
        return ServiceActionException.configurationInvalid(
                "Invalid ACTION_DISPATCH inbound route for gateway="
                        + context.gatewayChannel().getName()
                        + ", service=" + serviceCode
                        + ", channelServiceDefinitionId="
                        + (definition == null ? "<null>" : String.valueOf(definition.getId()))
                        + ": " + message,
                cause
        );
    }
}
