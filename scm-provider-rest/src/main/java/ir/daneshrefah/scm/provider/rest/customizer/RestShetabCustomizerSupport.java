package ir.daneshrefah.scm.provider.rest.customizer;

import ir.daneshrefah.scm.common.provider.message.ProviderExchange;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

final class RestShetabCustomizerSupport {
    private RestShetabCustomizerSupport() {}


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


}
