package ir.daneshrefah.scm.core.integration.runtime;

import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Locale;

public final class RouteIdSupport {
    private RouteIdSupport() {
    }

    public static String gatewayRouteId(RuntimeTargetKind targetKind,
                                        String gatewayName,
                                        String serviceCode,
                                        String serviceVersion) {
        String version = normalizeOptional(serviceVersion);
        String suffix = version != null ? "." + version : "";
        return "gw."
                + targetKindShort(targetKind)
                + "."
                + normalizeGatewayScopeName(gatewayName)
                + "."
                + normalizeRequired(serviceCode, "serviceCode")
                + suffix;
    }

    public static String gatewayRouteId(RuntimeTargetKind targetKind,
                                        String gatewayName,
                                        String serviceCode,
                                        String serviceVersion,
                                        String uniquenessKey) {
        return gatewayRouteId(targetKind, gatewayName, serviceCode, serviceVersion)
                + "."
                + shortHash(uniquenessKey);
    }

    public static String serviceRouteId(RuntimeTargetKind targetKind,
                                        String gatewayName,
                                        String serviceCode) {
        return "svc."
                + targetKindShort(targetKind)
                + "."
                + normalizeGatewayScopeName(gatewayName)
                + "."
                + normalizeRequired(serviceCode, "serviceCode");
    }

    public static String operationRouteId(String operationName) {
        return "op." + normalizeOperationName(operationName);
    }

    public static String targetKindShort(RuntimeTargetKind targetKind) {
        if (targetKind == null) {
            throw new IllegalArgumentException("targetKind is required for route id.");
        }
        return switch (targetKind) {
            case CHANNEL -> "ch";
            case SERVICE_DOMAIN -> "dm";
        };
    }

    public static RuntimeTargetKind targetKindFromGatewayName(String gatewayName) {
        String normalized = StringUtils.trimToNull(gatewayName);
        if (normalized == null) {
            throw new IllegalArgumentException("gatewayName is required for route id.");
        }
        String lowerCaseName = normalized.toLowerCase(Locale.ROOT);
        if (lowerCaseName.startsWith("channel.")) {
            return RuntimeTargetKind.CHANNEL;
        }
        if (lowerCaseName.startsWith("domain.")) {
            return RuntimeTargetKind.SERVICE_DOMAIN;
        }
        throw new IllegalArgumentException("Gateway name '" + gatewayName
                + "' cannot be mapped to a route target kind.");
    }

    public static String normalizeGatewayScopeName(String gatewayName) {
        String scopeName = StringUtils.trimToNull(gatewayName);
        if (scopeName == null) {
            throw new IllegalArgumentException("gatewayName is required for route id.");
        }
        String lowerCaseScopeName = scopeName.toLowerCase(Locale.ROOT);
        if (lowerCaseScopeName.startsWith("domain.")) {
            scopeName = scopeName.substring("domain.".length());
        } else if (lowerCaseScopeName.startsWith("channel.")) {
            scopeName = scopeName.substring("channel.".length());
        }
        return normalizeRequired(scopeName, "gatewayName");
    }

    public static String normalizeRequired(String value, String label) {
        String normalized = normalizeOptional(value);
        if (normalized == null) {
            throw new IllegalArgumentException(label + " is required for route id.");
        }
        return normalized;
    }

    public static String normalizeOptional(String value) {
        String normalized = StringUtils.trimToNull(value);
        if (normalized == null) {
            return null;
        }
        normalized = normalized.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("-+", "-")
                .replaceAll("(^-+|-+$)", "");
        return StringUtils.isBlank(normalized) ? null : normalized;
    }

    private static String normalizeOperationName(String operationName) {
        String normalized = StringUtils.trimToNull(operationName);
        if (normalized == null) {
            throw new IllegalArgumentException("operationName is required for route id.");
        }
        normalized = normalized
                .replaceAll("[^A-Za-z0-9_.-]+", "-")
                .replaceAll("-+", "-")
                .replaceAll("(^-+|-+$)", "");
        if (StringUtils.isBlank(normalized)) {
            throw new IllegalArgumentException("operationName '" + operationName + "' cannot be normalized.");
        }
        return normalized;
    }

    private static String shortHash(String key) {
        String normalized = StringUtils.defaultIfBlank(key, "route");
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(normalized.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest).substring(0, 8);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not found", e);
        }
    }
}
