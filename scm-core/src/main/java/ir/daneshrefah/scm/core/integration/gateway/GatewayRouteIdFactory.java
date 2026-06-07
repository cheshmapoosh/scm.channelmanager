package ir.daneshrefah.scm.core.integration.gateway;

import ir.daneshrefah.scm.common.model.gateway.InboundChannelServiceDefinition;
import ir.daneshrefah.scm.core.integration.runtime.RouteIdSupport;
import ir.daneshrefah.scm.core.integration.runtime.RuntimeTargetKind;
import org.apache.commons.lang3.StringUtils;

final class GatewayRouteIdFactory {
    private GatewayRouteIdFactory() {
    }

    static String singleRouteId(RuntimeTargetKind targetKind,
                                String gatewayName,
                                String serviceCode,
                                String serviceVersion) {
        return RouteIdSupport.gatewayRouteId(targetKind, gatewayName, serviceCode, serviceVersion);
    }

    static String inboundRouteId(RuntimeTargetKind targetKind,
                                 String gatewayName,
                                 String serviceCode,
                                 String serviceVersion,
                                 InboundChannelServiceDefinition definition) {
        return RouteIdSupport.gatewayRouteId(
                targetKind,
                gatewayName,
                serviceCode,
                serviceVersion,
                routeKey(definition));
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

}
