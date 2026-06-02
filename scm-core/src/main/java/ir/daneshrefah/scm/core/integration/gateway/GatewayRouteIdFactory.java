package ir.daneshrefah.scm.core.integration.gateway;

import ir.daneshrefah.scm.core.utils.RouteUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Locale;

final class GatewayRouteIdFactory {
    private GatewayRouteIdFactory() {
    }

    static String singleRouteId(String serviceCode, String serviceVersion) {
        return servicePart(serviceCode) + "-" + serviceVersion + "-route";
    }

    static String groupRouteId(String serviceCode, String serviceVersion, String operationCode) {
        return singleRouteId(serviceCode, serviceVersion)
                + "-"
                + RouteUtils.getInstance().generateRouteUniqId(operationCode);
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
