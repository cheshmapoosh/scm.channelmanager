package ir.daneshrefah.scm.provider.shetab.customizer;

import ir.daneshrefah.scm.common.provider.message.ProviderExchange;
import ir.daneshrefah.scm.provider.shetab.config.ShetabResolvedConfig;
import org.apache.commons.lang3.StringUtils;
import org.jpos.iso.ISOMsg;

import java.util.Map;

final class ShetabCustomizerSupport {
    private ShetabCustomizerSupport() {
    }

    static ISOMsg isoMessage(ProviderExchange exchange) {
        Object nativeRequest = exchange.request().nativeRequest();
        if (nativeRequest instanceof ISOMsg isoMsg) {
            return isoMsg;
        }
        throw new IllegalStateException("Shetab customizer requires ISOMsg native request");
    }

    static ISOMsg isoResponse(ProviderExchange exchange) {
        Object nativeResponse = exchange.response() == null ? null : exchange.response().nativeResponse();
        if (nativeResponse instanceof ISOMsg isoMsg) {
            return isoMsg;
        }
        throw new IllegalStateException("Shetab customizer requires ISOMsg native response");
    }

    static ShetabResolvedConfig resolvedConfig(ProviderExchange exchange) {
        return exchange.context().resolvedProviderConfig(ShetabResolvedConfig.class)
                .orElseThrow(() -> new IllegalStateException("Shetab provider configuration is unavailable"));
    }

    static String sourceValue(ProviderExchange exchange, String source) {
        String path = StringUtils.defaultIfBlank(source, "").trim();
        if (path.isEmpty()) {
            return null;
        }
        Object current = exchange.request().body();
        if (current == null) {
            return null;
        }
        for (String segment : path.split("\\.")) {
            if (!(current instanceof Map<?, ?> map)) {
                return null;
            }
            current = map.get(segment);
            if (current == null) {
                return null;
            }
        }
        return StringUtils.trimToNull(String.valueOf(current));
    }

    static boolean booleanSource(ProviderExchange exchange, String key) {
        String value = sourceValue(exchange, "security." + key);
        return value != null && Boolean.parseBoolean(value);
    }

    static String safeField(ISOMsg msg, int field) {
        try {
            return msg != null ? msg.getString(field) : null;
        } catch (Exception e) {
            return null;
        }
    }

    static void setIsoField(ProviderExchange exchange, ISOMsg msg, int field, String value) {
        if (StringUtils.isBlank(value)) {
            msg.unset(field);
            exchange.request().fields().remove(String.valueOf(field));
            return;
        }
        msg.set(field, value);
        exchange.request().putField(String.valueOf(field), value);
    }
}
