package ir.daneshrefah.scm.core.integration.runtime;

import org.apache.commons.lang3.StringUtils;

import java.util.Locale;

public enum RuntimeMode {
    CHANNEL,
    SERVICE_DOMAIN,
    CHANNEL_AND_SERVICE_DOMAIN;

    public static final RuntimeMode DEFAULT = CHANNEL_AND_SERVICE_DOMAIN;

    public static RuntimeMode from(String value) {
        String normalizedValue = StringUtils.trimToNull(value);
        if (normalizedValue == null) {
            return DEFAULT;
        }
        normalizedValue = normalizedValue
                .replace('-', '_')
                .replace('.', '_')
                .replace(' ', '_')
                .toUpperCase(Locale.ROOT);
        return RuntimeMode.valueOf(normalizedValue);
    }

    public boolean accepts(RuntimeTargetKind targetKind) {
        return switch (this) {
            case CHANNEL -> targetKind == RuntimeTargetKind.CHANNEL;
            case SERVICE_DOMAIN -> targetKind == RuntimeTargetKind.SERVICE_DOMAIN;
            case CHANNEL_AND_SERVICE_DOMAIN -> true;
        };
    }

    public boolean channelGatewayRoutesEnabled(RuntimeTargetKind targetKind) {
        return targetKind == RuntimeTargetKind.CHANNEL && accepts(targetKind);
    }

    public boolean serviceExecutionRoutesEnabled(RuntimeTargetKind targetKind) {
        return accepts(targetKind);
    }
}
