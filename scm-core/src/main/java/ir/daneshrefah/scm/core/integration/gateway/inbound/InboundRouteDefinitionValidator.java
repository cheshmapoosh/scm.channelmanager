package ir.daneshrefah.scm.core.integration.gateway.inbound;

import ir.daneshrefah.scm.common.model.gateway.InboundChannelServiceDefinition;
import ir.daneshrefah.scm.common.model.service.HttpMethod;
import ir.daneshrefah.scm.core.integration.gateway.GatewayInboundRouteContext;
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

    public InboundRouteDefinitionValidator(InboundRouteActionConfigExtractor actionConfigExtractor) {
        this.actionConfigExtractor = actionConfigExtractor;
    }

    public Map<InboundChannelServiceDefinition, InboundRouteActionConfig> validate(
            GatewayInboundRouteContext context,
            List<InboundChannelServiceDefinition> definitions
    ) {
        Set<String> methodPaths = new HashSet<>();
        Set<String> definitionIds = new HashSet<>();
        Map<InboundChannelServiceDefinition, InboundRouteActionConfig> configs = new IdentityHashMap<>();

        for (InboundChannelServiceDefinition definition : definitions) {
            validatePath(context, definition);
            validateMethodPath(context, definition, methodPaths);
            validateDefinitionId(context, definition, definitionIds);
            InboundRouteActionConfig config = actionConfigExtractor.extract(definition);
            if (config.configured() && config.inboundAction() == null) {
                throw invalid(context, definition, "field=inboundAction must not be blank");
            }
            configs.put(definition, config);
        }
        return configs;
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
}
