package ir.daneshrefah.scm.core.integration.gateway;

import ir.daneshrefah.scm.common.model.gateway.InboundChannelServiceDefinition;
import ir.daneshrefah.scm.core.utils.RouteUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Locale;

final class GatewayRouteIdFactory {
    private GatewayRouteIdFactory() {
    }

    static String singleRouteId(String serviceCode, String serviceVersion) {
        return servicePart(serviceCode) + "-" + serviceVersion + "-route";
    }

    static String inboundRouteId(String serviceCode, String serviceVersion, InboundChannelServiceDefinition definition) {
        return singleRouteId(serviceCode, serviceVersion)
                + "-"
                + RouteUtils.getInstance().generateRouteUniqId(routeKey(definition));
    }

    private static String routeKey(InboundChannelServiceDefinition definition) {
        if (definition == null) {
            return "inbound";
        }
        String definitionId = StringUtils.trimToNull(definition.getId());
        if (definitionId != null) {
            return definitionId;
        }
        String method = definition.getMethod() != null ? definition.getMethod().name() : "";
        String path = StringUtils.trimToEmpty(definition.getPath());
        String key = StringUtils.trimToNull(method + ":" + path);
        return key != null ? key : "inbound";
    }

    private static String servicePart(String serviceCode) {
        String normalized = StringUtils.trimToNull(serviceCode);
        if (normalized == null) {
            throw new IllegalArgumentException("Service code is required for gateway route id.");
        }
        normalized = normalized.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-+|-+$)", "");
        if (StringUtils.isBlank(normalized)) {
            throw new IllegalArgumentException("Service code '" + serviceCode + "' cannot be normalized.");
        }
        return normalized;
    }
}
